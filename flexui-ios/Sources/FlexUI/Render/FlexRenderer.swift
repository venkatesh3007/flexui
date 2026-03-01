import UIKit
import Foundation

/// Core rendering engine for FlexUI components
public final class FlexRenderer {
    
    // MARK: - Singleton
    
    public static let shared = FlexRenderer()
    
    // MARK: - Properties
    
    private let componentRegistry = ComponentRegistry.shared
    private let actionDispatcher = ActionDispatcher.shared
    
    // MARK: - Initialization
    
    private init() {}
    
    // MARK: - Public Methods
    
    /// Render a FlexNode into a UIView
    public func render(
        node: FlexNode,
        theme: FlexTheme,
        data: [String: Any] = [:]
    ) -> UIView? {
        // Check if node should be rendered
        guard node.shouldRender(data: data, theme: theme) else {
            return nil
        }
        
        // Get or create the component view
        guard let componentFactory = componentRegistry.resolve(type: node.type) else {
            print("FlexUI: Unknown component type: \\(node.type)")
            return nil
        }
        
        // Get props and resolve them
        var props = node.getProps().resolved(with: theme, data: data)
        
        // Create the component view
        let view = componentFactory(props, theme)
        
        // Apply styling
        applyStyle(to: view, node: node, theme: theme, data: data)
        
        // Set component ID if present
        if let id = node.id {
            view.tag = id.hashValue // Store ID for later reference
            view.accessibilityIdentifier = id
        }
        
        // Add action binding if present
        if let action = node.action {
            addActionBinding(to: view, action: action, data: data, theme: theme)
        }
        
        // Add children if this is a container
        if let children = node.children {
            addChildren(to: view, children: children, theme: theme, data: data)
        }
        
        return view
    }
    
    // MARK: - Private Methods
    
    /// Apply style properties to a view
    private func applyStyle(
        to view: UIView,
        node: FlexNode,
        theme: FlexTheme,
        data: [String: Any]
    ) {
        let style = node.resolvedStyle(theme: theme)
        
        // Set frame/layout properties
        if let width = style.width {
            view.widthAnchor.constraint(equalToConstant: width).isActive = true
        }
        if let height = style.height {
            view.heightAnchor.constraint(equalToConstant: height).isActive = true
        }
        
        // Set background color
        if let bgColor = style.backgroundColor {
            view.backgroundColor = bgColor
        }
        
        // Set border
        if style.borderWidth > 0 {
            view.layer.borderWidth = style.borderWidth
            if let borderColor = style.borderColor {
                view.layer.borderColor = borderColor.cgColor
            }
        }
        
        // Set corner radius
        if style.borderRadius > 0 {
            view.layer.cornerRadius = style.borderRadius
            view.clipsToBounds = true
        }
        
        // Set shadow
        if style.shadowRadius > 0 {
            view.layer.shadowColor = style.shadowColor?.cgColor ?? UIColor.black.cgColor
            view.layer.shadowOpacity = style.shadowOpacity
            view.layer.shadowOffset = style.shadowOffset
            view.layer.shadowRadius = style.shadowRadius
        }
        
        // Set opacity
        if style.opacity < 1.0 {
            view.alpha = CGFloat(style.opacity)
        }
        
        // Set padding (for container views with layout)
        if let stackView = view as? UIStackView {
            stackView.layoutMargins = UIEdgeInsets(
                top: style.paddingTop,
                left: style.paddingLeft,
                bottom: style.paddingBottom,
                right: style.paddingRight
            )
            stackView.isLayoutMarginsRelativeArrangement = true
        } else if let scrollView = view as? UIScrollView {
            // Apply padding to scroll view content
            scrollView.contentInset = UIEdgeInsets(
                top: style.paddingTop,
                left: style.paddingLeft,
                bottom: style.paddingBottom,
                right: style.paddingRight
            )
        } else {
            // Apply padding as layout margins for other views
            view.layoutMargins = UIEdgeInsets(
                top: style.paddingTop,
                left: style.paddingLeft,
                bottom: style.paddingBottom,
                right: style.paddingRight
            )
        }
        
        // Set margin through constraints if parent is set
        // Note: This would be better handled by the parent view
    }
    
    /// Add child views to a container
    private func addChildren(
        to view: UIView,
        children: [FlexNode],
        theme: FlexTheme,
        data: [String: Any]
    ) {
        // Handle UIStackView specially
        if let stackView = view as? UIStackView {
            for child in children {
                if let childView = render(node: child, theme: theme, data: data) {
                    stackView.addArrangedSubview(childView)
                }
            }
        } else if let scrollView = view as? UIScrollView {
            // Create a container view for scroll content
            let containerView = UIView()
            containerView.translatesAutoresizingMaskIntoConstraints = false
            scrollView.addSubview(containerView)
            
            // Constrain container to scroll view
            NSLayoutConstraint.activate([
                containerView.topAnchor.constraint(equalTo: scrollView.topAnchor),
                containerView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
                containerView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
                containerView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
                containerView.widthAnchor.constraint(equalTo: scrollView.widthAnchor)
            ])
            
            // Add children to container
            for child in children {
                if let childView = render(node: child, theme: theme, data: data) {
                    childView.translatesAutoresizingMaskIntoConstraints = false
                    containerView.addSubview(childView)
                }
            }
        } else {
            // Add as regular subviews
            for child in children {
                if let childView = render(node: child, theme: theme, data: data) {
                    childView.translatesAutoresizingMaskIntoConstraints = false
                    view.addSubview(childView)
                }
            }
        }
    }
    
    /// Add action binding to a view
    private func addActionBinding(
        to view: UIView,
        action: FlexAction,
        data: [String: Any],
        theme: FlexTheme
    ) {
        // Resolve action with current data
        let resolvedAction = action.resolved(with: data, theme: theme)
        
        if let button = view as? UIButton {
            // Add action to button
            button.addAction(
                UIAction { [weak self] _ in
                    self?.actionDispatcher.dispatch(resolvedAction, from: button)
                },
                for: .touchUpInside
            )
        } else if let gesture = view as? UIGestureRecognizer {
            // Handle gesture
            gesture.addTarget(self, action: #selector(handleGestureAction(_:)))
        } else {
            // Add tap gesture for other views
            let tapGesture = UITapGestureRecognizer()
            tapGesture.addTarget(self, action: #selector(handleTapGesture(_:)))
            view.addGestureRecognizer(tapGesture)
            
            // Store action in associated object for later retrieval
            objc_setAssociatedObject(view, &actionKey, resolvedAction, .OBJC_ASSOCIATION_RETAIN)
            objc_setAssociatedObject(view, &dispatcherKey, actionDispatcher, .OBJC_ASSOCIATION_RETAIN)
        }
    }
    
    // MARK: - Gesture Handlers
    
    @objc private func handleTapGesture(_ gesture: UITapGestureRecognizer) {
        guard let view = gesture.view else { return }
        
        if let action = objc_getAssociatedObject(view, &actionKey) as? FlexAction,
           let dispatcher = objc_getAssociatedObject(view, &dispatcherKey) as? ActionDispatcher {
            dispatcher.dispatch(action, from: view)
        }
    }
    
    @objc private func handleGestureAction(_ gesture: UIGestureRecognizer) {
        guard let view = gesture.view else { return }
        
        if let action = objc_getAssociatedObject(view, &actionKey) as? FlexAction,
           let dispatcher = objc_getAssociatedObject(view, &dispatcherKey) as? ActionDispatcher {
            dispatcher.dispatch(action, from: view)
        }
    }
}

// MARK: - Associated Object Keys

private var actionKey = "flexui_action"
private var dispatcherKey = "flexui_dispatcher"

// MARK: - Advanced Rendering

public extension FlexRenderer {
    
    /// Render with caching for performance
    func renderCached(
        node: FlexNode,
        theme: FlexTheme,
        data: [String: Any] = [:],
        cache: NSCache<NSString, UIView> = NSCache()
    ) -> UIView? {
        let cacheKey = "\(node.id ?? node.type)_\\(data.hashValue)" as NSString
        
        if let cached = cache.object(forKey: cacheKey) {
            return cached
        }
        
        guard let view = render(node: node, theme: theme, data: data) else {
            return nil
        }
        
        cache.setObject(view, forKey: cacheKey)
        return view
    }
    
    /// Batch render multiple nodes
    func renderBatch(
        nodes: [FlexNode],
        theme: FlexTheme,
        data: [String: Any] = [:]
    ) -> [UIView] {
        return nodes.compactMap { render(node: $0, theme: theme, data: data) }
    }
    
    /// Render with error handling
    func renderSafe(
        node: FlexNode,
        theme: FlexTheme,
        data: [String: Any] = [:],
        fallbackView: UIView? = nil
    ) -> UIView {
        do {
            if let view = render(node: node, theme: theme, data: data) {
                return view
            }
            
            if let fallback = fallbackView {
                return fallback
            }
            
            // Create error view
            let errorView = createErrorView(for: node)
            return errorView
            
        } catch {
            if let fallback = fallbackView {
                return fallback
            }
            
            return createErrorView(for: node)
        }
    }
    
    /// Create a view that displays an error message
    private func createErrorView(for node: FlexNode) -> UIView {
        let errorView = UIView()
        errorView.backgroundColor = UIColor(red: 1, green: 0.2, blue: 0.2, alpha: 0.1)
        errorView.layer.borderColor = UIColor.systemRed.cgColor
        errorView.layer.borderWidth = 1
        
        let label = UILabel()
        label.text = "Error rendering component: \\(node.type)"
        label.textColor = .systemRed
        label.font = UIFont.systemFont(ofSize: 12)
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        
        errorView.addSubview(label)
        NSLayoutConstraint.activate([
            label.topAnchor.constraint(equalTo: errorView.topAnchor, constant: 8),
            label.leadingAnchor.constraint(equalTo: errorView.leadingAnchor, constant: 8),
            label.trailingAnchor.constraint(equalTo: errorView.trailingAnchor, constant: -8),
            label.bottomAnchor.constraint(equalTo: errorView.bottomAnchor, constant: -8)
        ])
        
        return errorView
    }
}

// MARK: - Render Optimization

public extension FlexRenderer {
    
    /// Get rendering performance metrics
    func getPerformanceMetrics() -> RenderMetrics {
        return RenderMetrics(
            timestamp: Date(),
            componentsRendered: 0,
            renderTime: 0
        )
    }
}

/// Rendering performance metrics
public struct RenderMetrics {
    public let timestamp: Date
    public let componentsRendered: Int
    public let renderTime: TimeInterval
}

// MARK: - Configuration

public extension FlexRenderer {
    
    /// Configure rendering behavior
    struct Configuration {
        /// Maximum nesting depth
        var maxDepth: Int = 50
        
        /// Enable debug logging
        var debugLogging: Bool = false
        
        /// Enable rendering caching
        var enableCaching: Bool = true
        
        /// Maximum cache size
        var maxCacheSize: Int = 100
        
        /// Render timeout in seconds
        var renderTimeout: TimeInterval = 30
    }
    
    /// Set renderer configuration
    func configure(_ configuration: Configuration) {
        // Store configuration for use during rendering
        // This would typically be stored as a property
    }
}