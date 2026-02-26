package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders divider components as View with line styling
 */
class DividerRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val dividerView = View(context)
        
        // Apply divider-specific properties
        applyDividerProperties(dividerView, props, theme)
        
        return dividerView
    }
    
    private fun applyDividerProperties(dividerView: View, props: FlexProps, theme: FlexTheme) {
        // Set orientation and size
        val orientation = props.getString("orientation", "horizontal")
        val thickness = props.getInt("thickness") ?: 1
        
        val layoutParams = when (orientation) {
            "horizontal" -> ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                thickness
            )
            "vertical" -> ViewGroup.LayoutParams(
                thickness,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            else -> ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                thickness
            )
        }
        
        dividerView.layoutParams = layoutParams
        
        // Set color
        val color = props.getString("color")?.let { colorStr ->
            try {
                android.graphics.Color.parseColor(
                    if (colorStr.startsWith("#")) colorStr else "#$colorStr"
                )
            } catch (e: Exception) {
                null
            }
        } ?: theme.getColorInt("border") ?: 0xFFE0E0E0.toInt()
        
        dividerView.setBackgroundColor(color)
        
        // Set margin
        val margin = props.getInt("margin") ?: 0
        if (margin > 0 && layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.setMargins(margin, margin, margin, margin)
        }
    }
}