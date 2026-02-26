package dev.flexui.registry

import android.content.Context
import android.view.View
import dev.flexui.FlexComponentFactory
import dev.flexui.render.FlexRenderer
import dev.flexui.render.components.*
import dev.flexui.schema.FlexNode
import dev.flexui.schema.FlexTheme
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for managing FlexUI component factories (built-in and custom)
 */
class ComponentRegistry private constructor() {
    
    private val factories = ConcurrentHashMap<String, FlexComponentFactory>()
    
    init {
        registerBuiltInComponents()
    }
    
    /**
     * Register a custom component factory
     */
    fun registerComponent(type: String, factory: FlexComponentFactory) {
        factories[type] = factory
    }
    
    /**
     * Unregister a component factory
     */
    fun unregisterComponent(type: String) {
        factories.remove(type)
    }
    
    /**
     * Create a view for the given node type
     */
    fun createView(
        context: Context, 
        node: FlexNode, 
        theme: FlexTheme, 
        renderer: FlexRenderer
    ): View? {
        val factory = factories[node.type]
        return factory?.create(context, node.getProps(), theme)
    }
    
    /**
     * Check if a component type is registered
     */
    fun isRegistered(type: String): Boolean {
        return factories.containsKey(type)
    }
    
    /**
     * Get all registered component types
     */
    fun getRegisteredTypes(): Set<String> {
        return factories.keys.toSet()
    }
    
    /**
     * Clear all custom components (keeps built-in components)
     */
    fun clearCustomComponents() {
        val builtInTypes = setOf(
            "container", "row", "column", "text", "image", "button",
            "scroll", "list", "grid", "card", "input", "toggle",
            "divider", "spacer"
        )
        
        val keysToRemove = factories.keys.filter { it !in builtInTypes }
        keysToRemove.forEach { factories.remove(it) }
    }
    
    /**
     * Register all built-in component renderers
     */
    private fun registerBuiltInComponents() {
        factories["container"] = ContainerRenderer()
        factories["row"] = RowRenderer()
        factories["column"] = ColumnRenderer()
        factories["text"] = TextRenderer()
        factories["image"] = ImageRenderer()
        factories["button"] = ButtonRenderer()
        factories["scroll"] = ScrollRenderer()
        factories["list"] = ListRenderer()
        factories["grid"] = GridRenderer()
        factories["card"] = CardRenderer()
        factories["input"] = InputRenderer()
        factories["toggle"] = ToggleRenderer()
        factories["divider"] = DividerRenderer()
        factories["spacer"] = SpacerRenderer()
    }
    
    /**
     * Get factory for a specific component type
     */
    fun getFactory(type: String): FlexComponentFactory? {
        return factories[type]
    }
    
    /**
     * Get component metadata
     */
    fun getComponentInfo(): Map<String, ComponentInfo> {
        return factories.keys.associateWith { type ->
            ComponentInfo(
                type = type,
                isBuiltIn = isBuiltInComponent(type),
                factory = factories[type]!!
            )
        }
    }
    
    private fun isBuiltInComponent(type: String): Boolean {
        return type in setOf(
            "container", "row", "column", "text", "image", "button",
            "scroll", "list", "grid", "card", "input", "toggle",
            "divider", "spacer"
        )
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ComponentRegistry? = null
        
        /**
         * Get singleton instance
         */
        @JvmStatic
        fun getInstance(): ComponentRegistry {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ComponentRegistry().also { INSTANCE = it }
            }
        }
        
        /**
         * Register component using singleton instance
         */
        @JvmStatic
        fun registerComponent(type: String, factory: FlexComponentFactory) {
            getInstance().registerComponent(type, factory)
        }
        
        /**
         * Create view using singleton instance
         */
        @JvmStatic
        fun createView(
            context: Context, 
            node: FlexNode, 
            theme: FlexTheme, 
            renderer: FlexRenderer
        ): View? {
            return getInstance().createView(context, node, theme, renderer)
        }
        
        /**
         * Check if component is registered using singleton instance
         */
        @JvmStatic
        fun isRegistered(type: String): Boolean {
            return getInstance().isRegistered(type)
        }
    }
}

/**
 * Component information
 */
data class ComponentInfo(
    val type: String,
    val isBuiltIn: Boolean,
    val factory: FlexComponentFactory
)