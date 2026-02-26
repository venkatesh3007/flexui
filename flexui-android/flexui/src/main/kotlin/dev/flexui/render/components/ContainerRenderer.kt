package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders container components as FrameLayout with styling support
 */
class ContainerRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val container = FrameLayout(context)
        
        // Apply container-specific properties
        applyContainerProperties(container, props)
        
        return container
    }
    
    private fun applyContainerProperties(container: FrameLayout, props: FlexProps) {
        // Set minimum dimensions if specified
        props.getInt("minWidth")?.let { minWidth ->
            container.minimumWidth = minWidth
        }
        
        props.getInt("minHeight")?.let { minHeight ->
            container.minimumHeight = minHeight
        }
        
        // Set max dimensions if specified (requires custom implementation)
        props.getInt("maxWidth")?.let { maxWidth ->
            // Note: FrameLayout doesn't directly support maxWidth,
            // but this could be implemented via custom layout params
        }
        
        props.getInt("maxHeight")?.let { maxHeight ->
            // Note: FrameLayout doesn't directly support maxHeight,
            // but this could be implemented via custom layout params
        }
        
        // Set clipToPadding if specified
        props.getBoolean("clipToPadding", true).let { clipToPadding ->
            container.clipToPadding = clipToPadding
        }
        
        // Set clipChildren if specified
        props.getBoolean("clipChildren", true).let { clipChildren ->
            container.clipChildren = clipChildren
        }
    }
}