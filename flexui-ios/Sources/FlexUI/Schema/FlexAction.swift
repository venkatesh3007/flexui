import Foundation

/// Represents an action that can be triggered by FlexUI components
public struct FlexAction: Codable, Equatable {
    
    // MARK: - Properties
    
    /// The type of action (e.g., "navigate", "callback", "openUrl")
    public let type: String
    
    /// Additional data for the action
    public let data: [String: AnyCodable]?
    
    // MARK: - Initialization
    
    public init(type: String, data: [String: Any]? = nil) {
        self.type = type
        self.data = data?.mapValues { AnyCodable($0) }
    }
    
    // MARK: - Convenience Getters
    
    /// Get string value from action data
    public func getString(_ key: String) -> String? {
        return data?[key]?.value as? String
    }
    
    /// Get integer value from action data
    public func getInt(_ key: String) -> Int? {
        return data?[key]?.value as? Int
    }
    
    /// Get boolean value from action data
    public func getBool(_ key: String) -> Bool? {
        return data?[key]?.value as? Bool
    }
    
    /// Get float value from action data
    public func getFloat(_ key: String) -> Float? {
        if let value = data?[key]?.value as? Float {
            return value
        }
        if let value = data?[key]?.value as? Double {
            return Float(value)
        }
        if let value = data?[key]?.value as? Int {
            return Float(value)
        }
        return nil
    }
    
    /// Get dictionary value from action data
    public func getDictionary(_ key: String) -> [String: Any]? {
        return data?[key]?.value as? [String: Any]
    }
    
    /// Get array value from action data
    public func getArray(_ key: String) -> [Any]? {
        return data?[key]?.value as? [Any]
    }
    
    /// Get all action data as a dictionary
    public func getAllData() -> [String: Any] {
        return data?.mapValues { $0.value } ?? [:]
    }
    
    // MARK: - Action Type Checks
    
    /// Check if this is a navigation action
    public var isNavigate: Bool {
        return type == "navigate"
    }
    
    /// Check if this is a callback action
    public var isCallback: Bool {
        return type == "callback"
    }
    
    /// Check if this is a URL opening action
    public var isOpenUrl: Bool {
        return type == "openUrl"
    }
    
    /// Check if this is a dismiss action
    public var isDismiss: Bool {
        return type == "dismiss"
    }
    
    /// Check if this is a custom action
    public var isCustom: Bool {
        return !["navigate", "callback", "openUrl", "dismiss"].contains(type)
    }
}

// MARK: - Factory Methods

public extension FlexAction {
    
    /// Create a navigation action
    static func navigate(to screen: String, data: [String: Any]? = nil) -> FlexAction {
        var actionData: [String: Any] = ["screen": screen]
        if let additionalData = data {
            actionData.merge(additionalData) { _, new in new }
        }
        return FlexAction(type: "navigate", data: actionData)
    }
    
    /// Create a callback action
    static func callback(event: String, data: [String: Any]? = nil) -> FlexAction {
        var actionData: [String: Any] = ["event": event]
        if let additionalData = data {
            actionData.merge(additionalData) { _, new in new }
        }
        return FlexAction(type: "callback", data: actionData)
    }
    
    /// Create a URL opening action
    static func openUrl(_ url: String, external: Bool = true) -> FlexAction {
        return FlexAction(type: "openUrl", data: [
            "url": url,
            "external": external
        ])
    }
    
    /// Create a dismiss action
    static func dismiss(animated: Bool = true) -> FlexAction {
        return FlexAction(type: "dismiss", data: [
            "animated": animated
        ])
    }
    
    /// Create a custom action
    static func custom(type: String, data: [String: Any]? = nil) -> FlexAction {
        return FlexAction(type: type, data: data)
    }
    
    /// Create an action that updates component state
    static func updateState(componentId: String, state: [String: Any]) -> FlexAction {
        return FlexAction(type: "updateState", data: [
            "componentId": componentId,
            "state": state
        ])
    }
    
    /// Create an action that triggers an animation
    static func animate(componentId: String, animation: String, duration: Float = 0.3) -> FlexAction {
        return FlexAction(type: "animate", data: [
            "componentId": componentId,
            "animation": animation,
            "duration": duration
        ])
    }
    
    /// Create an action that shows an alert
    static func showAlert(title: String, message: String, buttons: [String] = ["OK"]) -> FlexAction {
        return FlexAction(type: "showAlert", data: [
            "title": title,
            "message": message,
            "buttons": buttons
        ])
    }
    
    /// Create an action that presents a modal
    static func presentModal(screenId: String, data: [String: Any]? = nil, dismissible: Bool = true) -> FlexAction {
        var actionData: [String: Any] = [
            "screenId": screenId,
            "dismissible": dismissible
        ]
        if let additionalData = data {
            actionData.merge(additionalData) { _, new in new }
        }
        return FlexAction(type: "presentModal", data: actionData)
    }
    
    /// Create an action that shares content
    static func share(text: String? = nil, url: String? = nil, image: String? = nil) -> FlexAction {
        var data: [String: Any] = [:]
        if let text = text { data["text"] = text }
        if let url = url { data["url"] = url }
        if let image = image { data["image"] = image }
        return FlexAction(type: "share", data: data)
    }
}

// MARK: - Action Resolution

public extension FlexAction {
    
    /// Resolve action data with theme variables and runtime data
    func resolved(with data: [String: Any], theme: FlexTheme) -> FlexAction {
        let resolver = ThemeResolver(theme: theme)
        let resolvedData = self.data?.mapValues { anyCodable in
            if let stringValue = anyCodable.value as? String {
                return AnyCodable(resolver.replaceVariables(stringValue, with: data))
            }
            return anyCodable
        }
        
        return FlexAction(
            type: type,
            data: resolvedData?.mapValues { $0.value }
        )
    }
}

// MARK: - Error Handling

/// Errors that can occur during action execution
public enum FlexActionError: Error, LocalizedError {
    case missingData(String)
    case invalidData(String)
    case unsupportedAction(String)
    case executionFailed(String, Error?)
    
    public var errorDescription: String? {
        switch self {
        case .missingData(let message):
            return "Missing action data: \(message)"
        case .invalidData(let message):
            return "Invalid action data: \(message)"
        case .unsupportedAction(let action):
            return "Unsupported action type: \(action)"
        case .executionFailed(let message, let error):
            if let error = error {
                return "Action execution failed: \(message) - \(error.localizedDescription)"
            } else {
                return "Action execution failed: \(message)"
            }
        }
    }
}

// MARK: - Action Validation

public extension FlexAction {
    
    /// Validate that this action has all required data for its type
    func validate() throws {
        switch type {
        case "navigate":
            guard getString("screen") != nil else {
                throw FlexActionError.missingData("navigate action requires 'screen' parameter")
            }
            
        case "openUrl":
            guard let url = getString("url"), !url.isEmpty else {
                throw FlexActionError.missingData("openUrl action requires 'url' parameter")
            }
            
            guard URL(string: url) != nil else {
                throw FlexActionError.invalidData("openUrl action has invalid URL: \(url)")
            }
            
        case "callback":
            guard getString("event") != nil else {
                throw FlexActionError.missingData("callback action requires 'event' parameter")
            }
            
        case "updateState":
            guard getString("componentId") != nil else {
                throw FlexActionError.missingData("updateState action requires 'componentId' parameter")
            }
            
            guard getDictionary("state") != nil else {
                throw FlexActionError.missingData("updateState action requires 'state' parameter")
            }
            
        case "presentModal":
            guard getString("screenId") != nil else {
                throw FlexActionError.missingData("presentModal action requires 'screenId' parameter")
            }
            
        case "share":
            let hasText = getString("text") != nil
            let hasUrl = getString("url") != nil
            let hasImage = getString("image") != nil
            
            guard hasText || hasUrl || hasImage else {
                throw FlexActionError.missingData("share action requires at least one of: text, url, or image")
            }
            
        case "dismiss", "showAlert":
            // These actions don't have strict requirements
            break
            
        default:
            // Custom actions - no validation for now
            break
        }
    }
}