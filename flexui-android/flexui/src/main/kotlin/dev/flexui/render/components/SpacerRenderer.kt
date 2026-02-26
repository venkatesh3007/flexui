package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders spacer components as Space or View for flexible spacing
 */
class SpacerRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        // Use Space view for better performance (no drawing)
        val spacerView = Space(context)
        
        // Apply spacer-specific properties
        applySpacerProperties(spacerView, props, theme)
        
        return spacerView
    }
    
    private fun applySpacerProperties(spacerView: Space, props: FlexProps, theme: FlexTheme) {
        // Set size - can be fixed or flexible
        val size = props.getInt("size")
        val flex = props.getFloat("flex")
        
        when {
            size != null -> {
                // Fixed size spacer
                val layoutParams = ViewGroup.LayoutParams(size, size)
                spacerView.layoutParams = layoutParams
            }
            flex != null -> {
                // Flexible spacer that takes available space
                val layoutParams = android.widget.LinearLayout.LayoutParams(0, 0, flex)
                spacerView.layoutParams = layoutParams
            }
            else -> {
                // Default small spacer
                val defaultSize = theme.getSpacing("sm") ?: 8
                val layoutParams = ViewGroup.LayoutParams(defaultSize, defaultSize)
                spacerView.layoutParams = layoutParams
            }
        }
        
        // Set width and height separately if specified
        props.getInt("width")?.let { width ->
            val currentParams = spacerView.layoutParams ?: ViewGroup.LayoutParams(0, 0)
            currentParams.width = width
            spacerView.layoutParams = currentParams
        }
        
        props.getInt("height")?.let { height ->
            val currentParams = spacerView.layoutParams ?: ViewGroup.LayoutParams(0, 0)
            currentParams.height = height
            spacerView.layoutParams = currentParams
        }
        
        // Set minimum dimensions
        props.getInt("minWidth")?.let { minWidth ->
            spacerView.minimumWidth = minWidth
        }
        
        props.getInt("minHeight")?.let { minHeight ->
            spacerView.minimumHeight = minHeight
        }
    }
}