package dev.flexui.schema

import android.graphics.Color
import org.json.JSONObject

/**
 * Represents theme configuration for FlexUI components
 */
data class FlexTheme(
    val colors: Map<String, String> = emptyMap(),
    val typography: Map<String, Any> = emptyMap(),
    val spacing: Map<String, Int> = emptyMap(),
    val borderRadius: Map<String, Int> = emptyMap()
) {
    
    /**
     * Get a color value by name, returns hex string
     */
    fun getColor(name: String): String? {
        return colors[name]
    }
    
    /**
     * Get a color value by name, returns Android Color int
     */
    fun getColorInt(name: String): Int? {
        return colors[name]?.let { hex ->
            try {
                if (hex.startsWith("#")) {
                    Color.parseColor(hex)
                } else {
                    Color.parseColor("#$hex")
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Get a spacing value by name
     */
    fun getSpacing(name: String): Int? {
        return spacing[name]
    }
    
    /**
     * Get a border radius value by name
     */
    fun getBorderRadius(name: String): Int? {
        return borderRadius[name]
    }
    
    /**
     * Get a typography value by name
     */
    fun getTypography(name: String): Any? {
        return typography[name]
    }
    
    /**
     * Get font size from typography
     */
    fun getFontSize(name: String): Float? {
        return when (val value = typography[name]) {
            is Number -> value.toFloat()
            else -> null
        }
    }
    
    /**
     * Get font family from typography
     */
    fun getFontFamily(name: String): String? {
        return typography[name] as? String
    }
    
    companion object {
        @JvmStatic
        fun fromJson(json: JSONObject): FlexTheme {
            val colorsMap = mutableMapOf<String, String>()
            val typographyMap = mutableMapOf<String, Any>()
            val spacingMap = mutableMapOf<String, Int>()
            val borderRadiusMap = mutableMapOf<String, Int>()
            
            // Parse colors
            json.optJSONObject("colors")?.let { colorsJson ->
                val keys = colorsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    colorsMap[key] = colorsJson.getString(key)
                }
            }
            
            // Parse typography
            json.optJSONObject("typography")?.let { typographyJson ->
                val keys = typographyJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    typographyMap[key] = typographyJson.get(key)
                }
            }
            
            // Parse spacing
            json.optJSONObject("spacing")?.let { spacingJson ->
                val keys = spacingJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    spacingMap[key] = spacingJson.getInt(key)
                }
            }
            
            // Parse border radius
            json.optJSONObject("borderRadius")?.let { borderRadiusJson ->
                val keys = borderRadiusJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    borderRadiusMap[key] = borderRadiusJson.getInt(key)
                }
            }
            
            return FlexTheme(
                colors = colorsMap,
                typography = typographyMap,
                spacing = spacingMap,
                borderRadius = borderRadiusMap
            )
        }
        
        @JvmStatic
        fun getDefault(): FlexTheme {
            return FlexTheme(
                colors = mapOf(
                    "primary" to "#FF6B00",
                    "secondary" to "#1A1A2E",
                    "background" to "#FFFFFF",
                    "text" to "#333333",
                    "textSecondary" to "#999999",
                    "success" to "#4CAF50",
                    "error" to "#F44336",
                    "transparent" to "#00000000"
                ),
                typography = mapOf(
                    "headingFont" to "sans-serif",
                    "bodyFont" to "sans-serif",
                    "headingSize" to 24,
                    "bodySize" to 14,
                    "captionSize" to 12
                ),
                spacing = mapOf(
                    "xs" to 4,
                    "sm" to 8,
                    "md" to 16,
                    "lg" to 24,
                    "xl" to 32
                ),
                borderRadius = mapOf(
                    "sm" to 4,
                    "md" to 8,
                    "lg" to 16,
                    "full" to 999
                )
            )
        }
    }
}