package dev.flexui.render.components

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme
import dev.flexui.theme.ThemeResolver

/**
 * Renders button components as Button with styling and state support
 */
class ButtonRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val button = Button(context)
        
        // Apply button-specific properties
        applyButtonProperties(context, button, props, theme)
        
        return button
    }
    
    private fun applyButtonProperties(context: Context, button: Button, props: FlexProps, theme: FlexTheme) {
        val themeResolver = ThemeResolver(theme)
        
        // Set button text
        val text = props.getString("text") ?: props.getString("title") ?: ""
        val resolvedText = themeResolver.replaceVariables(text) ?: ""
        button.text = resolvedText
        
        // Set text size
        val fontSize = props.getFloat("fontSize") ?: theme.getFontSize("bodySize") ?: 16f
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        
        // Set text color
        val textColor = props.getString("textColor") ?: props.getString("color")
        if (textColor != null) {
            val colorInt = themeResolver.resolveColor(textColor) ?: theme.getColorInt("primary")
            if (colorInt != null) {
                button.setTextColor(colorInt)
            }
        } else {
            // Default to white text on primary background
            button.setTextColor(0xFFFFFFFF.toInt())
        }
        
        // Set font weight
        val fontWeight = props.getString("fontWeight", "normal")
        val typeface = when (fontWeight) {
            "bold" -> Typeface.DEFAULT_BOLD
            "normal" -> Typeface.DEFAULT
            else -> Typeface.DEFAULT
        }
        button.typeface = typeface
        
        // Set button style/type
        val buttonType = props.getString("type", "filled") ?: "filled"
        applyButtonType(button, buttonType, theme, themeResolver)
        
        // Set enabled/disabled state
        val enabled = props.getBoolean("enabled", true)
        button.isEnabled = enabled
        
        // Set loading state
        val loading = props.getBoolean("loading", false)
        if (loading) {
            applyLoadingState(button, props)
        }
        
        // Set text alignment
        val textAlign = props.getString("textAlign", "center")
        button.gravity = when (textAlign) {
            "left", "start" -> Gravity.START or Gravity.CENTER_VERTICAL
            "center" -> Gravity.CENTER
            "right", "end" -> Gravity.END or Gravity.CENTER_VERTICAL
            else -> Gravity.CENTER
        }
        
        // Set all caps
        val allCaps = props.getBoolean("allCaps", false)
        button.isAllCaps = allCaps
        
        // Set compound drawables (icons)
        val iconLeft = props.getString("iconLeft")
        val iconRight = props.getString("iconRight")
        val iconTop = props.getString("iconTop")
        val iconBottom = props.getString("iconBottom")
        
        applyCompoundDrawables(context, button, iconLeft, iconRight, iconTop, iconBottom)
        
        // Set compound drawable padding
        val iconPadding = props.getInt("iconPadding") ?: theme.getSpacing("xs") ?: 4
        button.compoundDrawablePadding = iconPadding
        
        // Set minimum dimensions
        props.getInt("minWidth")?.let { minWidth ->
            button.minimumWidth = minWidth
        }
        
        props.getInt("minHeight")?.let { minHeight ->
            button.minimumHeight = minHeight
        }
        
        // Set letter spacing
        props.getFloat("letterSpacing")?.let { letterSpacing ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                button.letterSpacing = letterSpacing
            }
        }
    }
    
    private fun applyButtonType(
        button: Button, 
        buttonType: String, 
        theme: FlexTheme, 
        themeResolver: ThemeResolver
    ) {
        when (buttonType) {
            "filled" -> {
                // Primary button with background color
                val backgroundColor = theme.getColorInt("primary") ?: 0xFF007AFF.toInt()
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.setColor(backgroundColor)
                drawable.cornerRadius = (theme.getBorderRadius("md") ?: 8).toFloat()
                button.background = drawable
                button.setTextColor(0xFFFFFFFF.toInt())
            }
            "outlined" -> {
                // Outlined button with border
                val borderColor = theme.getColorInt("primary") ?: 0xFF007AFF.toInt()
                val textColor = theme.getColorInt("primary") ?: 0xFF007AFF.toInt()
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.setColor(0x00000000) // Transparent background
                drawable.setStroke(2, borderColor)
                drawable.cornerRadius = (theme.getBorderRadius("md") ?: 8).toFloat()
                button.background = drawable
                button.setTextColor(textColor)
            }
            "text" -> {
                // Text-only button
                button.background = null
                val textColor = theme.getColorInt("primary") ?: 0xFF007AFF.toInt()
                button.setTextColor(textColor)
            }
            "elevated" -> {
                // Elevated button with shadow
                val backgroundColor = theme.getColorInt("surface") ?: 0xFFF8F8F8.toInt()
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.setColor(backgroundColor)
                drawable.cornerRadius = (theme.getBorderRadius("md") ?: 8).toFloat()
                button.background = drawable
                button.elevation = 4f
                val textColor = theme.getColorInt("primary") ?: 0xFF007AFF.toInt()
                button.setTextColor(textColor)
            }
        }
    }
    
    private fun applyLoadingState(button: Button, props: FlexProps) {
        // Disable button and show loading text
        button.isEnabled = false
        val loadingText = props.getString("loadingText", "Loading...")
        button.text = loadingText
        
        // Could add a progress indicator here
        // For now, just disable the button
    }
    
    private fun applyCompoundDrawables(
        context: Context,
        button: Button,
        iconLeft: String?,
        iconRight: String?,
        iconTop: String?,
        iconBottom: String?
    ) {
        val drawableLeft = iconLeft?.let { getDrawableByName(context, it) }
        val drawableRight = iconRight?.let { getDrawableByName(context, it) }
        val drawableTop = iconTop?.let { getDrawableByName(context, it) }
        val drawableBottom = iconBottom?.let { getDrawableByName(context, it) }
        
        button.setCompoundDrawablesWithIntrinsicBounds(
            drawableLeft,
            drawableTop,
            drawableRight,
            drawableBottom
        )
    }
    
    private fun getDrawableByName(context: Context, name: String): android.graphics.drawable.Drawable? {
        return try {
            val resourceId = context.resources.getIdentifier(
                name.removePrefix("@drawable/"),
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                context.resources.getDrawable(resourceId, context.theme)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}