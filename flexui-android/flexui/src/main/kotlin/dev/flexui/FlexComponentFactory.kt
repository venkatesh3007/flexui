package dev.flexui

import android.content.Context
import android.view.View
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Interface for creating custom FlexUI components.
 * Implement this interface to register custom component types.
 */
fun interface FlexComponentFactory {
    /**
     * Create a View for this component type.
     * 
     * @param context Android context
     * @param props Component properties from JSON
     * @param theme Current theme configuration
     * @return View instance for this component
     */
    fun create(context: Context, props: FlexProps, theme: FlexTheme): View
}