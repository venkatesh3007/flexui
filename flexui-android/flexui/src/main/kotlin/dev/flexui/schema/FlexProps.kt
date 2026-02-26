package dev.flexui.schema

/**
 * Provides type-safe access to component properties
 */
class FlexProps(private val data: Map<String, Any>) {
    
    /**
     * Get string property
     */
    fun getString(key: String, default: String? = null): String? {
        return when (val value = data[key]) {
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> default
        }
    }
    
    /**
     * Get required string property
     */
    fun getRequiredString(key: String): String {
        return getString(key) ?: throw IllegalArgumentException("Required property '$key' is missing")
    }
    
    /**
     * Get int property
     */
    fun getInt(key: String, default: Int? = null): Int? {
        return when (val value = data[key]) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: default
            else -> default
        }
    }
    
    /**
     * Get required int property
     */
    fun getRequiredInt(key: String): Int {
        return getInt(key) ?: throw IllegalArgumentException("Required property '$key' is missing")
    }
    
    /**
     * Get float property
     */
    fun getFloat(key: String, default: Float? = null): Float? {
        return when (val value = data[key]) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: default
            else -> default
        }
    }
    
    /**
     * Get required float property
     */
    fun getRequiredFloat(key: String): Float {
        return getFloat(key) ?: throw IllegalArgumentException("Required property '$key' is missing")
    }
    
    /**
     * Get boolean property
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return when (val value = data[key]) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: default
            else -> default
        }
    }
    
    /**
     * Get list property
     */
    fun getList(key: String): List<Any>? {
        return data[key] as? List<Any>
    }
    
    /**
     * Get string list property
     */
    fun getStringList(key: String): List<String>? {
        return getList(key)?.mapNotNull { it as? String }
    }
    
    /**
     * Get map property
     */
    fun getMap(key: String): Map<String, Any>? {
        return data[key] as? Map<String, Any>
    }
    
    /**
     * Get nested FlexProps from map property
     */
    fun getProps(key: String): FlexProps? {
        return getMap(key)?.let { FlexProps(it) }
    }
    
    /**
     * Check if property exists
     */
    fun has(key: String): Boolean {
        return data.containsKey(key)
    }
    
    /**
     * Get all keys
     */
    fun keys(): Set<String> {
        return data.keys
    }
    
    /**
     * Get raw value
     */
    fun getRaw(key: String): Any? {
        return data[key]
    }
    
    /**
     * Get all data as map
     */
    fun toMap(): Map<String, Any> {
        return data.toMap()
    }
    
    /**
     * Check if empty
     */
    fun isEmpty(): Boolean {
        return data.isEmpty()
    }
    
    /**
     * Get size
     */
    fun size(): Int {
        return data.size
    }
    
    companion object {
        @JvmStatic
        fun empty(): FlexProps {
            return FlexProps(emptyMap())
        }
        
        @JvmStatic
        fun of(vararg pairs: Pair<String, Any>): FlexProps {
            return FlexProps(mapOf(*pairs))
        }
    }
}