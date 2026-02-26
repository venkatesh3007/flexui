package dev.flexui.render.components

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders column components as vertical LinearLayout
 */
class ColumnRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val column = LinearLayout(context)
        column.orientation = LinearLayout.VERTICAL
        
        // Apply column-specific properties
        applyColumnProperties(column, props, theme)
        
        return column
    }
    
    private fun applyColumnProperties(column: LinearLayout, props: FlexProps, theme: FlexTheme) {
        // Set alignment
        val alignment = props.getString("alignment", "start")
        column.gravity = when (alignment) {
            "start" -> Gravity.TOP or Gravity.START
            "center" -> Gravity.CENTER
            "end" -> Gravity.BOTTOM or Gravity.START
            "left" -> Gravity.TOP or Gravity.START
            "right" -> Gravity.TOP or Gravity.END
            "spaceBetween" -> Gravity.TOP or Gravity.START // Note: space-between requires custom implementation
            "spaceAround" -> Gravity.CENTER // Note: space-around requires custom implementation
            "spaceEvenly" -> Gravity.CENTER // Note: space-evenly requires custom implementation
            else -> Gravity.TOP or Gravity.START
        }
        
        // Set horizontal alignment
        val horizontalAlignment = props.getString("horizontalAlignment", "start")
        val horizontalGravity = when (horizontalAlignment) {
            "left", "start" -> Gravity.START
            "center" -> Gravity.CENTER_HORIZONTAL
            "right", "end" -> Gravity.END
            "stretch" -> Gravity.FILL_HORIZONTAL
            else -> Gravity.START
        }
        
        // Set vertical alignment
        val verticalAlignment = props.getString("verticalAlignment", "start")
        val verticalGravity = when (verticalAlignment) {
            "top", "start" -> Gravity.TOP
            "center" -> Gravity.CENTER_VERTICAL
            "bottom", "end" -> Gravity.BOTTOM
            else -> Gravity.TOP
        }
        
        // Combine horizontal and vertical gravity
        column.gravity = horizontalGravity or verticalGravity
        
        // Set spacing between children
        val spacing = props.getInt("spacing") ?: theme.getSpacing("sm") ?: 8
        // Note: spacing will be handled by adding margins to child views during rendering
        column.tag = "spacing:$spacing"
        
        // Set wrap behavior if specified
        val wrap = props.getBoolean("wrap", false)
        // Note: LinearLayout doesn't support wrapping natively
        // This would require a custom layout like FlexboxLayout
        
        // Set weighted distribution
        column.weightSum = props.getFloat("weightSum", 0f)
        
        // Set baseline alignment
        val baselineAligned = props.getBoolean("baselineAligned", false)
        column.isBaselineAligned = baselineAligned
        
        // Set divider if specified
        props.getString("divider")?.let { divider ->
            // Could set a divider drawable here
            // column.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            // column.dividerDrawable = createDividerDrawable(divider, theme)
        }
        
        // Set main axis size
        val mainAxisSize = props.getString("mainAxisSize", "max")
        when (mainAxisSize) {
            "min" -> {
                val layoutParams = column.layoutParams ?: LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                column.layoutParams = layoutParams
            }
            "max" -> {
                val layoutParams = column.layoutParams ?: LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                column.layoutParams = layoutParams
            }
        }
        
        // Set cross axis alignment
        val crossAxisAlignment = props.getString("crossAxisAlignment", "center")
        when (crossAxisAlignment) {
            "start" -> column.gravity = column.gravity or Gravity.START
            "center" -> column.gravity = column.gravity or Gravity.CENTER_HORIZONTAL
            "end" -> column.gravity = column.gravity or Gravity.END
            "stretch" -> column.gravity = column.gravity or Gravity.FILL_HORIZONTAL
        }
    }
}