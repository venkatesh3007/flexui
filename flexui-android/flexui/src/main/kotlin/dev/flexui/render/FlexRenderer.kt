package dev.flexui.render

import android.content.Context
import android.view.View
import android.view.ViewGroup
import dev.flexui.actions.ActionDispatcher
import dev.flexui.registry.ComponentRegistry
import dev.flexui.schema.FlexNode
import dev.flexui.schema.FlexTheme
import dev.flexui.theme.ThemeResolver

/**
 * Core renderer that converts FlexNode tree into Android View hierarchy
 */
class FlexRenderer private constructor(
    private val componentRegistry: ComponentRegistry,
    private val actionDispatcher: ActionDispatcher
) {
    
    /**
     * Render a FlexNode tree into a View hierarchy
     */
    fun render(
        context: Context, 
        node: FlexNode, 
        theme: FlexTheme,
        data: Map<String, Any> = emptyMap()
    ): View? {
        // Check if node should be rendered based on conditions
        if (!node.shouldRender(data, theme)) {
            return null
        }
        
        // Create view for this node
        val view = createViewForNode(context, node, theme)
        
        if (view != null) {
            // Apply styling
            applyStyle(view, node, theme)
            
            // Render children if this is a container
            if (view is ViewGroup && node.children != null) {
                renderChildren(context, view, node.children, theme, data)
            }
            
            // Bind actions
            bindAction(context, view, node)
            
            // Set accessibility
            applyAccessibility(view, node)
        }
        
        return view
    }
    
    /**
     * Render children nodes into a container view
     */
    fun renderChildren(
        context: Context,
        container: ViewGroup,
        children: List<FlexNode>,
        theme: FlexTheme,
        data: Map<String, Any> = emptyMap()
    ) {
        for (child in children) {
            val childView = render(context, child, theme, data)
            if (childView != null) {
                container.addView(childView)
            }
        }
    }
    
    /**
     * Create view for a specific node using component registry
     */
    private fun createViewForNode(
        context: Context,
        node: FlexNode,
        theme: FlexTheme
    ): View? {
        return componentRegistry.createView(context, node, theme, this)
    }
    
    /**
     * Apply styling to a view based on node style properties
     */
    private fun applyStyle(view: View, node: FlexNode, theme: FlexTheme) {
        val style = node.resolvedStyle(theme)
        
        // Apply padding
        view.setPadding(
            style.paddingLeft,
            style.paddingTop,
            style.paddingRight,
            style.paddingBottom
        )
        
        // Apply layout parameters if view is not root
        val layoutParams = view.layoutParams ?: ViewGroup.LayoutParams(
            style.width ?: ViewGroup.LayoutParams.MATCH_PARENT,
            style.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.setMargins(
                style.marginLeft,
                style.marginTop,
                style.marginRight,
                style.marginBottom
            )
        }
        
        view.layoutParams = layoutParams
        
        // Apply elevation
        if (style.elevation > 0) {
            view.elevation = style.elevation.toFloat()
        }
        
        // Apply background if specified
        if (style.backgroundColor != 0x00000000) {
            applyBackground(view, style)
        }
    }
    
    /**
     * Apply background drawable with color, border radius, and border
     */
    private fun applyBackground(view: View, style: dev.flexui.schema.ResolvedStyle) {
        val drawable = android.graphics.drawable.GradientDrawable()
        
        // Background color
        drawable.setColor(style.backgroundColor)
        
        // Border radius
        if (style.borderRadius > 0) {
            drawable.cornerRadius = style.borderRadius.toFloat()
        }
        
        // Border
        if (style.borderWidth > 0 && style.borderColor != 0x00000000) {
            drawable.setStroke(style.borderWidth, style.borderColor)
        }
        
        view.background = drawable
    }
    
    /**
     * Bind action to view click listener
     */
    private fun bindAction(context: Context, view: View, node: FlexNode) {
        node.action?.let { action ->
            view.setOnClickListener {
                actionDispatcher.dispatch(context, action)
            }
            
            // Make view clickable and focusable
            view.isClickable = true
            view.isFocusable = true
        }
    }
    
    /**
     * Apply accessibility properties
     */
    private fun applyAccessibility(view: View, node: FlexNode) {
        node.id?.let { id ->
            // Set content description if ID is provided
            view.contentDescription = id
        }
        
        // Set accessibility properties based on node type
        when (node.type) {
            "button" -> {
                view.isClickable = true
                view.isFocusable = true
            }
            "text" -> {
                // Text views should be accessible for screen readers
                view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: FlexRenderer? = null
        
        @JvmStatic
        fun getInstance(): FlexRenderer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlexRenderer(
                    ComponentRegistry.getInstance(),
                    ActionDispatcher.getInstance()
                ).also { INSTANCE = it }
            }
        }
        
        @JvmStatic
        fun create(): FlexRenderer {
            return FlexRenderer(
                ComponentRegistry.getInstance(),
                ActionDispatcher.getInstance()
            )
        }
    }
}