package dev.flexui.actions

import dev.flexui.FlexActionHandler
import dev.flexui.schema.FlexAction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Registry for managing FlexUI action handlers with priority support
 */
class ActionRegistry {
    
    private val typeHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<HandlerEntry>>()
    private val eventHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<HandlerEntry>>()
    private val interceptors = CopyOnWriteArrayList<ActionInterceptor>()
    
    /**
     * Register a handler for a specific action type
     */
    fun registerTypeHandler(
        actionType: String, 
        handler: FlexActionHandler,
        priority: Int = PRIORITY_NORMAL
    ) {
        val entry = HandlerEntry(handler, priority)
        typeHandlers.getOrPut(actionType) { CopyOnWriteArrayList() }
            .add(entry)
            .also { sortByPriority(typeHandlers[actionType]!!) }
    }
    
    /**
     * Register a handler for a specific event name (callback actions)
     */
    fun registerEventHandler(
        eventName: String,
        handler: FlexActionHandler,
        priority: Int = PRIORITY_NORMAL
    ) {
        val entry = HandlerEntry(handler, priority)
        eventHandlers.getOrPut(eventName) { CopyOnWriteArrayList() }
            .add(entry)
            .also { sortByPriority(eventHandlers[eventName]!!) }
    }
    
    /**
     * Register an action interceptor that can modify or block actions
     */
    fun registerInterceptor(interceptor: ActionInterceptor, priority: Int = PRIORITY_NORMAL) {
        val entry = InterceptorEntry(interceptor, priority)
        interceptors.add(entry)
        interceptors.sortWith { a, b -> b.priority.compareTo(a.priority) }
    }
    
    /**
     * Unregister a type handler
     */
    fun unregisterTypeHandler(actionType: String, handler: FlexActionHandler) {
        typeHandlers[actionType]?.removeAll { it.handler == handler }
    }
    
    /**
     * Unregister an event handler
     */
    fun unregisterEventHandler(eventName: String, handler: FlexActionHandler) {
        eventHandlers[eventName]?.removeAll { it.handler == handler }
    }
    
    /**
     * Unregister an interceptor
     */
    fun unregisterInterceptor(interceptor: ActionInterceptor) {
        interceptors.removeAll { it.interceptor == interceptor }
    }
    
    /**
     * Execute an action through the registry
     */
    fun executeAction(action: FlexAction): ActionResult {
        // Apply interceptors first
        val processedAction = applyInterceptors(action)
        if (processedAction == null) {
            return ActionResult.Blocked("Action blocked by interceptor")
        }
        
        // Handle based on action type
        when (processedAction.type) {
            "callback" -> {
                val event = processedAction.getEvent()
                if (event != null) {
                    return executeEventHandlers(event, processedAction)
                }
            }
            else -> {
                return executeTypeHandlers(processedAction.type, processedAction)
            }
        }
        
        return ActionResult.NoHandler("No handler found for action type: ${processedAction.type}")
    }
    
    /**
     * Check if a handler exists for an action type
     */
    fun hasTypeHandler(actionType: String): Boolean {
        return typeHandlers[actionType]?.isNotEmpty() == true
    }
    
    /**
     * Check if a handler exists for an event
     */
    fun hasEventHandler(eventName: String): Boolean {
        return eventHandlers[eventName]?.isNotEmpty() == true
    }
    
    /**
     * Get all registered action types
     */
    fun getRegisteredTypes(): Set<String> {
        return typeHandlers.keys.toSet()
    }
    
    /**
     * Get all registered events
     */
    fun getRegisteredEvents(): Set<String> {
        return eventHandlers.keys.toSet()
    }
    
    /**
     * Clear all handlers and interceptors
     */
    fun clear() {
        typeHandlers.clear()
        eventHandlers.clear()
        interceptors.clear()
    }
    
    private fun applyInterceptors(action: FlexAction): FlexAction? {
        var currentAction = action
        
        for (entry in interceptors) {
            val result = entry.interceptor.intercept(currentAction)
            when (result) {
                is InterceptResult.Continue -> {
                    currentAction = result.action
                }
                is InterceptResult.Block -> {
                    return null
                }
            }
        }
        
        return currentAction
    }
    
    private fun executeTypeHandlers(actionType: String, action: FlexAction): ActionResult {
        val handlers = typeHandlers[actionType]
        if (handlers.isNullOrEmpty()) {
            return ActionResult.NoHandler("No handler found for action type: $actionType")
        }
        
        val results = mutableListOf<ActionResult>()
        
        for (entry in handlers) {
            try {
                entry.handler.handle(action)
                results.add(ActionResult.Success)
            } catch (e: Exception) {
                results.add(ActionResult.Error("Handler error: ${e.message}", e))
            }
        }
        
        // Return success if any handler succeeded, otherwise return first error
        return results.firstOrNull { it is ActionResult.Success } 
            ?: results.firstOrNull { it is ActionResult.Error } 
            ?: ActionResult.NoHandler("No handlers executed")
    }
    
    private fun executeEventHandlers(eventName: String, action: FlexAction): ActionResult {
        val handlers = eventHandlers[eventName]
        if (handlers.isNullOrEmpty()) {
            return ActionResult.NoHandler("No handler found for event: $eventName")
        }
        
        val results = mutableListOf<ActionResult>()
        
        for (entry in handlers) {
            try {
                entry.handler.handle(action)
                results.add(ActionResult.Success)
            } catch (e: Exception) {
                results.add(ActionResult.Error("Handler error: ${e.message}", e))
            }
        }
        
        // For events, we consider it success if any handler succeeded
        return if (results.any { it is ActionResult.Success }) {
            ActionResult.Success
        } else {
            results.firstOrNull { it is ActionResult.Error } 
                ?: ActionResult.NoHandler("No handlers executed")
        }
    }
    
    private fun sortByPriority(list: CopyOnWriteArrayList<HandlerEntry>) {
        list.sortWith { a, b -> b.priority.compareTo(a.priority) }
    }
    
    companion object {
        const val PRIORITY_LOW = 1
        const val PRIORITY_NORMAL = 5
        const val PRIORITY_HIGH = 10
        
        @Volatile
        private var INSTANCE: ActionRegistry? = null
        
        /**
         * Get singleton instance
         */
        @JvmStatic
        fun getInstance(): ActionRegistry {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ActionRegistry().also { INSTANCE = it }
            }
        }
    }
}

/**
 * Handler entry with priority
 */
private data class HandlerEntry(
    val handler: FlexActionHandler,
    val priority: Int
)

/**
 * Interceptor entry with priority
 */
private data class InterceptorEntry(
    val interceptor: ActionInterceptor,
    val priority: Int
)

/**
 * Interface for action interceptors
 */
interface ActionInterceptor {
    fun intercept(action: FlexAction): InterceptResult
}

/**
 * Result of action interception
 */
sealed class InterceptResult {
    data class Continue(val action: FlexAction) : InterceptResult()
    object Block : InterceptResult()
}

/**
 * Result of action execution
 */
sealed class ActionResult {
    object Success : ActionResult()
    data class Error(val message: String, val exception: Throwable? = null) : ActionResult()
    data class NoHandler(val message: String) : ActionResult()
    data class Blocked(val message: String) : ActionResult()
}