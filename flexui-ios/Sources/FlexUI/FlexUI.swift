import UIKit
import Foundation

/// Main entry point for FlexUI SDK.
/// Provides a Swift API for initializing and rendering FlexUI components.
public final class FlexUI {
    
    // MARK: - Singleton
    private static let shared = FlexUI()
    
    // MARK: - Private Properties
    private var isInitialized = false
    private var configFetcher: ConfigFetcher?
    private var configCache: ConfigCache?
    private var defaultTheme: FlexTheme = ThemeDefaults.defaultTheme
    private let backgroundQueue = DispatchQueue(label: "flexui.background", qos: .utility)
    
    // MARK: - Configuration
    
    /// Initialize FlexUI with base URL
    public static func configure(baseURL: String) {
        let config = FlexConfig(baseURL: baseURL)
        configure(config)
    }
    
    /// Initialize FlexUI with configuration
    public static func configure(_ config: FlexConfig) {
        shared.configFetcher = ConfigFetcher(
            baseURL: config.baseURL,
            connectTimeout: config.connectTimeout,
            readTimeout: config.readTimeout
        )
        shared.configCache = ConfigCache.shared
        
        if let defaultTheme = config.defaultTheme {
            shared.defaultTheme = ThemeDefaults.merge(theme: defaultTheme)
        }
        
        shared.isInitialized = true
    }
    
    // MARK: - Component Registration
    
    /// Register a custom component factory
    public static func registerComponent(
        _ type: String,
        factory: @escaping FlexComponentFactory
    ) {
        ComponentRegistry.shared.register(type: type, factory: factory)
    }
    
    // MARK: - Action Handling
    
    /// Register an action handler for a specific action type
    public static func onAction(
        _ type: String,
        handler: @escaping FlexActionHandler
    ) {
        ActionDispatcher.shared.registerHandler(type: type, handler: handler)
    }
    
    /// Register an event handler for callback actions
    public static func onEvent(
        _ eventName: String,
        handler: @escaping FlexActionHandler
    ) {
        ActionDispatcher.shared.registerEventHandler(eventName: eventName, handler: handler)
    }
    
    // MARK: - Rendering
    
    /// Render a screen synchronously
    public static func render(
        screenId: String,
        data: [String: Any] = [:]
    ) -> UIView? {
        shared.ensureInitialized()
        
        do {
            guard let configJson = shared.fetchConfigSync(screenId: screenId) else {
                return nil
            }
            return try shared.renderFromJson(configJson, data: data)
        } catch {
            print("FlexUI render error: \\(error)")
            return nil
        }
    }
    
    /// Render a screen asynchronously
    public static func renderAsync(
        screenId: String,
        data: [String: Any] = [:],
        completion: @escaping (Result<UIView, FlexError>) -> Void
    ) {
        shared.ensureInitialized()
        
        shared.backgroundQueue.async {
            do {
                if let view = render(screenId: screenId, data: data) {
                    DispatchQueue.main.async {
                        completion(.success(view))
                    }
                } else {
                    DispatchQueue.main.async {
                        completion(.failure(.renderError("No view rendered for screen: \\(screenId)")))
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    let flexError = error as? FlexError ?? .renderError("Render failed", underlyingError: error)
                    completion(.failure(flexError))
                }
            }
        }
    }
    
    /// Render from JSON string directly
    public static func renderFromJson(
        _ jsonString: String,
        data: [String: Any] = [:]
    ) throws -> UIView? {
        return try shared.renderFromJson(jsonString, data: data)
    }
    
    /// Create a view controller containing the rendered FlexUI view
    public static func viewController(
        screenId: String,
        data: [String: Any] = [:]
    ) -> UIViewController {
        let viewController = FlexViewController()
        
        renderAsync(screenId: screenId, data: data) { result in
            switch result {
            case .success(let view):
                viewController.setFlexView(view)
            case .failure(let error):
                viewController.setError(error)
            }
        }
        
        return viewController
    }
    
    // MARK: - Cache Management
    
    /// Refresh configuration cache for a screen
    public static func refreshConfig(screenId: String) {
        shared.ensureInitialized()
        shared.configCache?.remove(screenId: screenId)
    }
    
    /// Clear all cached configurations
    public static func clearCache() {
        shared.configCache?.clear()
    }
    
    // MARK: - Utility
    
    /// Check if FlexUI is initialized
    public static func isInitialized() -> Bool {
        return shared.isInitialized
    }
    
    /// Get current default theme
    public static func getDefaultTheme() -> FlexTheme {
        return shared.defaultTheme
    }
    
    /// Set default theme
    public static func setDefaultTheme(_ theme: FlexTheme) {
        shared.defaultTheme = ThemeDefaults.merge(theme: theme)
    }
    
    /// Get registered component types
    public static func getRegisteredComponents() -> Set<String> {
        return ComponentRegistry.shared.getRegisteredTypes()
    }
    
    /// Check if a component type is registered
    public static func isComponentRegistered(_ type: String) -> Bool {
        return ComponentRegistry.shared.isRegistered(type: type)
    }
    
    // MARK: - Private Methods
    
    private func ensureInitialized() {
        guard isInitialized else {
            fatalError("FlexUI not initialized. Call FlexUI.configure() first.")
        }
    }
    
    private func fetchConfigSync(screenId: String) -> String? {
        guard let cache = configCache, let fetcher = configFetcher else {
            return nil
        }
        
        // Try cache first
        if let cached = cache.get(screenId: screenId) {
            return cached.json
        }
        
        // Fetch from network
        let result = fetcher.fetchConfigSync(screenId: screenId)
        switch result {
        case .success(let json):
            cache.put(screenId: screenId, json: json)
            return json
        case .failure:
            return nil
        }
    }
    
    private func renderFromJson(
        _ jsonString: String,
        data: [String: Any]
    ) throws -> UIView? {
        let config = try FlexParser.parseConfig(jsonString)
        let theme = ThemeDefaults.merge(theme: config.theme ?? FlexTheme())
        return FlexRenderer.shared.render(node: config.root, theme: theme, data: data)
    }
}

// MARK: - FlexViewController

/// A simple view controller that hosts a FlexUI view
private class FlexViewController: UIViewController {
    private let loadingView = UIActivityIndicatorView(style: .large)
    private let errorLabel = UILabel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // Setup loading view
        loadingView.translatesAutoresizingMaskIntoConstraints = false
        loadingView.hidesWhenStopped = true
        view.addSubview(loadingView)
        NSLayoutConstraint.activate([
            loadingView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            loadingView.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
        
        // Setup error label
        errorLabel.translatesAutoresizingMaskIntoConstraints = false
        errorLabel.textColor = .systemRed
        errorLabel.textAlignment = .center
        errorLabel.numberOfLines = 0
        errorLabel.isHidden = true
        view.addSubview(errorLabel)
        NSLayoutConstraint.activate([
            errorLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            errorLabel.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            errorLabel.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 20),
            errorLabel.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -20)
        ])
        
        loadingView.startAnimating()
    }
    
    func setFlexView(_ flexView: UIView) {
        loadingView.stopAnimating()
        errorLabel.isHidden = true
        
        flexView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(flexView)
        NSLayoutConstraint.activate([
            flexView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            flexView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            flexView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            flexView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
    }
    
    func setError(_ error: FlexError) {
        loadingView.stopAnimating()
        errorLabel.isHidden = false
        errorLabel.text = "Error: \\(error.localizedDescription)"
    }
}

// MARK: - Type Aliases

/// Factory function for creating custom components
public typealias FlexComponentFactory = (FlexProps, FlexTheme) -> UIView

/// Handler function for FlexUI actions
public typealias FlexActionHandler = (FlexAction) -> Void