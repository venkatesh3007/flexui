package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders card components as FrameLayout with elevation and background
 */
class CardRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val cardView = FrameLayout(context)
        
        // Apply card-specific properties
        applyCardProperties(cardView, props, theme)
        
        return cardView
    }
    
    private fun applyCardProperties(cardView: FrameLayout, props: FlexProps, theme: FlexTheme) {
        // Set elevation
        val elevation = props.getInt("elevation") ?: theme.getSpacing("sm") ?: 8
        cardView.elevation = elevation.toFloat()
        
        // Set background with corner radius
        val backgroundColor = theme.getColorInt("surface") ?: 0xFFFFFFFF.toInt()
        val cornerRadius = props.getInt("cornerRadius") ?: theme.getBorderRadius("md") ?: 8
        
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.setColor(backgroundColor)
        drawable.cornerRadius = cornerRadius.toFloat()
        cardView.background = drawable
        
        // Set clip to padding to respect corner radius
        cardView.clipToPadding = true
        cardView.clipChildren = true
    }
}