package dev.flexui.config

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles caching of FlexUI configurations with both memory (LRU) and disk storage
 */
class ConfigCache private constructor(
    private val context: Context,
    private val maxMemoryEntries: Int = 50,
    private val defaultTtlMs: Long = 3600_000 // 1 hour
) {
    
    private val memoryCache = LruCache<String, CacheEntry>(maxMemoryEntries)
    private val diskCache: SharedPreferences
    private val cacheDir: File
    
    init {
        diskCache = context.getSharedPreferences("flexui_cache", Context.MODE_PRIVATE)
        cacheDir = File(context.cacheDir, "flexui")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Get cached configuration if available and not expired
     */
    fun get(screenId: String): CachedConfig? {
        // Check memory cache first
        val memoryEntry = memoryCache.get(screenId)
        if (memoryEntry != null && !memoryEntry.isExpired()) {
            return CachedConfig(memoryEntry.json, memoryEntry.timestamp, false)
        }
        
        // Check disk cache
        val diskEntry = getDiskEntry(screenId)
        if (diskEntry != null && !diskEntry.isExpired()) {
            // Put back in memory cache
            memoryCache.put(screenId, diskEntry)
            return CachedConfig(diskEntry.json, diskEntry.timestamp, true)
        }
        
        return null
    }
    
    /**
     * Put configuration in cache
     */
    fun put(screenId: String, json: String, ttlMs: Long = defaultTtlMs) {
        val entry = CacheEntry(
            json = json,
            timestamp = System.currentTimeMillis(),
            ttlMs = ttlMs
        )
        
        // Store in memory cache
        memoryCache.put(screenId, entry)
        
        // Store in disk cache
        putDiskEntry(screenId, entry)
    }
    
    /**
     * Check if cache has entry (even if expired)
     */
    fun has(screenId: String): Boolean {
        return memoryCache.get(screenId) != null || hasDiskEntry(screenId)
    }
    
    /**
     * Check if cache has valid (non-expired) entry
     */
    fun hasValid(screenId: String): Boolean {
        return get(screenId) != null
    }
    
    /**
     * Remove entry from cache
     */
    fun remove(screenId: String) {
        memoryCache.remove(screenId)
        removeDiskEntry(screenId)
    }
    
    /**
     * Clear all cached entries
     */
    fun clear() {
        memoryCache.clear()
        
        // Clear disk cache
        diskCache.edit().clear().apply()
        cacheDir.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * Clear expired entries
     */
    fun clearExpired() {
        val currentTime = System.currentTimeMillis()
        
        // Clear expired memory entries
        val memoryKeysToRemove = mutableListOf<String>()
        memoryCache.snapshot().forEach { (key, entry) ->
            if (entry.isExpired(currentTime)) {
                memoryKeysToRemove.add(key)
            }
        }
        memoryKeysToRemove.forEach { memoryCache.remove(it) }
        
        // Clear expired disk entries
        val diskKeysToRemove = mutableListOf<String>()
        diskCache.all.forEach { (key, _) ->
            if (key.startsWith("timestamp_")) {
                val screenId = key.substring("timestamp_".length)
                val diskEntry = getDiskEntry(screenId)
                if (diskEntry?.isExpired(currentTime) == true) {
                    diskKeysToRemove.add(screenId)
                }
            }
        }
        diskKeysToRemove.forEach { removeDiskEntry(it) }
    }
    
    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats {
        val memorySize = memoryCache.size()
        val diskSize = diskCache.all.keys.count { it.startsWith("json_") }
        val totalSize = cacheDir.listFiles()?.sumOf { it.length() } ?: 0
        
        return CacheStats(
            memoryEntries = memorySize,
            diskEntries = diskSize,
            totalSizeBytes = totalSize
        )
    }
    
    private fun getDiskEntry(screenId: String): CacheEntry? {
        val jsonKey = "json_$screenId"
        val timestampKey = "timestamp_$screenId"
        val ttlKey = "ttl_$screenId"
        
        val json = diskCache.getString(jsonKey, null) ?: return null
        val timestamp = diskCache.getLong(timestampKey, 0)
        val ttl = diskCache.getLong(ttlKey, defaultTtlMs)
        
        if (timestamp == 0L) return null
        
        return CacheEntry(json, timestamp, ttl)
    }
    
    private fun putDiskEntry(screenId: String, entry: CacheEntry) {
        val jsonKey = "json_$screenId"
        val timestampKey = "timestamp_$screenId"
        val ttlKey = "ttl_$screenId"
        
        diskCache.edit()
            .putString(jsonKey, entry.json)
            .putLong(timestampKey, entry.timestamp)
            .putLong(ttlKey, entry.ttlMs)
            .apply()
    }
    
    private fun hasDiskEntry(screenId: String): Boolean {
        return diskCache.contains("json_$screenId")
    }
    
    private fun removeDiskEntry(screenId: String) {
        val jsonKey = "json_$screenId"
        val timestampKey = "timestamp_$screenId"
        val ttlKey = "ttl_$screenId"
        
        diskCache.edit()
            .remove(jsonKey)
            .remove(timestampKey)
            .remove(ttlKey)
            .apply()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ConfigCache? = null
        
        /**
         * Get singleton instance
         */
        @JvmStatic
        fun getInstance(context: Context): ConfigCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigCache(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        /**
         * Create new instance with custom settings
         */
        @JvmStatic
        fun create(
            context: Context,
            maxMemoryEntries: Int = 50,
            defaultTtlMs: Long = 3600_000
        ): ConfigCache {
            return ConfigCache(context.applicationContext, maxMemoryEntries, defaultTtlMs)
        }
    }
}

/**
 * Cache entry with expiration
 */
private data class CacheEntry(
    val json: String,
    val timestamp: Long,
    val ttlMs: Long
) {
    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - timestamp) > ttlMs
    }
}

/**
 * Cached configuration data
 */
data class CachedConfig(
    val json: String,
    val timestamp: Long,
    val fromDisk: Boolean
)

/**
 * Cache statistics
 */
data class CacheStats(
    val memoryEntries: Int,
    val diskEntries: Int,
    val totalSizeBytes: Long
)

/**
 * Simple LRU cache implementation
 */
private class LruCache<K, V>(private val maxSize: Int) {
    private val cache = object : LinkedHashMap<K, V>(maxSize + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }
    
    @Synchronized
    fun get(key: K): V? = cache[key]
    
    @Synchronized
    fun put(key: K, value: V): V? = cache.put(key, value)
    
    @Synchronized
    fun remove(key: K): V? = cache.remove(key)
    
    @Synchronized
    fun clear() = cache.clear()
    
    @Synchronized
    fun size(): Int = cache.size
    
    @Synchronized
    fun snapshot(): Map<K, V> = cache.toMap()
}