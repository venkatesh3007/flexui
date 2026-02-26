package dev.flexui.schema

import org.json.JSONObject

/**
 * Represents an action that can be triggered by user interaction
 */
data class FlexAction(
    val type: String,
    val data: Map<String, Any> = emptyMap()
) {
    /**
     * Get action data as string
     */
    fun getString(key: String): String? {
        return data[key] as? String
    }
    
    /**
     * Get action data as int
     */
    fun getInt(key: String): Int? {
        return when (val value = data[key]) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }
    
    /**
     * Get action data as boolean
     */
    fun getBoolean(key: String): Boolean? {
        return when (val value = data[key]) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull()
            else -> null
        }
    }
    
    /**
     * Get action data as map
     */
    fun getMap(key: String): Map<String, Any>? {
        return data[key] as? Map<String, Any>
    }
    
    /**
     * Check if this is a navigation action
     */
    fun isNavigation(): Boolean {
        return type == "navigate"
    }
    
    /**
     * Check if this is a callback action
     */
    fun isCallback(): Boolean {
        return type == "callback"
    }
    
    /**
     * Check if this is an URL action
     */
    fun isOpenUrl(): Boolean {
        return type == "openUrl"
    }
    
    /**
     * Check if this is a dismiss action
     */
    fun isDismiss(): Boolean {
        return type == "dismiss"
    }
    
    /**
     * Get navigation screen ID
     */
    fun getScreen(): String? {
        return if (isNavigation()) getString("screen") else null
    }
    
    /**
     * Get callback event name
     */
    fun getEvent(): String? {
        return if (isCallback()) getString("event") else null
    }
    
    /**
     * Get URL to open
     */
    fun getUrl(): String? {
        return if (isOpenUrl()) getString("url") else null
    }
    
    /**
     * Get callback data
     */
    fun getCallbackData(): Map<String, Any>? {
        return if (isCallback()) getMap("data") else null
    }
    
    companion object {
        @JvmStatic
        fun fromJson(json: JSONObject): FlexAction {
            val type = json.getString("type")
            val dataMap = mutableMapOf<String, Any>()
            
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key != "type") {
                    dataMap[key] = json.get(key)
                }
            }
            
            return FlexAction(type = type, data = dataMap)
        }
        
        /**
         * Create a navigation action
         */
        @JvmStatic
        fun navigate(screen: String, data: Map<String, Any> = emptyMap()): FlexAction {
            return FlexAction(
                type = "navigate",
                data = mapOf("screen" to screen) + data
            )
        }
        
        /**
         * Create a callback action
         */
        @JvmStatic
        fun callback(event: String, data: Map<String, Any> = emptyMap()): FlexAction {
            return FlexAction(
                type = "callback",
                data = mapOf("event" to event, "data" to data)
            )
        }
        
        /**
         * Create an open URL action
         */
        @JvmStatic
        fun openUrl(url: String): FlexAction {
            return FlexAction(
                type = "openUrl",
                data = mapOf("url" to url)
            )
        }
        
        /**
         * Create a dismiss action
         */
        @JvmStatic
        fun dismiss(): FlexAction {
            return FlexAction(type = "dismiss")
        }
    }
}