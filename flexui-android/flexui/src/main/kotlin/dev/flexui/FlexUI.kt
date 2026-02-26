package dev.flexui

import android.content.Context
import android.view.View
import dev.flexui.actions.ActionDispatcher
import dev.flexui.config.ConfigCache
import dev.flexui.config.ConfigFetcher
import dev.flexui.registry.ComponentRegistry
import dev.flexui.render.FlexRenderer
import dev.flexui.schema.FlexNode
import dev.flexui.schema.FlexParser
import dev.flexui.schema.FlexTheme
import dev.flexui.theme.ThemeDefaults
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Main entry point for FlexUI SDK.
 * Provides Java-compatible static API for initializing and rendering FlexUI components.
 */
object FlexUI {
    
    private var isInitialized = false
    private var configFetcher: ConfigFetcher? = null
    private var configCache: ConfigCache? = null
    private var defaultTheme: FlexTheme = ThemeDefaults.getDefaultTheme()
    private var backgroundExecutor: Executor = Executors.newFixedThreadPool(4)
    
    /**
     * Initialize FlexUI with base URL
     */
    @JvmStatic
    fun init(context: Context, baseUrl: String) {
        val config = FlexConfig.Builder(baseUrl).build()
        init(context, config)
    }
    
    /**
     * Initialize FlexUI with configuration
     */
    @JvmStatic
    fun init(context: Context, config: FlexConfig) {
        configFetcher = ConfigFetcher.create(
            config.baseUrl,
            config.connectTimeoutMs,
            config.readTimeoutMs
        )
        configCache = ConfigCache.getInstance(context)
        
        if (config.defaultTheme != null) {
            defaultTheme = ThemeDefaults.mergeWithDefaults(config.defaultTheme)
        }
        
        isInitialized = true
    }
    
    /**
     * Register a custom component factory
     */
    @JvmStatic
    fun registerComponent(type: String, factory: FlexComponentFactory) {
        ComponentRegistry.getInstance().registerComponent(type, factory)
    }
    
    /**
     * Register an action handler
     */
    @JvmStatic
    fun onAction(actionType: String, handler: FlexActionHandler) {
        ActionDispatcher.getInstance().registerHandler(actionType, handler)
    }
    
    /**
     * Register an event handler for callback actions
     */
    @JvmStatic
    fun onEvent(eventName: String, handler: FlexActionHandler) {
        ActionDispatcher.getInstance().registerEventHandler(eventName, handler)
    }
    
    /**
     * Render a screen synchronously
     */
    @JvmStatic
    fun render(
        context: Context, 
        screenId: String, 
        data: Map<String, Any> = emptyMap()
    ): View? {
        ensureInitialized()
        
        return try {
            val configJson = fetchConfigSync(screenId)
            if (configJson != null) {
                renderFromJson(context, configJson, data)
            } else {
                null
            }
        } catch (e: Exception) {
            throw FlexError.renderError("Failed to render screen: $screenId", e)
        }
    }
    
    /**
     * Render a screen asynchronously
     */
    @JvmStatic
    fun renderAsync(
        context: Context,
        screenId: String,
        data: Map<String, Any> = emptyMap(),
        callback: FlexRenderCallback
    ) {
        ensureInitialized()
        
        backgroundExecutor.execute {
            try {
                val view = render(context, screenId, data)
                if (view != null) {
                    // Post back to main thread
                    if (context is android.app.Activity) {
                        context.runOnUiThread {
                            callback.onSuccess(view)
                        }
                    } else {
                        callback.onSuccess(view)
                    }
                } else {
                    val error = FlexError.renderError("No view rendered for screen: $screenId")
                    if (context is android.app.Activity) {
                        context.runOnUiThread {
                            callback.onError(error)
                        }
                    } else {
                        callback.onError(error)
                    }
                }
            } catch (e: Exception) {
                val error = if (e is FlexError) e else FlexError.renderError("Render failed", e)
                if (context is android.app.Activity) {
                    context.runOnUiThread {
                        callback.onError(error)
                    }
                } else {
                    callback.onError(error)
                }
            }
        }
    }
    
    /**
     * Render from JSON string directly
     */
    @JvmStatic
    fun renderFromJson(
        context: Context,
        jsonConfig: String,
        data: Map<String, Any> = emptyMap()
    ): View? {
        return try {
            val config = FlexParser.parseConfig(jsonConfig)
            val theme = ThemeDefaults.mergeWithDefaults(config.theme)
            FlexRenderer.getInstance().render(context, config.root, theme, data)
        } catch (e: Exception) {
            throw FlexError.parseError("Failed to parse JSON config", e)
        }
    }
    
    /**
     * Refresh configuration cache for a screen
     */
    @JvmStatic
    fun refreshConfig(screenId: String) {
        ensureInitialized()
        configCache?.remove(screenId)
    }
    
    /**
     * Clear all cached configurations
     */
    @JvmStatic
    fun clearCache() {
        configCache?.clear()
    }
    
    /**
     * Check if FlexUI is initialized
     */
    @JvmStatic
    fun isInitialized(): Boolean {
        return isInitialized
    }
    
    /**
     * Get current default theme
     */
    @JvmStatic
    fun getDefaultTheme(): FlexTheme {
        return defaultTheme
    }
    
    /**
     * Set default theme
     */
    @JvmStatic
    fun setDefaultTheme(theme: FlexTheme) {
        defaultTheme = ThemeDefaults.mergeWithDefaults(theme)
    }
    
    /**
     * Get registered component types
     */
    @JvmStatic
    fun getRegisteredComponents(): Set<String> {
        return ComponentRegistry.getInstance().getRegisteredTypes()
    }
    
    /**
     * Check if a component type is registered
     */
    @JvmStatic
    fun isComponentRegistered(type: String): Boolean {
        return ComponentRegistry.getInstance().isRegistered(type)
    }
    
    private fun ensureInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("FlexUI not initialized. Call FlexUI.init() first.")
        }
    }
    
    private fun fetchConfigSync(screenId: String): String? {
        val cache = configCache ?: return null
        val fetcher = configFetcher ?: return null
        
        // Try cache first
        val cached = cache.get(screenId)
        if (cached != null) {
            return cached.json
        }
        
        // Fetch from network
        val result = fetcher.fetchConfigSync(screenId)
        if (result.isSuccess()) {
            val json = result.getJsonOrNull()
            if (json != null) {
                cache.put(screenId, json)
                return json
            }
        }
        
        return null
    }
}