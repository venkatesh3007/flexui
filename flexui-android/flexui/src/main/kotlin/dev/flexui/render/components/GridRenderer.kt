package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders grid components as GridLayout
 */
class GridRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val gridLayout = GridLayout(context)
        
        // Apply grid-specific properties
        applyGridProperties(gridLayout, props, theme)
        
        return gridLayout
    }
    
    private fun applyGridProperties(gridLayout: GridLayout, props: FlexProps, theme: FlexTheme) {
        // Set column count
        val columns = props.getInt("columns") ?: 2
        gridLayout.columnCount = columns
        
        // Set row count (optional)
        props.getInt("rows")?.let { rows ->
            gridLayout.rowCount = rows
        }
        
        // Set orientation
        val orientation = props.getString("orientation", "horizontal")
        gridLayout.orientation = when (orientation) {
            "vertical" -> GridLayout.VERTICAL
            "horizontal" -> GridLayout.HORIZONTAL
            else -> GridLayout.HORIZONTAL
        }
        
        // Set alignment
        val useDefaultMargins = props.getBoolean("useDefaultMargins", false)
        gridLayout.setUseDefaultMargins(useDefaultMargins)
        
        val alignmentMode = props.getString("alignmentMode", "alignBounds")
        gridLayout.alignmentMode = when (alignmentMode) {
            "alignBounds" -> GridLayout.ALIGN_BOUNDS
            "alignMargins" -> GridLayout.ALIGN_MARGINS
            else -> GridLayout.ALIGN_BOUNDS
        }
    }
}