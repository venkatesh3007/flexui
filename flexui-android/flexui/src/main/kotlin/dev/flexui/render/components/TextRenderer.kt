package dev.flexui.render.components

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme
import dev.flexui.theme.ThemeResolver

/**
 * Renders text components as TextView with rich styling support
 */
class TextRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val textView = TextView(context)
        
        // Apply text-specific properties
        applyTextProperties(textView, props, theme)
        
        return textView
    }
    
    private fun applyTextProperties(textView: TextView, props: FlexProps, theme: FlexTheme) {
        val themeResolver = ThemeResolver(theme)
        
        // Set text content
        val content = props.getString("content") ?: props.getString("text") ?: ""
        val resolvedContent = themeResolver.replaceVariables(content)
        textView.text = resolvedContent
        
        // Set text size
        val fontSize = props.getFloat("fontSize") ?: theme.getFontSize("bodySize") ?: 16f
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        
        // Set text color
        val textColor = props.getString("color") ?: props.getString("textColor")
        if (textColor != null) {
            val colorInt = themeResolver.resolveColor(textColor) ?: theme.getColorInt("text")
            if (colorInt != null) {
                textView.setTextColor(colorInt)
            }
        } else {
            theme.getColorInt("text")?.let { textView.setTextColor(it) }
        }
        
        // Set font weight
        val fontWeight = props.getString("fontWeight", "normal")
        val typeface = when (fontWeight) {
            "bold" -> Typeface.DEFAULT_BOLD
            "normal" -> Typeface.DEFAULT
            else -> Typeface.DEFAULT
        }
        textView.typeface = typeface
        
        // Set font family
        val fontFamily = props.getString("fontFamily") ?: theme.getFontFamily("bodyFont")
        if (fontFamily != null) {
            try {
                val customTypeface = Typeface.create(fontFamily, textView.typeface.style)
                textView.typeface = customTypeface
            } catch (e: Exception) {
                // Fallback to default typeface if custom font fails
            }
        }
        
        // Set text alignment
        val textAlign = props.getString("textAlign", "left")
        textView.gravity = when (textAlign) {
            "left", "start" -> Gravity.START
            "center" -> Gravity.CENTER_HORIZONTAL
            "right", "end" -> Gravity.END
            "justify" -> Gravity.START // Note: full justification requires API 26+
            else -> Gravity.START
        }
        
        // Set max lines
        val maxLines = props.getInt("maxLines") ?: Int.MAX_VALUE
        if (maxLines != Int.MAX_VALUE) {
            textView.maxLines = maxLines
        }
        
        // Set ellipsize mode
        val ellipsize = props.getString("ellipsize", "end")
        textView.ellipsize = when (ellipsize) {
            "start" -> TextUtils.TruncateAt.START
            "middle" -> TextUtils.TruncateAt.MIDDLE
            "end" -> TextUtils.TruncateAt.END
            "marquee" -> TextUtils.TruncateAt.MARQUEE
            "none" -> null
            else -> TextUtils.TruncateAt.END
        }
        
        // Set line height
        props.getFloat("lineHeight")?.let { lineHeight ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                textView.lineHeight = (fontSize * lineHeight).toInt()
            } else {
                textView.setLineSpacing(0f, lineHeight)
            }
        }
        
        // Set letter spacing
        props.getFloat("letterSpacing")?.let { letterSpacing ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                textView.letterSpacing = letterSpacing
            }
        }
        
        // Set text style variations
        val textStyle = props.getString("textStyle")
        when (textStyle) {
            "italic" -> textView.typeface = Typeface.create(textView.typeface, Typeface.ITALIC)
            "bold" -> textView.typeface = Typeface.create(textView.typeface, Typeface.BOLD)
            "boldItalic" -> textView.typeface = Typeface.create(textView.typeface, Typeface.BOLD_ITALIC)
        }
        
        // Set text decoration
        val textDecoration = props.getString("textDecoration")
        when (textDecoration) {
            "underline" -> textView.paintFlags = textView.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            "strikethrough" -> textView.paintFlags = textView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        }
        
        // Set selectable
        val selectable = props.getBoolean("selectable", false)
        textView.isTextSelectable = selectable
        
        // Set single line
        val singleLine = props.getBoolean("singleLine", false)
        textView.isSingleLine = singleLine
        
        // Set auto size if specified
        props.getInt("autoSizeMinTextSize")?.let { minSize ->
            props.getInt("autoSizeMaxTextSize")?.let { maxSize ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    textView.setAutoSizeTextTypeUniformWithConfiguration(
                        minSize,
                        maxSize,
                        props.getInt("autoSizeStepGranularity") ?: 1,
                        TypedValue.COMPLEX_UNIT_SP
                    )
                }
            }
        }
        
        // Set shadow if specified
        props.getFloat("shadowRadius")?.let { shadowRadius ->
            val shadowDx = props.getFloat("shadowDx") ?: 0f
            val shadowDy = props.getFloat("shadowDy") ?: 0f
            val shadowColor = props.getString("shadowColor")?.let { color ->
                themeResolver.resolveColor(color)
            } ?: 0x80000000.toInt()
            
            textView.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
        }
    }
}