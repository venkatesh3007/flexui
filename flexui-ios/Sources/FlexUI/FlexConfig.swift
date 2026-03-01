import Foundation

/// Configuration object for FlexUI initialization
public struct FlexConfig {
    
    // MARK: - Properties
    
    /// Base URL for fetching configurations
    public let baseURL: String
    
    /// Cache policy for configuration fetching
    public let cachePolicy: CachePolicy
    
    /// Cache time-to-live in seconds
    public let cacheTTL: TimeInterval
    
    /// Default theme to merge with server-provided themes
    public let defaultTheme: FlexTheme?
    
    /// Connection timeout for network requests
    public let connectTimeout: TimeInterval
    
    /// Read timeout for network requests
    public let readTimeout: TimeInterval
    
    // MARK: - Initializers
    
    /// Initialize with base URL and default settings
    public init(baseURL: String) {
        self.baseURL = baseURL
        self.cachePolicy = .cacheFirst
        self.cacheTTL = 3600 // 1 hour
        self.defaultTheme = nil
        self.connectTimeout = 10
        self.readTimeout = 30
    }
    
    /// Initialize with all configuration options
    public init(
        baseURL: String,
        cachePolicy: CachePolicy = .cacheFirst,
        cacheTTL: TimeInterval = 3600,
        defaultTheme: FlexTheme? = nil,
        connectTimeout: TimeInterval = 10,
        readTimeout: TimeInterval = 30
    ) {
        self.baseURL = baseURL
        self.cachePolicy = cachePolicy
        self.cacheTTL = cacheTTL
        self.defaultTheme = defaultTheme
        self.connectTimeout = connectTimeout
        self.readTimeout = readTimeout
    }
}

// MARK: - Cache Policy

/// Defines how FlexUI handles configuration caching
public enum CachePolicy {
    /// Use cache if available, fetch in background to update
    case cacheFirst
    
    /// Always fetch from network first, fallback to cache on failure
    case networkFirst
    
    /// Never fetch from network, only use cached configurations
    case cacheOnly
    
    /// Never use cache, always fetch fresh from network
    case networkOnly
}

// MARK: - Convenience Builder

public extension FlexConfig {
    
    /// Builder pattern for creating FlexConfig with optional parameters
    struct Builder {
        private var baseURL: String
        private var cachePolicy: CachePolicy = .cacheFirst
        private var cacheTTL: TimeInterval = 3600
        private var defaultTheme: FlexTheme?
        private var connectTimeout: TimeInterval = 10
        private var readTimeout: TimeInterval = 30
        
        /// Initialize builder with base URL
        public init(baseURL: String) {
            self.baseURL = baseURL
        }
        
        /// Set cache policy
        public func cachePolicy(_ policy: CachePolicy) -> Builder {
            var builder = self
            builder.cachePolicy = policy
            return builder
        }
        
        /// Set cache TTL in seconds
        public func cacheTTL(_ ttl: TimeInterval) -> Builder {
            var builder = self
            builder.cacheTTL = ttl
            return builder
        }
        
        /// Set default theme
        public func defaultTheme(_ theme: FlexTheme) -> Builder {
            var builder = self
            builder.defaultTheme = theme
            return builder
        }
        
        /// Set connection timeout in seconds
        public func connectTimeout(_ timeout: TimeInterval) -> Builder {
            var builder = self
            builder.connectTimeout = timeout
            return builder
        }
        
        /// Set read timeout in seconds
        public func readTimeout(_ timeout: TimeInterval) -> Builder {
            var builder = self
            builder.readTimeout = timeout
            return builder
        }
        
        /// Build the final FlexConfig
        public func build() -> FlexConfig {
            return FlexConfig(
                baseURL: baseURL,
                cachePolicy: cachePolicy,
                cacheTTL: cacheTTL,
                defaultTheme: defaultTheme,
                connectTimeout: connectTimeout,
                readTimeout: readTimeout
            )
        }
    }
}

// MARK: - Convenience Methods

public extension FlexConfig {
    
    /// Create a FlexConfig with cache-first policy and custom TTL
    static func cacheFirst(baseURL: String, cacheTTL: TimeInterval = 3600) -> FlexConfig {
        return FlexConfig(baseURL: baseURL, cachePolicy: .cacheFirst, cacheTTL: cacheTTL)
    }
    
    /// Create a FlexConfig with network-first policy
    static func networkFirst(baseURL: String) -> FlexConfig {
        return FlexConfig(baseURL: baseURL, cachePolicy: .networkFirst)
    }
    
    /// Create a FlexConfig with cache-only policy (offline mode)
    static func cacheOnly(baseURL: String) -> FlexConfig {
        return FlexConfig(baseURL: baseURL, cachePolicy: .cacheOnly)
    }
    
    /// Create a FlexConfig with network-only policy (no caching)
    static func networkOnly(baseURL: String) -> FlexConfig {
        return FlexConfig(baseURL: baseURL, cachePolicy: .networkOnly)
    }
}