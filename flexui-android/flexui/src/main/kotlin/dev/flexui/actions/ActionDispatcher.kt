package dev.flexui.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.flexui.FlexActionHandler
import dev.flexui.schema.FlexAction
import java.util.concurrent.ConcurrentHashMap

/**
 * Dispatches FlexUI actions to registered handlers.
 * Singleton — use ActionDispatcher.getInstance().
 */
class ActionDispatcher private constructor() {
    
    private val globalHandlers = ConcurrentHashMap<String, FlexActionHandler>()
    private val eventHandlers = ConcurrentHashMap<String, MutableList<FlexActionHandler>>()
    
    fun dispatch(context: Context, action: FlexAction) {
        when (action.type) {
            "navigate" -> handleNavigation(context, action)
            "openUrl" -> handleOpenUrl(context, action)
            "dismiss" -> handleDismiss(context, action)
            "callback" -> handleCallback(context, action)
            else -> handleCustomAction(context, action)
        }
    }
    
    fun registerHandler(actionType: String, handler: FlexActionHandler) {
        globalHandlers[actionType] = handler
    }
    
    fun registerEventHandler(eventName: String, handler: FlexActionHandler) {
        eventHandlers.getOrPut(eventName) { mutableListOf() }.add(handler)
    }
    
    fun unregisterHandler(actionType: String) {
        globalHandlers.remove(actionType)
    }
    
    fun unregisterEventHandler(eventName: String, handler: FlexActionHandler) {
        eventHandlers[eventName]?.remove(handler)
    }
    
    fun clearHandlers() {
        globalHandlers.clear()
        eventHandlers.clear()
    }
    
    fun hasHandler(actionType: String): Boolean = globalHandlers.containsKey(actionType)
    
    fun hasEventHandler(eventName: String): Boolean = eventHandlers[eventName]?.isNotEmpty() == true
    
    fun getRegisteredActionTypes(): Set<String> = globalHandlers.keys.toSet()
    
    fun getRegisteredEvents(): Set<String> = eventHandlers.keys.toSet()
    
    private fun handleNavigation(context: Context, action: FlexAction) {
        val screen = action.getScreen()
        if (screen != null) {
            val handler = globalHandlers["navigate"] ?: globalHandlers["defaultNavigate"]
            handler?.handle(action)
        }
    }
    
    private fun handleOpenUrl(context: Context, action: FlexAction) {
        val url = action.getUrl() ?: return
        val handler = globalHandlers["openUrl"]
        if (handler != null) {
            handler.handle(action)
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                globalHandlers["error"]?.handle(FlexAction.callback("error", mapOf(
                    "type" to "url_open_failed",
                    "url" to url,
                    "error" to (e.message ?: "Unknown error")
                )))
            }
        }
    }
    
    private fun handleDismiss(context: Context, action: FlexAction) {
        val handler = globalHandlers["dismiss"]
        if (handler != null) {
            handler.handle(action)
        } else if (context is android.app.Activity) {
            context.finish()
        }
    }
    
    private fun handleCallback(context: Context, action: FlexAction) {
        val event = action.getEvent() ?: return
        eventHandlers[event]?.forEach { handler ->
            try {
                handler.handle(action)
            } catch (e: Exception) {
                globalHandlers["error"]?.handle(FlexAction.callback("error", mapOf(
                    "type" to "event_handler_error",
                    "event" to event,
                    "error" to (e.message ?: "Unknown error")
                )))
            }
        }
        globalHandlers["callback"]?.handle(action)
    }
    
    private fun handleCustomAction(context: Context, action: FlexAction) {
        val handler = globalHandlers[action.type] ?: globalHandlers["unknown"]
        handler?.handle(action)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ActionDispatcher? = null
        
        @JvmStatic
        fun getInstance(): ActionDispatcher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ActionDispatcher().also { INSTANCE = it }
            }
        }
    }
}
