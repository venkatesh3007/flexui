package dev.flexui

import dev.flexui.schema.FlexTheme

/**
 * Configuration class for FlexUI SDK initialization
 */
class FlexConfig private constructor(
    val baseUrl: String,
    val cachePolicy: CachePolicy,
    val cacheTtlMs: Long,
    val defaultTheme: FlexTheme?,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val enableDebugLogging: Boolean
) {
    
    /**
     * Builder pattern for creating FlexConfig instances
     */
    class Builder(private val baseUrl: String) {
        private var cachePolicy: CachePolicy = CachePolicy.CACHE_FIRST
        private var cacheTtlMs: Long = 3600_000 // 1 hour
        private var defaultTheme: FlexTheme? = null
        private var connectTimeoutMs: Long = 10_000 // 10 seconds
        private var readTimeoutMs: Long = 30_000 // 30 seconds
        private var enableDebugLogging: Boolean = false
        
        /**
         * Set cache policy
         */
        fun cachePolicy(policy: CachePolicy): Builder {
            this.cachePolicy = policy
            return this
        }
        
        /**
         * Set cache TTL in milliseconds
         */
        fun cacheTtlMs(ttl: Long): Builder {
            this.cacheTtlMs = ttl
            return this
        }
        
        /**
         * Set cache TTL in seconds (convenience method)
         */
        fun cacheTtlSeconds(ttl: Long): Builder {
            this.cacheTtlMs = ttl * 1000
            return this
        }
        
        /**
         * Set cache TTL in minutes (convenience method)
         */
        fun cacheTtlMinutes(ttl: Long): Builder {
            this.cacheTtlMs = ttl * 60 * 1000
            return this
        }
        
        /**
         * Set default theme
         */
        fun defaultTheme(theme: FlexTheme): Builder {
            this.defaultTheme = theme
            return this
        }
        
        /**
         * Set connection timeout in milliseconds
         */
        fun connectTimeoutMs(timeout: Long): Builder {
            this.connectTimeoutMs = timeout
            return this
        }
        
        /**
         * Set connection timeout in seconds (convenience method)
         */
        fun connectTimeoutSeconds(timeout: Long): Builder {
            this.connectTimeoutMs = timeout * 1000
            return this
        }
        
        /**
         * Set read timeout in milliseconds
         */
        fun readTimeoutMs(timeout: Long): Builder {
            this.readTimeoutMs = timeout
            return this
        }
        
        /**
         * Set read timeout in seconds (convenience method)
         */
        fun readTimeoutSeconds(timeout: Long): Builder {
            this.readTimeoutMs = timeout * 1000
            return this
        }
        
        /**
         * Enable debug logging
         */
        fun enableDebugLogging(enable: Boolean = true): Builder {
            this.enableDebugLogging = enable
            return this
        }
        
        /**
         * Build the FlexConfig instance
         */
        fun build(): FlexConfig {
            return FlexConfig(
                baseUrl = baseUrl,
                cachePolicy = cachePolicy,
                cacheTtlMs = cacheTtlMs,
                defaultTheme = defaultTheme,
                connectTimeoutMs = connectTimeoutMs,
                readTimeoutMs = readTimeoutMs,
                enableDebugLogging = enableDebugLogging
            )
        }
    }
}

/**
 * Cache policy options for FlexUI configuration fetching
 */
enum class CachePolicy {
    /** Use cache if available, fetch in background to update */
    CACHE_FIRST,
    
    /** Always fetch from network first, fallback to cache on failure */
    NETWORK_FIRST,
    
    /** Only use cache, never fetch from network (offline mode) */
    CACHE_ONLY,
    
    /** Always fetch from network, never use cache */
    NETWORK_ONLY
}