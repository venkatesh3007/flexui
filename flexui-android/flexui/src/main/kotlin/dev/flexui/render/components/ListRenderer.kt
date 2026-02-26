package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders list components as LinearLayout with dynamic items
 * Note: For production use, this should use RecyclerView for performance
 */
class ListRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val listView = LinearLayout(context)
        listView.orientation = LinearLayout.VERTICAL
        
        // Apply list-specific properties
        applyListProperties(listView, props, theme)
        
        return listView
    }
    
    private fun applyListProperties(listView: LinearLayout, props: FlexProps, theme: FlexTheme) {
        // Set spacing between items
        val spacing = props.getInt("spacing") ?: theme.getSpacing("sm") ?: 8
        listView.tag = "spacing:$spacing"
        
        // Set divider
        val showDivider = props.getBoolean("showDivider", false)
        if (showDivider) {
            // Could implement divider logic here
        }
        
        // Note: Item data and template would be handled by the FlexRenderer
        // when it processes the list's children
    }
}