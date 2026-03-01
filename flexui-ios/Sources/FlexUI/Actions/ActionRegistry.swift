import Foundation

/// Registry for managing FlexUI action handlers
public final class ActionRegistry {
    
    // MARK: - Singleton
    
    public static let shared = ActionRegistry()
    
    // MARK: - Properties
    
    private var actionHandlers: [String: FlexActionHandler] = [:]
    private var eventHandlers: [String: FlexActionHandler] = [:]
    private let queue = DispatchQueue(label: "flexui.action_registry", attributes: .concurrent)
    
    // MARK: - Initialization
    
    private init() {
        setupBuiltInHandlers()
    }
    
    // MARK: - Action Handler Registration
    
    /// Register a handler for a specific action type
    public func register(actionType: String, handler: @escaping FlexActionHandler) {
        queue.async(flags: .barrier) { [weak self] in
            self?.actionHandlers[actionType] = handler
        }
    }
    
    /// Unregister a handler for an action type
    public func unregister(actionType: String) {
        queue.async(flags: .barrier) { [weak self] in
            self?.actionHandlers.removeValue(forKey: actionType)
        }
    }
    
    /// Get a handler for an action type
    public func getHandler(for actionType: String) -> FlexActionHandler? {
        return queue.sync {
            return actionHandlers[actionType]
        }
    }
    
    /// Check if a handler is registered for an action type
    public func hasHandler(for actionType: String) -> Bool {
        return queue.sync {
            return actionHandlers[actionType] != nil
        }
    }
    
    /// Get all registered action types
    public func getRegisteredActionTypes() -> Set<String> {
        return queue.sync {
            return Set(actionHandlers.keys)
        }
    }
    
    // MARK: - Event Handler Registration
    
    /// Register a handler for callback events
    public func registerEvent(eventName: String, handler: @escaping FlexActionHandler) {
        queue.async(flags: .barrier) { [weak self] in
            self?.eventHandlers[eventName] = handler
        }
    }
    
    /// Unregister an event handler
    public func unregisterEvent(eventName: String) {
        queue.async(flags: .barrier) { [weak self] in
            self?.eventHandlers.removeValue(forKey: eventName)
        }
    }
    
    /// Get a handler for an event
    public func getEventHandler(for eventName: String) -> FlexActionHandler? {
        return queue.sync {
            return eventHandlers[eventName]
        }
    }
    
    /// Check if an event handler is registered
    public func hasEventHandler(for eventName: String) -> Bool {
        return queue.sync {
            return eventHandlers[eventName] != nil
        }
    }
    
    /// Get all registered event names
    public func getRegisteredEventNames() -> Set<String> {
        return queue.sync {
            return Set(eventHandlers.keys)
        }
    }
    
    // MARK: - Bulk Operations
    
    /// Register multiple action handlers at once
    public func registerMultiple(_ handlers: [String: FlexActionHandler]) {
        queue.async(flags: .barrier) { [weak self] in
            for (actionType, handler) in handlers {
                self?.actionHandlers[actionType] = handler
            }
        }
    }
    
    /// Register multiple event handlers at once
    public func registerMultipleEvents(_ handlers: [String: FlexActionHandler]) {
        queue.async(flags: .barrier) { [weak self] in
            for (eventName, handler) in handlers {
                self?.eventHandlers[eventName] = handler
            }
        }
    }
    
    /// Clear all registered handlers
    public func clearAll() {
        queue.async(flags: .barrier) { [weak self] in
            self?.actionHandlers.removeAll()
            self?.eventHandlers.removeAll()
            // Re-setup built-in handlers
            self?.setupBuiltInHandlers()
        }
    }
    
    /// Clear all custom handlers (keep built-in handlers)
    public func clearCustomHandlers() {
        queue.async(flags: .barrier) { [weak self] in
            let builtInTypes = Set(["navigate", "callback", "openUrl", "dismiss", "showAlert", "presentModal", "share", "updateState", "animate"])
            
            // Remove action handlers that are not built-in
            self?.actionHandlers = self?.actionHandlers.filter { key, _ in
                builtInTypes.contains(key)
            } ?? [:]
            
            // Clear all event handlers (they are all custom)
            self?.eventHandlers.removeAll()
        }
    }
    
    // MARK: - Debug and Inspection
    
    /// Get detailed information about registered handlers
    public func getRegistryInfo() -> ActionRegistryInfo {
        return queue.sync {
            return ActionRegistryInfo(
                actionTypes: Set(actionHandlers.keys),
                eventNames: Set(eventHandlers.keys),
                totalActionHandlers: actionHandlers.count,
                totalEventHandlers: eventHandlers.count
            )
        }
    }
    
    /// Print registry information for debugging
    public func debugPrint() {
        let info = getRegistryInfo()
        
        print("FlexUI Action Registry Info")
        print("==========================")
        print("Action Handlers (\\(info.totalActionHandlers)):")
        for actionType in info.actionTypes.sorted() {
            print("  - \\(actionType)")
        }
        
        print("\\nEvent Handlers (\\(info.totalEventHandlers)):")
        for eventName in info.eventNames.sorted() {
            print("  - \\(eventName)")
        }
    }
    
    // MARK: - Built-in Handler Setup
    
    private func setupBuiltInHandlers() {
        // Built-in handlers are implemented in ActionDispatcher
        // This method is reserved for any registry-level built-in functionality
    }
    
    // MARK: - Action Handler Utilities
    
    /// Create a chained handler that calls multiple handlers in sequence
    public func chainHandlers(_ handlers: [FlexActionHandler]) -> FlexActionHandler {
        return { action in
            for handler in handlers {
                handler(action)
            }
        }
    }
    
    /// Create a conditional handler that only executes based on action data
    public func conditionalHandler(
        condition: @escaping (FlexAction) -> Bool,
        handler: @escaping FlexActionHandler
    ) -> FlexActionHandler {
        return { action in
            if condition(action) {
                handler(action)
            }
        }
    }
    
    /// Create a throttled handler that limits execution frequency
    public func throttledHandler(
        interval: TimeInterval,
        handler: @escaping FlexActionHandler
    ) -> FlexActionHandler {
        var lastExecutionTime: TimeInterval = 0
        
        return { action in
            let currentTime = CFAbsoluteTimeGetCurrent()
            if currentTime - lastExecutionTime >= interval {
                lastExecutionTime = currentTime
                handler(action)
            }
        }
    }
    
    /// Create a debounced handler that delays execution until calls stop
    public func debouncedHandler(
        delay: TimeInterval,
        handler: @escaping FlexActionHandler
    ) -> FlexActionHandler {
        var debounceWorkItem: DispatchWorkItem?
        
        return { action in
            debounceWorkItem?.cancel()
            debounceWorkItem = DispatchWorkItem {
                handler(action)
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + delay, execute: debounceWorkItem!)
        }
    }
    
    /// Create a retry handler that retries failed actions
    public func retryHandler(
        maxRetries: Int = 3,
        delay: TimeInterval = 1.0,
        handler: @escaping (FlexAction) throws -> Void
    ) -> FlexActionHandler {
        return { action in
            func attemptExecution(attempt: Int) {
                do {
                    try handler(action)
                } catch {
                    if attempt < maxRetries {
                        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                            attemptExecution(attempt: attempt + 1)
                        }
                    } else {
                        print("FlexUI: Action failed after \\(maxRetries) retries: \\(error)")
                    }
                }
            }
            
            attemptExecution(attempt: 1)
        }
    }
    
    /// Create a logging handler wrapper
    public func loggingHandler(
        prefix: String = "FlexUI Action",
        handler: @escaping FlexActionHandler
    ) -> FlexActionHandler {
        return { action in
            print("\\(prefix): \\(action.type) with data: \\(action.getAllData())")
            handler(action)
        }
    }
}

// MARK: - Registry Info

/// Information about the current state of the action registry
public struct ActionRegistryInfo {
    public let actionTypes: Set<String>
    public let eventNames: Set<String>
    public let totalActionHandlers: Int
    public let totalEventHandlers: Int
    
    /// Check if all built-in action types are registered
    public var hasAllBuiltInActions: Bool {
        let builtInTypes = Set(["navigate", "callback", "openUrl", "dismiss", "showAlert", "presentModal", "share", "updateState", "animate"])
        return builtInTypes.isSubset(of: actionTypes)
    }
    
    /// Get custom (non-built-in) action types
    public var customActionTypes: Set<String> {
        let builtInTypes = Set(["navigate", "callback", "openUrl", "dismiss", "showAlert", "presentModal", "share", "updateState", "animate"])
        return actionTypes.subtracting(builtInTypes)
    }
}

// MARK: - Handler Builder

/// Builder class for creating complex action handlers
public final class ActionHandlerBuilder {
    
    private var baseHandler: FlexActionHandler?
    private var conditions: [(FlexAction) -> Bool] = []
    private var middleware: [FlexActionHandler] = []
    private var errorHandler: ((Error, FlexAction) -> Void)?
    
    public init() {}
    
    /// Set the base handler
    public func handler(_ handler: @escaping FlexActionHandler) -> ActionHandlerBuilder {
        self.baseHandler = handler
        return self
    }
    
    /// Add a condition that must be met for the handler to execute
    public func condition(_ condition: @escaping (FlexAction) -> Bool) -> ActionHandlerBuilder {
        conditions.append(condition)
        return self
    }
    
    /// Add middleware that executes before the main handler
    public func middleware(_ handler: @escaping FlexActionHandler) -> ActionHandlerBuilder {
        middleware.append(handler)
        return self
    }
    
    /// Set error handling
    public func onError(_ handler: @escaping (Error, FlexAction) -> Void) -> ActionHandlerBuilder {
        self.errorHandler = handler
        return self
    }
    
    /// Build the final handler
    public func build() -> FlexActionHandler {
        guard let baseHandler = baseHandler else {
            fatalError("ActionHandlerBuilder: Base handler is required")
        }
        
        return { action in
            // Check all conditions
            for condition in self.conditions {
                if !condition(action) {
                    return // Condition not met, skip execution
                }
            }
            
            do {
                // Execute middleware
                for middlewareHandler in self.middleware {
                    middlewareHandler(action)
                }
                
                // Execute main handler
                baseHandler(action)
                
            } catch {
                // Handle errors
                if let errorHandler = self.errorHandler {
                    errorHandler(error, action)
                } else {
                    print("FlexUI Action Error: \\(error.localizedDescription)")
                }
            }
        }
    }
}

// MARK: - Convenience Extensions

public extension ActionRegistry {
    
    /// Register a simple callback action handler
    func onCallback(_ eventName: String, handler: @escaping ([String: Any]) -> Void) {
        registerEvent(eventName: eventName) { action in
            handler(action.getAllData())
        }
    }
    
    /// Register a navigation handler
    func onNavigate(handler: @escaping (String, [String: Any]) -> Void) {
        register(actionType: "navigate") { action in
            guard let screen = action.getString("screen") else { return }
            let data = action.getDictionary("data") ?? [:]
            handler(screen, data)
        }
    }
    
    /// Register a URL opening handler
    func onOpenUrl(handler: @escaping (String, Bool) -> Void) {
        register(actionType: "openUrl") { action in
            guard let url = action.getString("url") else { return }
            let external = action.getBool("external") ?? true
            handler(url, external)
        }
    }
    
    /// Create an action handler builder
    func builder() -> ActionHandlerBuilder {
        return ActionHandlerBuilder()
    }
}