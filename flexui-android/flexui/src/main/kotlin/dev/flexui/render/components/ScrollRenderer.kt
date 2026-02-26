package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders scroll components as ScrollView (vertical) or HorizontalScrollView
 */
class ScrollRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val direction = props.getString("direction", "vertical")
        
        return when (direction) {
            "horizontal" -> {
                val scrollView = HorizontalScrollView(context)
                applyHorizontalScrollProperties(scrollView, props)
                scrollView
            }
            "vertical" -> {
                val scrollView = ScrollView(context)
                applyVerticalScrollProperties(scrollView, props)
                scrollView
            }
            else -> {
                val scrollView = ScrollView(context)
                applyVerticalScrollProperties(scrollView, props)
                scrollView
            }
        }
    }
    
    private fun applyVerticalScrollProperties(scrollView: ScrollView, props: FlexProps) {
        // Set scroll behavior
        val fillViewport = props.getBoolean("fillViewport", false)
        scrollView.isFillViewport = fillViewport
        
        // Set scroll bar visibility
        val showScrollbars = props.getBoolean("showScrollbars", true)
        scrollView.isVerticalScrollBarEnabled = showScrollbars
        
        // Set smooth scrolling
        val smoothScrolling = props.getBoolean("smoothScrolling", true)
        scrollView.isSmoothScrollingEnabled = smoothScrolling
        
        // Set overscroll mode
        val overScrollMode = props.getString("overScrollMode", "always")
        scrollView.overScrollMode = when (overScrollMode) {
            "always" -> View.OVER_SCROLL_ALWAYS
            "ifContentScrolls" -> View.OVER_SCROLL_IF_CONTENT_SCROLLS
            "never" -> View.OVER_SCROLL_NEVER
            else -> View.OVER_SCROLL_ALWAYS
        }
        
        // Set fade edge length
        props.getInt("fadeEdgeLength")?.let { length ->
            scrollView.setVerticalFadingEdgeEnabled(true)
            scrollView.setFadingEdgeLength(length)
        }
        
        // Set scroll indicator style
        val scrollbarStyle = props.getString("scrollbarStyle", "insideOverlay")
        scrollView.scrollBarStyle = when (scrollbarStyle) {
            "insideInset" -> View.SCROLLBARS_INSIDE_INSET
            "insideOverlay" -> View.SCROLLBARS_INSIDE_OVERLAY
            "outsideInset" -> View.SCROLLBARS_OUTSIDE_INSET
            "outsideOverlay" -> View.SCROLLBARS_OUTSIDE_OVERLAY
            else -> View.SCROLLBARS_INSIDE_OVERLAY
        }
    }
    
    private fun applyHorizontalScrollProperties(scrollView: HorizontalScrollView, props: FlexProps) {
        // Set scroll behavior
        val fillViewport = props.getBoolean("fillViewport", false)
        scrollView.isFillViewport = fillViewport
        
        // Set scroll bar visibility
        val showScrollbars = props.getBoolean("showScrollbars", true)
        scrollView.isHorizontalScrollBarEnabled = showScrollbars
        
        // Set smooth scrolling
        val smoothScrolling = props.getBoolean("smoothScrolling", true)
        scrollView.isSmoothScrollingEnabled = smoothScrolling
        
        // Set overscroll mode
        val overScrollMode = props.getString("overScrollMode", "always")
        scrollView.overScrollMode = when (overScrollMode) {
            "always" -> View.OVER_SCROLL_ALWAYS
            "ifContentScrolls" -> View.OVER_SCROLL_IF_CONTENT_SCROLLS
            "never" -> View.OVER_SCROLL_NEVER
            else -> View.OVER_SCROLL_ALWAYS
        }
        
        // Set fade edge length
        props.getInt("fadeEdgeLength")?.let { length ->
            scrollView.setHorizontalFadingEdgeEnabled(true)
            scrollView.setFadingEdgeLength(length)
        }
        
        // Set scroll indicator style
        val scrollbarStyle = props.getString("scrollbarStyle", "insideOverlay")
        scrollView.scrollBarStyle = when (scrollbarStyle) {
            "insideInset" -> View.SCROLLBARS_INSIDE_INSET
            "insideOverlay" -> View.SCROLLBARS_INSIDE_OVERLAY
            "outsideInset" -> View.SCROLLBARS_OUTSIDE_INSET
            "outsideOverlay" -> View.SCROLLBARS_OUTSIDE_OVERLAY
            else -> View.SCROLLBARS_INSIDE_OVERLAY
        }
    }
}