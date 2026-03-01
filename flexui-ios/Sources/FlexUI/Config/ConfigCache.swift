import Foundation

/// Manages caching of FlexUI configurations in memory and on disk
public final class ConfigCache {
    
    // MARK: - Singleton
    public static let shared = ConfigCache()
    
    // MARK: - Properties
    
    private let memoryCache = NSCache<NSString, CacheEntry>()
    private let diskCacheDirectory: URL
    private let cacheTTL: TimeInterval = 3600 // 1 hour default
    private let maxDiskCacheSize: Int = 50 * 1024 * 1024 // 50MB
    private let cacheQueue = DispatchQueue(label: "flexui.cache", qos: .utility)
    
    // MARK: - Initialization
    
    private init() {
        // Setup memory cache
        memoryCache.countLimit = 50 // Maximum 50 configs in memory
        memoryCache.totalCostLimit = 10 * 1024 * 1024 // 10MB memory limit
        
        // Setup disk cache directory
        let cacheDir = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first!
        diskCacheDirectory = cacheDir.appendingPathComponent("FlexUI", isDirectory: true)
        
        // Create cache directory if needed
        createCacheDirectoryIfNeeded()
        
        // Clean up old cache entries on init
        cleanupExpiredEntries()
    }
    
    // MARK: - Public Methods
    
    /// Store configuration in cache
    public func put(screenId: String, json: String) {
        let entry = CacheEntry(
            json: json,
            timestamp: Date(),
            ttl: cacheTTL,
            cost: json.utf8.count
        )
        
        // Store in memory cache
        memoryCache.setObject(entry, forKey: screenId as NSString, cost: entry.cost)
        
        // Store in disk cache asynchronously
        cacheQueue.async { [weak self] in
            self?.saveToDisk(screenId: screenId, entry: entry)
        }
    }
    
    /// Retrieve configuration from cache
    public func get(screenId: String) -> CacheEntry? {
        // Try memory cache first
        if let entry = memoryCache.object(forKey: screenId as NSString) {
            if !entry.isExpired {
                return entry
            } else {
                // Remove expired entry from memory
                memoryCache.removeObject(forKey: screenId as NSString)
            }
        }
        
        // Try disk cache
        return loadFromDisk(screenId: screenId)
    }
    
    /// Remove specific configuration from cache
    public func remove(screenId: String) {
        // Remove from memory
        memoryCache.removeObject(forKey: screenId as NSString)
        
        // Remove from disk asynchronously
        cacheQueue.async { [weak self] in
            self?.removeFromDisk(screenId: screenId)
        }
    }
    
    /// Clear all cached configurations
    public func clear() {
        // Clear memory cache
        memoryCache.removeAllObjects()
        
        // Clear disk cache asynchronously
        cacheQueue.async { [weak self] in
            self?.clearDiskCache()
        }
    }
    
    /// Get cache statistics
    public func getCacheStats() -> CacheStats {
        let memoryCount = memoryCache.name // NSCache doesn't expose count directly
        let diskCount = getDiskCacheCount()
        let diskSize = getDiskCacheSize()
        
        return CacheStats(
            memoryEntries: 0, // NSCache doesn't expose this
            diskEntries: diskCount,
            diskSizeBytes: diskSize
        )
    }
    
    // MARK: - Private Methods
    
    /// Create cache directory if it doesn't exist
    private func createCacheDirectoryIfNeeded() {
        do {
            try FileManager.default.createDirectory(
                at: diskCacheDirectory,
                withIntermediateDirectories: true,
                attributes: nil
            )
        } catch {
            print("FlexUI: Failed to create cache directory: \\(error)")
        }
    }
    
    /// Save cache entry to disk
    private func saveToDisk(screenId: String, entry: CacheEntry) {
        let fileURL = diskCacheURL(for: screenId)
        
        do {
            let data = try JSONEncoder().encode(entry)
            try data.write(to: fileURL)
        } catch {
            print("FlexUI: Failed to save cache entry to disk: \\(error)")
        }
        
        // Cleanup old entries if cache is getting too large
        cleanupDiskCacheIfNeeded()
    }
    
    /// Load cache entry from disk
    private func loadFromDisk(screenId: String) -> CacheEntry? {
        let fileURL = diskCacheURL(for: screenId)
        
        guard FileManager.default.fileExists(atPath: fileURL.path) else {
            return nil
        }
        
        do {
            let data = try Data(contentsOf: fileURL)
            let entry = try JSONDecoder().decode(CacheEntry.self, from: data)
            
            if !entry.isExpired {
                // Put back in memory cache for faster access
                memoryCache.setObject(entry, forKey: screenId as NSString, cost: entry.cost)
                return entry
            } else {
                // Remove expired file
                try? FileManager.default.removeItem(at: fileURL)
                return nil
            }
        } catch {
            // Remove corrupted file
            try? FileManager.default.removeItem(at: fileURL)
            return nil
        }
    }
    
    /// Remove cache entry from disk
    private func removeFromDisk(screenId: String) {
        let fileURL = diskCacheURL(for: screenId)
        try? FileManager.default.removeItem(at: fileURL)
    }
    
    /// Clear all disk cache
    private func clearDiskCache() {
        do {
            let fileURLs = try FileManager.default.contentsOfDirectory(
                at: diskCacheDirectory,
                includingPropertiesForKeys: nil
            )
            
            for fileURL in fileURLs {
                try FileManager.default.removeItem(at: fileURL)
            }
        } catch {
            print("FlexUI: Failed to clear disk cache: \\(error)")
        }
    }
    
    /// Get disk cache file URL for screen ID
    private func diskCacheURL(for screenId: String) -> URL {
        let safeScreenId = screenId.replacingOccurrences(of: "/", with: "_")
        return diskCacheDirectory.appendingPathComponent("\\(safeScreenId).json")
    }
    
    /// Cleanup expired entries from disk cache
    private func cleanupExpiredEntries() {
        cacheQueue.async { [weak self] in
            self?.performDiskCleanup()
        }
    }
    
    /// Perform actual disk cleanup
    private func performDiskCleanup() {
        do {
            let fileURLs = try FileManager.default.contentsOfDirectory(
                at: diskCacheDirectory,
                includingPropertiesForKeys: [.contentModificationDateKey]
            )
            
            for fileURL in fileURLs {
                // Try to load and check if expired
                if let data = try? Data(contentsOf: fileURL),
                   let entry = try? JSONDecoder().decode(CacheEntry.self, from: data),
                   entry.isExpired {
                    try FileManager.default.removeItem(at: fileURL)
                }
            }
        } catch {
            print("FlexUI: Failed to cleanup expired entries: \\(error)")
        }
    }
    
    /// Cleanup disk cache if it exceeds size limit
    private func cleanupDiskCacheIfNeeded() {
        let currentSize = getDiskCacheSize()
        
        if currentSize > maxDiskCacheSize {
            // Remove oldest files first
            do {
                let fileURLs = try FileManager.default.contentsOfDirectory(
                    at: diskCacheDirectory,
                    includingPropertiesForKeys: [.contentModificationDateKey]
                )
                
                let sortedURLs = fileURLs.sorted { url1, url2 in
                    let date1 = try? url1.resourceValues(forKeys: [.contentModificationDateKey]).contentModificationDate
                    let date2 = try? url2.resourceValues(forKeys: [.contentModificationDateKey]).contentModificationDate
                    return date1?.compare(date2 ?? Date()) == .orderedAscending
                }
                
                var removedSize = 0
                for fileURL in sortedURLs {
                    if removedSize >= (currentSize - maxDiskCacheSize / 2) {
                        break
                    }
                    
                    if let attributes = try? FileManager.default.attributesOfItem(atPath: fileURL.path),
                       let fileSize = attributes[.size] as? Int {
                        removedSize += fileSize
                        try FileManager.default.removeItem(at: fileURL)
                    }
                }
            } catch {
                print("FlexUI: Failed to cleanup disk cache: \\(error)")
            }
        }
    }
    
    /// Get number of entries in disk cache
    private func getDiskCacheCount() -> Int {
        do {
            let fileURLs = try FileManager.default.contentsOfDirectory(at: diskCacheDirectory, includingPropertiesForKeys: nil)
            return fileURLs.count
        } catch {
            return 0
        }
    }
    
    /// Get total size of disk cache in bytes
    private func getDiskCacheSize() -> Int {
        do {
            let fileURLs = try FileManager.default.contentsOfDirectory(at: diskCacheDirectory, includingPropertiesForKeys: [.fileSizeKey])
            
            return fileURLs.reduce(0) { totalSize, fileURL in
                let fileSize = (try? fileURL.resourceValues(forKeys: [.fileSizeKey]))?.fileSize ?? 0
                return totalSize + fileSize
            }
        } catch {
            return 0
        }
    }
}

// MARK: - CacheEntry

/// Represents a cached configuration entry
public final class CacheEntry: NSObject, Codable {
    let json: String
    let timestamp: Date
    let ttl: TimeInterval
    let cost: Int
    
    init(json: String, timestamp: Date, ttl: TimeInterval, cost: Int) {
        self.json = json
        self.timestamp = timestamp
        self.ttl = ttl
        self.cost = cost
    }
    
    /// Check if the cache entry has expired
    var isExpired: Bool {
        return Date().timeIntervalSince(timestamp) > ttl
    }
}

// MARK: - CacheStats

/// Statistics about the current cache state
public struct CacheStats {
    public let memoryEntries: Int
    public let diskEntries: Int
    public let diskSizeBytes: Int
    
    public var diskSizeMB: Double {
        return Double(diskSizeBytes) / (1024 * 1024)
    }
}