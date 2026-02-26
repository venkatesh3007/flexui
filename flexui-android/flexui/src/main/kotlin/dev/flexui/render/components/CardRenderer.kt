package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders card components as a vertical LinearLayout with elevation and rounded background.
 * Children stack vertically (like a column) — this is the natural behavior for cards.
 */
class CardRenderer : FlexComponentFactory {

    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val cardView = LinearLayout(context)
        cardView.orientation = LinearLayout.VERTICAL

        // Elevation
        val elevation = props.getInt("elevation") ?: 4
        cardView.elevation = elevation.toFloat() * context.resources.displayMetrics.density

        // Background with corner radius
        val backgroundColor = theme.getColorInt("surface") ?: 0xFFFFFFFF.toInt()
        val cornerRadius = props.getInt("cornerRadius") ?: theme.getBorderRadius("md") ?: 8

        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.setColor(backgroundColor)
        drawable.cornerRadius = cornerRadius.toFloat() * context.resources.displayMetrics.density
        cardView.background = drawable

        cardView.clipToPadding = true
        cardView.clipChildren = true

        return cardView
    }
}
