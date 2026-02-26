package dev.flexui.render.components

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders row components as horizontal LinearLayout
 */
class RowRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        
        // Apply row-specific properties
        applyRowProperties(row, props, theme)
        
        return row
    }
    
    private fun applyRowProperties(row: LinearLayout, props: FlexProps, theme: FlexTheme) {
        // Set alignment
        val alignment = props.getString("alignment", "start")
        row.gravity = when (alignment) {
            "start" -> Gravity.START or Gravity.CENTER_VERTICAL
            "center" -> Gravity.CENTER
            "end" -> Gravity.END or Gravity.CENTER_VERTICAL
            "top" -> Gravity.START or Gravity.TOP
            "bottom" -> Gravity.START or Gravity.BOTTOM
            "spaceBetween" -> Gravity.START or Gravity.CENTER_VERTICAL // Note: space-between requires custom implementation
            "spaceAround" -> Gravity.CENTER // Note: space-around requires custom implementation
            "spaceEvenly" -> Gravity.CENTER // Note: space-evenly requires custom implementation
            else -> Gravity.START or Gravity.CENTER_VERTICAL
        }
        
        // Set vertical alignment
        val verticalAlignment = props.getString("verticalAlignment", "center")
        val verticalGravity = when (verticalAlignment) {
            "top" -> Gravity.TOP
            "center" -> Gravity.CENTER_VERTICAL
            "bottom" -> Gravity.BOTTOM
            "stretch" -> Gravity.FILL_VERTICAL
            else -> Gravity.CENTER_VERTICAL
        }
        
        // Combine horizontal and vertical gravity
        val horizontalGravity = when (alignment) {
            "start" -> Gravity.START
            "center" -> Gravity.CENTER_HORIZONTAL
            "end" -> Gravity.END
            else -> Gravity.START
        }
        
        row.gravity = horizontalGravity or verticalGravity
        
        // Set spacing between children
        val spacing = props.getInt("spacing") ?: theme.getSpacing("sm") ?: 8
        // Note: spacing will be handled by adding margins to child views during rendering
        row.tag = "spacing:$spacing"
        
        // Set wrap behavior if specified
        val wrap = props.getBoolean("wrap", false)
        // Note: LinearLayout doesn't support wrapping natively
        // This would require a custom layout like FlexboxLayout
        
        // Set weighted distribution
        row.weightSum = props.getFloat("weightSum", 0f)
        
        // Set baseline alignment
        val baselineAligned = props.getBoolean("baselineAligned", true)
        row.isBaselineAligned = baselineAligned
        
        // Set divider if specified
        props.getString("divider")?.let { divider ->
            // Could set a divider drawable here
            // row.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            // row.dividerDrawable = createDividerDrawable(divider, theme)
        }
    }
}