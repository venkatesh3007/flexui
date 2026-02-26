package dev.flexui.theme

import android.graphics.Color
import dev.flexui.schema.FlexTheme

/**
 * Provides default theme values as fallbacks when theme properties are not defined
 */
object ThemeDefaults {
    
    // Default color palette
    private val DEFAULT_COLORS = mapOf(
        "primary" to "#007AFF",
        "secondary" to "#5856D6",
        "tertiary" to "#AF52DE",
        "background" to "#FFFFFF",
        "surface" to "#F8F8F8",
        "text" to "#000000",
        "textSecondary" to "#6B6B6B",
        "textTertiary" to "#AEAEB2",
        "success" to "#34C759",
        "warning" to "#FF9500",
        "error" to "#FF3B30",
        "info" to "#007AFF",
        "disabled" to "#C7C7CC",
        "border" to "#D1D1D6",
        "shadow" to "#000000",
        "transparent" to "#00000000",
        "white" to "#FFFFFF",
        "black" to "#000000"
    )
    
    // Default typography values
    private val DEFAULT_TYPOGRAPHY = mapOf(
        "titleFont" to "sans-serif-medium",
        "headingFont" to "sans-serif",
        "bodyFont" to "sans-serif",
        "captionFont" to "sans-serif",
        "titleSize" to 28,
        "headingSize" to 22,
        "subheadingSize" to 18,
        "bodySize" to 16,
        "captionSize" to 12,
        "smallSize" to 10,
        "lineHeight" to 1.4f,
        "letterSpacing" to 0f
    )
    
    // Default spacing values (in dp)
    private val DEFAULT_SPACING = mapOf(
        "xxs" to 2,
        "xs" to 4,
        "sm" to 8,
        "md" to 16,
        "lg" to 24,
        "xl" to 32,
        "xxl" to 48,
        "xxxl" to 64
    )
    
    // Default border radius values (in dp)
    private val DEFAULT_BORDER_RADIUS = mapOf(
        "none" to 0,
        "xs" to 2,
        "sm" to 4,
        "md" to 8,
        "lg" to 12,
        "xl" to 16,
        "xxl" to 24,
        "full" to 999
    )
    
    // Default elevation values (in dp)
    private val DEFAULT_ELEVATION = mapOf(
        "none" to 0,
        "xs" to 1,
        "sm" to 2,
        "md" to 4,
        "lg" to 8,
        "xl" to 16,
        "xxl" to 24
    )
    
    // Default opacity values
    private val DEFAULT_OPACITY = mapOf(
        "disabled" to 0.38f,
        "placeholder" to 0.6f,
        "secondary" to 0.8f,
        "overlay" to 0.5f
    )
    
    /**
     * Get default theme instance
     */
    @JvmStatic
    fun getDefaultTheme(): FlexTheme {
        return FlexTheme(
            colors = DEFAULT_COLORS,
            typography = DEFAULT_TYPOGRAPHY,
            spacing = DEFAULT_SPACING,
            borderRadius = DEFAULT_BORDER_RADIUS
        )
    }
    
    /**
     * Get default color value by name
     */
    @JvmStatic
    fun getDefaultColor(name: String): String? {
        return DEFAULT_COLORS[name]
    }
    
    /**
     * Get default color as Android Color int
     */
    @JvmStatic
    fun getDefaultColorInt(name: String): Int? {
        return DEFAULT_COLORS[name]?.let { hex ->
            try {
                Color.parseColor(hex)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Get default typography value by name
     */
    @JvmStatic
    fun getDefaultTypography(name: String): Any? {
        return DEFAULT_TYPOGRAPHY[name]
    }
    
    /**
     * Get default spacing value by name
     */
    @JvmStatic
    fun getDefaultSpacing(name: String): Int? {
        return DEFAULT_SPACING[name]
    }
    
    /**
     * Get default border radius value by name
     */
    @JvmStatic
    fun getDefaultBorderRadius(name: String): Int? {
        return DEFAULT_BORDER_RADIUS[name]
    }
    
    /**
     * Get default elevation value by name
     */
    @JvmStatic
    fun getDefaultElevation(name: String): Int? {
        return DEFAULT_ELEVATION[name]
    }
    
    /**
     * Get default opacity value by name
     */
    @JvmStatic
    fun getDefaultOpacity(name: String): Float? {
        return DEFAULT_OPACITY[name]
    }
    
    /**
     * Merge provided theme with defaults, using defaults as fallback
     */
    @JvmStatic
    fun mergeWithDefaults(theme: FlexTheme): FlexTheme {
        return FlexTheme(
            colors = DEFAULT_COLORS + theme.colors,
            typography = DEFAULT_TYPOGRAPHY + theme.typography,
            spacing = DEFAULT_SPACING + theme.spacing,
            borderRadius = DEFAULT_BORDER_RADIUS + theme.borderRadius
        )
    }
    
    /**
     * Create a theme with only the specified overrides
     */
    @JvmStatic
    fun createTheme(
        colors: Map<String, String> = emptyMap(),
        typography: Map<String, Any> = emptyMap(),
        spacing: Map<String, Int> = emptyMap(),
        borderRadius: Map<String, Int> = emptyMap()
    ): FlexTheme {
        return FlexTheme(
            colors = DEFAULT_COLORS + colors,
            typography = DEFAULT_TYPOGRAPHY + typography,
            spacing = DEFAULT_SPACING + spacing,
            borderRadius = DEFAULT_BORDER_RADIUS + borderRadius
        )
    }
    
    /**
     * Get common dark theme
     */
    @JvmStatic
    fun getDarkTheme(): FlexTheme {
        val darkColors = DEFAULT_COLORS.toMutableMap()
        darkColors["background"] = "#000000"
        darkColors["surface"] = "#1C1C1E"
        darkColors["text"] = "#FFFFFF"
        darkColors["textSecondary"] = "#AEAEB2"
        darkColors["textTertiary"] = "#6B6B6B"
        darkColors["border"] = "#38383A"
        
        return FlexTheme(
            colors = darkColors,
            typography = DEFAULT_TYPOGRAPHY,
            spacing = DEFAULT_SPACING,
            borderRadius = DEFAULT_BORDER_RADIUS
        )
    }
}