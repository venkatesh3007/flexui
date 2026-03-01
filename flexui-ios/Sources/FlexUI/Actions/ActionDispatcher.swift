import UIKit
import SafariServices

/// Handles dispatching and execution of FlexUI actions
public final class ActionDispatcher {
    
    // MARK: - Singleton
    
    public static let shared = ActionDispatcher()
    
    // MARK: - Properties
    
    private let registry = ActionRegistry.shared
    private let mainQueue = DispatchQueue.main
    
    // MARK: - Initialization
    
    private init() {
        setupDefaultHandlers()
    }
    
    // MARK: - Public Methods
    
    /// Register a handler for a specific action type
    public func registerHandler(type: String, handler: @escaping FlexActionHandler) {
        registry.register(actionType: type, handler: handler)
    }
    
    /// Register a handler for callback events
    public func registerEventHandler(eventName: String, handler: @escaping FlexActionHandler) {
        registry.registerEvent(eventName: eventName, handler: handler)
    }
    
    /// Dispatch an action for execution
    public func dispatch(_ action: FlexAction, from view: UIView? = nil) {
        mainQueue.async { [weak self] in
            self?.executeAction(action, from: view)
        }
    }
    
    /// Dispatch an action with resolved data
    public func dispatch(_ action: FlexAction, with data: [String: Any], theme: FlexTheme, from view: UIView? = nil) {
        let resolvedAction = action.resolved(with: data, theme: theme)
        dispatch(resolvedAction, from: view)
    }
    
    // MARK: - Private Methods
    
    /// Execute an action
    private func executeAction(_ action: FlexAction, from view: UIView?) {
        do {
            // Validate action first
            try action.validate()
            
            // Check for custom handlers first
            if let handler = registry.getHandler(for: action.type) {
                handler(action)
                return
            }
            
            // Handle built-in actions
            try executeBuiltInAction(action, from: view)
            
        } catch {
            handleActionError(error, action: action)
        }
    }
    
    /// Execute built-in actions
    private func executeBuiltInAction(_ action: FlexAction, from view: UIView?) throws {
        switch action.type {
        case "navigate":
            try handleNavigateAction(action, from: view)
            
        case "callback":
            try handleCallbackAction(action)
            
        case "openUrl":
            try handleOpenUrlAction(action, from: view)
            
        case "dismiss":
            try handleDismissAction(action, from: view)
            
        case "showAlert":
            try handleShowAlertAction(action, from: view)
            
        case "presentModal":
            try handlePresentModalAction(action, from: view)
            
        case "share":
            try handleShareAction(action, from: view)
            
        case "updateState":
            try handleUpdateStateAction(action, from: view)
            
        case "animate":
            try handleAnimateAction(action, from: view)
            
        default:
            throw FlexActionError.unsupportedAction(action.type)
        }
    }
    
    // MARK: - Action Handlers
    
    /// Handle navigation actions
    private func handleNavigateAction(_ action: FlexAction, from view: UIView?) throws {
        guard let screenId = action.getString("screen") else {
            throw FlexActionError.missingData("screen parameter required for navigate action")
        }
        
        let data = action.getDictionary("data") ?? [:]
        
        // Create new FlexUI view
        guard let flexView = FlexUI.render(screenId: screenId, data: data) else {
            throw FlexActionError.executionFailed("Failed to render screen: \\(screenId)", error: nil)
        }
        
        // Find the presenting view controller
        guard let presentingViewController = findViewController(from: view) else {
            throw FlexActionError.executionFailed("No view controller found to present navigation", error: nil)
        }
        
        // Create view controller with FlexUI view
        let destinationVC = UIViewController()
        destinationVC.view.addSubview(flexView)
        flexView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            flexView.topAnchor.constraint(equalTo: destinationVC.view.safeAreaLayoutGuide.topAnchor),
            flexView.leadingAnchor.constraint(equalTo: destinationVC.view.leadingAnchor),
            flexView.trailingAnchor.constraint(equalTo: destinationVC.view.trailingAnchor),
            flexView.bottomAnchor.constraint(equalTo: destinationVC.view.bottomAnchor)
        ])
        
        // Present or push based on navigation style
        let style = action.getString("style") ?? "push"
        let animated = action.getBool("animated") ?? true
        
        if style == "present" || presentingViewController.navigationController == nil {
            let navController = UINavigationController(rootViewController: destinationVC)
            presentingViewController.present(navController, animated: animated)
        } else {
            presentingViewController.navigationController?.pushViewController(destinationVC, animated: animated)
        }
    }
    
    /// Handle callback actions
    private func handleCallbackAction(_ action: FlexAction) throws {
        guard let eventName = action.getString("event") else {
            throw FlexActionError.missingData("event parameter required for callback action")
        }
        
        // Check for registered event handler
        if let handler = registry.getEventHandler(for: eventName) {
            handler(action)
        } else {
            print("FlexUI: No handler registered for event '\\(eventName)'")
        }
    }
    
    /// Handle URL opening actions
    private func handleOpenUrlAction(_ action: FlexAction, from view: UIView?) throws {
        guard let urlString = action.getString("url"),
              let url = URL(string: urlString) else {
            throw FlexActionError.invalidData("Invalid URL for openUrl action")
        }
        
        let external = action.getBool("external") ?? true
        
        if external {
            // Open in external browser
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url, options: [:])
            } else {
                throw FlexActionError.executionFailed("Cannot open URL: \\(urlString)", error: nil)
            }
        } else {
            // Open in in-app browser
            guard let presentingViewController = findViewController(from: view) else {
                throw FlexActionError.executionFailed("No view controller found to present browser", error: nil)
            }
            
            let safariVC = SFSafariViewController(url: url)
            presentingViewController.present(safariVC, animated: true)
        }
    }
    
    /// Handle dismiss actions
    private func handleDismissAction(_ action: FlexAction, from view: UIView?) throws {
        guard let viewController = findViewController(from: view) else {
            throw FlexActionError.executionFailed("No view controller found to dismiss", error: nil)
        }
        
        let animated = action.getBool("animated") ?? true
        
        if let presentingViewController = viewController.presentingViewController {
            presentingViewController.dismiss(animated: animated)
        } else if let navigationController = viewController.navigationController {
            navigationController.popViewController(animated: animated)
        } else {
            throw FlexActionError.executionFailed("View controller cannot be dismissed", error: nil)
        }
    }
    
    /// Handle show alert actions
    private func handleShowAlertAction(_ action: FlexAction, from view: UIView?) throws {
        guard let title = action.getString("title") else {
            throw FlexActionError.missingData("title parameter required for showAlert action")
        }
        
        let message = action.getString("message") ?? ""
        let buttons = action.getStringArray("buttons") ?? ["OK"]
        
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        
        for buttonTitle in buttons {
            let alertAction = UIAlertAction(title: buttonTitle, style: .default) { _ in
                // Create callback action for button tap
                let callbackAction = FlexAction.callback(event: "alert_button_tapped", data: [
                    "button": buttonTitle,
                    "title": title
                ])
                self.dispatch(callbackAction)
            }
            alertController.addAction(alertAction)
        }
        
        guard let presentingViewController = findViewController(from: view) else {
            throw FlexActionError.executionFailed("No view controller found to present alert", error: nil)
        }
        
        presentingViewController.present(alertController, animated: true)
    }
    
    /// Handle present modal actions
    private func handlePresentModalAction(_ action: FlexAction, from view: UIView?) throws {
        guard let screenId = action.getString("screenId") else {
            throw FlexActionError.missingData("screenId parameter required for presentModal action")
        }
        
        let data = action.getDictionary("data") ?? [:]
        let dismissible = action.getBool("dismissible") ?? true
        
        // Create modal view controller
        let modalViewController = FlexUI.viewController(screenId: screenId, data: data)
        
        // Add dismiss button if dismissible
        if dismissible {
            let closeButton = UIBarButtonItem(
                barButtonSystemItem: .close,
                target: self,
                action: #selector(dismissModal(_:))
            )
            modalViewController.navigationItem.leftBarButtonItem = closeButton
        }
        
        // Wrap in navigation controller
        let navigationController = UINavigationController(rootViewController: modalViewController)
        navigationController.modalPresentationStyle = .pageSheet
        
        // Present modal
        guard let presentingViewController = findViewController(from: view) else {
            throw FlexActionError.executionFailed("No view controller found to present modal", error: nil)
        }
        
        presentingViewController.present(navigationController, animated: true)
    }
    
    /// Handle share actions
    private func handleShareAction(_ action: FlexAction, from view: UIView?) throws {
        var shareItems: [Any] = []
        
        if let text = action.getString("text") {
            shareItems.append(text)
        }
        
        if let urlString = action.getString("url"), let url = URL(string: urlString) {
            shareItems.append(url)
        }
        
        if let imagePath = action.getString("image") {
            // Load image from path/URL
            // This is a simplified implementation
            shareItems.append(imagePath)
        }
        
        guard !shareItems.isEmpty else {
            throw FlexActionError.missingData("No content provided for share action")
        }
        
        let activityViewController = UIActivityViewController(
            activityItems: shareItems,
            applicationActivities: nil
        )
        
        guard let presentingViewController = findViewController(from: view) else {
            throw FlexActionError.executionFailed("No view controller found to present share sheet", error: nil)
        }
        
        // For iPad, set up popover
        if let popover = activityViewController.popoverPresentationController {
            if let sourceView = view {
                popover.sourceView = sourceView
                popover.sourceRect = sourceView.bounds
            } else {
                popover.sourceView = presentingViewController.view
                popover.sourceRect = CGRect(x: presentingViewController.view.bounds.midX,
                                          y: presentingViewController.view.bounds.midY,
                                          width: 0, height: 0)
                popover.permittedArrowDirections = []
            }
        }
        
        presentingViewController.present(activityViewController, animated: true)
    }
    
    /// Handle update state actions
    private func handleUpdateStateAction(_ action: FlexAction, from view: UIView?) throws {
        guard let componentId = action.getString("componentId") else {
            throw FlexActionError.missingData("componentId parameter required for updateState action")
        }
        
        guard let state = action.getDictionary("state") else {
            throw FlexActionError.missingData("state parameter required for updateState action")
        }
        
        // Find the component view by ID and update its state
        // This would require a component registry or view tagging system
        // For now, just trigger a callback
        let callbackAction = FlexAction.callback(event: "state_updated", data: [
            "componentId": componentId,
            "state": state
        ])
        dispatch(callbackAction)
    }
    
    /// Handle animate actions
    private func handleAnimateAction(_ action: FlexAction, from view: UIView?) throws {
        guard let componentId = action.getString("componentId") else {
            throw FlexActionError.missingData("componentId parameter required for animate action")
        }
        
        let animation = action.getString("animation") ?? "fadeIn"
        let duration = TimeInterval(action.getFloat("duration") ?? 0.3)
        
        // Find the target view (simplified - would need proper component lookup)
        guard let targetView = view else {
            throw FlexActionError.executionFailed("No target view found for animation", error: nil)
        }
        
        // Execute animation based on type
        switch animation {
        case "fadeIn":
            targetView.alpha = 0
            UIView.animate(withDuration: duration) {
                targetView.alpha = 1
            }
            
        case "fadeOut":
            UIView.animate(withDuration: duration) {
                targetView.alpha = 0
            }
            
        case "slideInLeft":
            let originalTransform = targetView.transform
            targetView.transform = CGAffineTransform(translationX: -targetView.bounds.width, y: 0)
            UIView.animate(withDuration: duration) {
                targetView.transform = originalTransform
            }
            
        case "slideInRight":
            let originalTransform = targetView.transform
            targetView.transform = CGAffineTransform(translationX: targetView.bounds.width, y: 0)
            UIView.animate(withDuration: duration) {
                targetView.transform = originalTransform
            }
            
        case "bounce":
            UIView.animate(withDuration: duration, delay: 0, usingSpringWithDamping: 0.5, initialSpringVelocity: 0.5, options: []) {
                targetView.transform = CGAffineTransform(scaleX: 1.1, y: 1.1)
            } completion: { _ in
                UIView.animate(withDuration: duration) {
                    targetView.transform = .identity
                }
            }
            
        default:
            throw FlexActionError.unsupportedAction("Unsupported animation: \\(animation)")
        }
    }
    
    // MARK: - Helper Methods
    
    /// Find the view controller that contains a given view
    private func findViewController(from view: UIView?) -> UIViewController? {
        guard let view = view else {
            // Return the top-most view controller
            return UIApplication.shared.windows.first?.rootViewController?.topMostViewController
        }
        
        var responder = view.next
        while responder != nil {
            if let viewController = responder as? UIViewController {
                return viewController
            }
            responder = responder?.next
        }
        
        return nil
    }
    
    /// Handle action execution errors
    private func handleActionError(_ error: Error, action: FlexAction) {
        print("FlexUI Action Error: \\(error.localizedDescription)")
        print("Action: \\(action.type) with data: \\(action.getAllData())")
        
        // Optionally show error to user in debug mode
        if FlexUI.getDefaultTheme().getFlag("debug") == true {
            DispatchQueue.main.async {
                let alert = UIAlertController(
                    title: "Action Error",
                    message: error.localizedDescription,
                    preferredStyle: .alert
                )
                alert.addAction(UIAlertAction(title: "OK", style: .default))
                
                if let topViewController = UIApplication.shared.windows.first?.rootViewController?.topMostViewController {
                    topViewController.present(alert, animated: true)
                }
            }
        }
    }
    
    /// Setup default action handlers
    private func setupDefaultHandlers() {
        // Default handlers are implemented as built-in actions above
        // Custom handlers can be registered via registerHandler()
    }
    
    // MARK: - Selectors
    
    @objc private func dismissModal(_ sender: UIBarButtonItem) {
        if let viewController = findViewController(from: nil) {
            viewController.dismiss(animated: true)
        }
    }
}

// MARK: - UIViewController Extension

private extension UIViewController {
    
    /// Get the top-most presented view controller
    var topMostViewController: UIViewController {
        if let presented = presentedViewController {
            return presented.topMostViewController
        }
        
        if let navigationController = self as? UINavigationController {
            return navigationController.topViewController?.topMostViewController ?? self
        }
        
        if let tabBarController = self as? UITabBarController {
            return tabBarController.selectedViewController?.topMostViewController ?? self
        }
        
        return self
    }
}