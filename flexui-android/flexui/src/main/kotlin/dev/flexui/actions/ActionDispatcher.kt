package dev.flexui.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.flexui.FlexActionHandler
import dev.flexui.schema.FlexAction
import java.util.concurrent.ConcurrentHashMap

/**
 * Dispatches FlexUI actions to registered handlers
 */
class ActionDispatcher private constructor() {
    
    private val globalHandlers = ConcurrentHashMap<String, FlexActionHandler>()
    private val eventHandlers = ConcurrentHashMap<String, MutableList<FlexActionHandler>>()
    
    /**
     * Dispatch an action to appropriate handlers
     */
    fun dispatch(context: Context, action: FlexAction) {
        when (action.type) {
            "navigate" -> handleNavigation(context, action)
            "openUrl" -> handleOpenUrl(context, action)
            "dismiss" -> handleDismiss(context, action)
            "callback" -> handleCallback(context, action)
            else -> handleCustomAction(context, action)
        }
    }
    
    /**
     * Register a global action handler for a specific action type
     */
    fun registerHandler(actionType: String, handler: FlexActionHandler) {
        globalHandlers[actionType] = handler
    }
    
    /**
     * Register an event handler for callback actions
     */
    fun registerEventHandler(eventName: String, handler: FlexActionHandler) {
        eventHandlers.getOrPut(eventName) { mutableListOf() }.add(handler)
    }
    
    /**
     * Unregister a global action handler
     */
    fun unregisterHandler(actionType: String) {
        globalHandlers.remove(actionType)
    }
    
    /**
     * Unregister an event handler
     */
    fun unregisterEventHandler(eventName: String, handler: FlexActionHandler) {
        eventHandlers[eventName]?.remove(handler)
    }
    
    /**
     * Clear all handlers
     */
    fun clearHandlers() {
        globalHandlers.clear()
        eventHandlers.clear()
    }
    
    private fun handleNavigation(context: Context, action: FlexAction) {
        val screen = action.getScreen()
        if (screen != null) {
            val handler = globalHandlers["navigate"]
            if (handler != null) {
                handler.handle(action)
            } else {
                // Default navigation behavior - could be overridden by host app
                // For now, just call any registered navigation handler
                globalHandlers["defaultNavigate"]?.handle(action)
            }
        }
    }
    
    private fun handleOpenUrl(context: Context, action: FlexAction) {
        val url = action.getUrl()
        if (url != null) {
            val handler = globalHandlers["openUrl"]
            if (handler != null) {
                handler.handle(action)
            } else {
                // Default URL opening behavior
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error - maybe show toast or call error handler
                    globalHandlers["error"]?.handle(FlexAction.callback("error", mapOf(
                        "type" to "url_open_failed",
                        "url" to url,
                        "error" to (e.message ?: "Unknown error")
                    )))
                }
            }
        }
    }
    
    private fun handleDismiss(context: Context, action: FlexAction) {
        val handler = globalHandlers["dismiss"]
        if (handler != null) {
            handler.handle(action)
        } else {
            // Default dismiss behavior - finish activity if it's an Activity context
            if (context is android.app.Activity) {
                context.finish()
            }
        }
    }
    
    private fun handleCallback(context: Context, action: FlexAction) {
        val event = action.getEvent()
        if (event != null) {
            // Call all registered event handlers
            eventHandlers[event]?.forEach { handler ->
                try {
                    handler.handle(action)
                } catch (e: Exception) {
                    // Handle error in event handler
                    globalHandlers["error"]?.handle(FlexAction.callback("error", mapOf(
                        "type" to "event_handler_error",
                        "event" to event,
                        "error" to (e.message ?: "Unknown error")
                    )))
                }
            }
            
            // Also call global callback handler if registered
            globalHandlers["callback"]?.handle(action)
        }
    }
    
    private fun handleCustomAction(context: Context, action: FlexAction) {
        val handler = globalHandlers[action.type]
        if (handler != null) {
            handler.handle(action)
        } else {
            // Unknown action type - call generic handler if available
            globalHandlers["unknown"]?.handle(action)
        }
    }
    
    /**
     * Check if a handler is registered for an action type
     */
    fun hasHandler(actionType: String): Boolean {
        return globalHandlers.containsKey(actionType)
    }
    
    /**
     * Check if an event handler is registered
     */
    fun hasEventHandler(eventName: String): Boolean {
        return eventHandlers[eventName]?.isNotEmpty() == true
    }
    
    /**
     * Get list of registered action types
     */
    fun getRegisteredActionTypes(): Set<String> {
        return globalHandlers.keys.toSet()
    }
    
    /**
     * Get list of registered event names
     */
    fun getRegisteredEvents(): Set<String> {
        return eventHandlers.keys.toSet()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ActionDispatcher? = null
        
        /**
         * Get singleton instance
         */
        @JvmStatic
        fun getInstance(): ActionDispatcher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ActionDispatcher().also { INSTANCE = it }
            }
        }
        
        /**
         * Dispatch action using singleton instance
         */
        @JvmStatic
        fun dispatch(context: Context, action: FlexAction) {
            getInstance().dispatch(context, action)
        }
        
        /**
         * Register handler using singleton instance
         */
        @JvmStatic
        fun registerHandler(actionType: String, handler: FlexActionHandler) {
            getInstance().registerHandler(actionType, handler)
        }
        
        /**
         * Register event handler using singleton instance
         */
        @JvmStatic
        fun registerEventHandler(eventName: String, handler: FlexActionHandler) {
            getInstance().registerEventHandler(eventName, handler)
        }
    }
}