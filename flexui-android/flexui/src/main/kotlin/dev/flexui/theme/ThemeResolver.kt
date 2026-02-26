package dev.flexui.theme

import dev.flexui.schema.FlexTheme

/**
 * Resolves theme variables in the format {{variable}} to their actual values
 */
class ThemeResolver(private val theme: FlexTheme) {
    
    /**
     * Resolve a string value that may contain theme variables
     */
    fun resolveString(value: String): String? {
        if (!value.startsWith("{{") || !value.endsWith("}}")) {
            return value
        }
        
        val path = value.substring(2, value.length - 2).trim()
        return resolveThemePath(path)?.toString()
    }
    
    /**
     * Resolve an int value from theme variables
     */
    fun resolveInt(value: String): Int? {
        val resolved = resolveString(value) ?: return null
        return resolved.toIntOrNull()
    }
    
    /**
     * Resolve a float value from theme variables
     */
    fun resolveFloat(value: String): Float? {
        val resolved = resolveString(value) ?: return null
        return resolved.toFloatOrNull()
    }
    
    /**
     * Resolve a color value from theme variables
     */
    fun resolveColor(value: String): Int? {
        if (!value.startsWith("{{") || !value.endsWith("}}")) {
            return null
        }
        
        val path = value.substring(2, value.length - 2).trim()
        val colorValue = resolveThemePath(path)?.toString()
        
        return colorValue?.let { color ->
            try {
                if (color.startsWith("#")) {
                    android.graphics.Color.parseColor(color)
                } else {
                    android.graphics.Color.parseColor("#$color")
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Resolve a boolean value from theme variables
     */
    fun resolveBoolean(value: String): Boolean? {
        val resolved = resolveString(value) ?: return null
        return when (resolved.lowercase()) {
            "true", "1", "yes", "on" -> true
            "false", "0", "no", "off" -> false
            else -> null
        }
    }
    
    /**
     * Resolve a theme path like "colors.primary" or "spacing.md"
     */
    private fun resolveThemePath(path: String): Any? {
        val parts = path.split(".")
        if (parts.isEmpty()) return null
        
        val category = parts[0]
        val key = if (parts.size > 1) parts[1] else null
        
        return when (category) {
            "colors" -> {
                if (key != null) {
                    theme.getColor(key)
                } else {
                    theme.colors
                }
            }
            "typography" -> {
                if (key != null) {
                    theme.getTypography(key)
                } else {
                    theme.typography
                }
            }
            "spacing" -> {
                if (key != null) {
                    theme.getSpacing(key)
                } else {
                    theme.spacing
                }
            }
            "borderRadius" -> {
                if (key != null) {
                    theme.getBorderRadius(key)
                } else {
                    theme.borderRadius
                }
            }
            else -> null
        }
    }
    
    /**
     * Replace all theme variables in a string with their resolved values
     */
    fun replaceVariables(text: String): String {
        val pattern = Regex("\\{\\{([^}]+)\\}\\}")
        return pattern.replace(text) { matchResult ->
            val path = matchResult.groupValues[1].trim()
            resolveThemePath(path)?.toString() ?: matchResult.value
        }
    }
    
    /**
     * Check if a string contains theme variables
     */
    fun hasVariables(value: String): Boolean {
        return value.contains("{{") && value.contains("}}")
    }
    
    companion object {
        /**
         * Extract variable names from a string
         */
        @JvmStatic
        fun extractVariables(text: String): List<String> {
            val pattern = Regex("\\{\\{([^}]+)\\}\\}")
            return pattern.findAll(text).map { it.groupValues[1].trim() }.toList()
        }
        
        /**
         * Check if a value is a theme variable
         */
        @JvmStatic
        fun isThemeVariable(value: String): Boolean {
            return value.startsWith("{{") && value.endsWith("}}")
        }
    }
}