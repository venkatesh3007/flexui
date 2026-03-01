import Foundation

/// Handles parsing of FlexUI JSON configurations into typed models
public final class FlexParser {
    
    // MARK: - Configuration Parsing
    
    /// Parse a complete FlexUI configuration from JSON string
    public static func parseConfig(_ jsonString: String) throws -> FlexConfig {
        guard let data = jsonString.data(using: .utf8) else {
            throw FlexError.parseError("Invalid UTF-8 encoding")
        }
        
        return try parseConfig(data)
    }
    
    /// Parse a complete FlexUI configuration from JSON data
    public static func parseConfig(_ data: Data) throws -> FlexConfig {
        do {
            let decoder = JSONDecoder()
            decoder.dateDecodingStrategy = .iso8601
            return try decoder.decode(FlexConfig.self, from: data)
        } catch let decodingError as DecodingError {
            throw FlexError.parseError("Failed to parse FlexUI config: \\(decodingError.localizedDescription)")
        } catch {
            throw FlexError.parseError("Failed to parse FlexUI config: \\(error.localizedDescription)")
        }
    }
    
    // MARK: - Component Parsing
    
    /// Parse a single FlexNode from JSON string
    public static func parseNode(_ jsonString: String) throws -> FlexNode {
        guard let data = jsonString.data(using: .utf8) else {
            throw FlexError.parseError("Invalid UTF-8 encoding")
        }
        
        return try parseNode(data)
    }
    
    /// Parse a single FlexNode from JSON data
    public static func parseNode(_ data: Data) throws -> FlexNode {
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(FlexNode.self, from: data)
        } catch let decodingError as DecodingError {
            throw FlexError.parseError("Failed to parse FlexNode: \\(decodingError.localizedDescription)")
        } catch {
            throw FlexError.parseError("Failed to parse FlexNode: \\(error.localizedDescription)")
        }
    }
    
    // MARK: - Theme Parsing
    
    /// Parse a FlexTheme from JSON string
    public static func parseTheme(_ jsonString: String) throws -> FlexTheme {
        guard let data = jsonString.data(using: .utf8) else {
            throw FlexError.parseError("Invalid UTF-8 encoding")
        }
        
        return try parseTheme(data)
    }
    
    /// Parse a FlexTheme from JSON data
    public static func parseTheme(_ data: Data) throws -> FlexTheme {
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(FlexTheme.self, from: data)
        } catch let decodingError as DecodingError {
            throw FlexError.parseError("Failed to parse FlexTheme: \\(decodingError.localizedDescription)")
        } catch {
            throw FlexError.parseError("Failed to parse FlexTheme: \\(error.localizedDescription)")
        }
    }
    
    // MARK: - Validation
    
    /// Validate a JSON string is well-formed
    public static func validateJSON(_ jsonString: String) -> Bool {
        guard let data = jsonString.data(using: .utf8) else {
            return false
        }
        
        do {
            _ = try JSONSerialization.jsonObject(with: data, options: [])
            return true
        } catch {
            return false
        }
    }
    
    /// Validate a FlexUI configuration has required fields
    public static func validateConfig(_ config: FlexConfig) throws {
        // Check version
        if config.version.isEmpty {
            throw FlexError.parseError("Configuration missing version")
        }
        
        // Check screenId
        if config.screenId.isEmpty {
            throw FlexError.parseError("Configuration missing screenId")
        }
        
        // Validate root node
        try validateNode(config.root)
    }
    
    /// Validate a FlexNode recursively
    public static func validateNode(_ node: FlexNode) throws {
        // Check type
        if node.type.isEmpty {
            throw FlexError.parseError("Node missing type")
        }
        
        // Validate children recursively
        if let children = node.children {
            for child in children {
                try validateNode(child)
            }
        }
        
        // Validate action if present
        if let action = node.action {
            try action.validate()
        }
    }
    
    // MARK: - Serialization
    
    /// Convert FlexConfig to JSON string
    public static func serialize(_ config: FlexConfig) throws -> String {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        encoder.dateEncodingStrategy = .iso8601
        
        let data = try encoder.encode(config)
        guard let jsonString = String(data: data, encoding: .utf8) else {
            throw FlexError.parseError("Failed to convert to UTF-8 string")
        }
        
        return jsonString
    }
    
    /// Convert FlexNode to JSON string
    public static func serialize(_ node: FlexNode) throws -> String {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        
        let data = try encoder.encode(node)
        guard let jsonString = String(data: data, encoding: .utf8) else {
            throw FlexError.parseError("Failed to convert to UTF-8 string")
        }
        
        return jsonString
    }
    
    /// Convert FlexTheme to JSON string
    public static func serialize(_ theme: FlexTheme) throws -> String {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        
        let data = try encoder.encode(theme)
        guard let jsonString = String(data: data, encoding: .utf8) else {
            throw FlexError.parseError("Failed to convert to UTF-8 string")
        }
        
        return jsonString
    }
}

// MARK: - FlexConfig Model

/// Root configuration model for FlexUI
public struct FlexConfig: Codable {
    
    /// Configuration format version
    public let version: String
    
    /// Unique identifier for this screen/configuration
    public let screenId: String
    
    /// Theme configuration
    public let theme: FlexTheme?
    
    /// Root UI component
    public let root: FlexNode
    
    /// Global actions available to all components
    public let actions: [String: FlexAction]?
    
    /// Metadata about the configuration
    public let metadata: [String: AnyCodable]?
    
    public init(
        version: String,
        screenId: String,
        theme: FlexTheme? = nil,
        root: FlexNode,
        actions: [String: FlexAction]? = nil,
        metadata: [String: Any]? = nil
    ) {
        self.version = version
        self.screenId = screenId
        self.theme = theme
        self.root = root
        self.actions = actions
        self.metadata = metadata?.mapValues { AnyCodable($0) }
    }
    
    /// Get metadata value by key
    public func getMetadata(_ key: String) -> Any? {
        return metadata?[key]?.value
    }
    
    /// Get global action by name
    public func getAction(_ name: String) -> FlexAction? {
        return actions?[name]
    }
}

// MARK: - Error Handling

/// FlexUI-specific errors
public enum FlexError: Error, LocalizedError {
    case parseError(String)
    case renderError(String, underlyingError: Error? = nil)
    case networkError(String)
    case validationError(String)
    case configurationError(String)
    
    public var errorDescription: String? {
        switch self {
        case .parseError(let message):
            return "Parse error: \\(message)"
        case .renderError(let message, let underlyingError):
            if let underlyingError = underlyingError {
                return "Render error: \\(message) - \\(underlyingError.localizedDescription)"
            } else {
                return "Render error: \\(message)"
            }
        case .networkError(let message):
            return "Network error: \\(message)"
        case .validationError(let message):
            return "Validation error: \\(message)"
        case .configurationError(let message):
            return "Configuration error: \\(message)"
        }
    }
}

// MARK: - Parser Utilities

public extension FlexParser {
    
    /// Parse JSON with helpful error messages
    static func parseJSONObject(from string: String) throws -> [String: Any] {
        guard let data = string.data(using: .utf8) else {
            throw FlexError.parseError("Invalid UTF-8 encoding")
        }
        
        do {
            guard let jsonObject = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                throw FlexError.parseError("JSON root is not an object")
            }
            return jsonObject
        } catch {
            throw FlexError.parseError("Invalid JSON: \\(error.localizedDescription)")
        }
    }
    
    /// Parse JSON array
    static func parseJSONArray(from string: String) throws -> [Any] {
        guard let data = string.data(using: .utf8) else {
            throw FlexError.parseError("Invalid UTF-8 encoding")
        }
        
        do {
            guard let jsonArray = try JSONSerialization.jsonObject(with: data) as? [Any] else {
                throw FlexError.parseError("JSON root is not an array")
            }
            return jsonArray
        } catch {
            throw FlexError.parseError("Invalid JSON: \\(error.localizedDescription)")
        }
    }
    
    /// Create a minimal test configuration
    static func createTestConfig() -> FlexConfig {
        let textNode = FlexNode(
            type: "text",
            props: [
                "content": "Hello, FlexUI!",
                "textAlign": "center"
            ]
        )
        
        let containerNode = FlexNode(
            type: "container",
            style: [
                "padding": 16,
                "backgroundColor": "#FFFFFF"
            ],
            children: [textNode]
        )
        
        return FlexConfig(
            version: "1.0",
            screenId: "test_screen",
            root: containerNode
        )
    }
}

// MARK: - JSON Schema Validation

public extension FlexParser {
    
    /// Basic schema validation for FlexUI configurations
    static func validateSchema(_ jsonObject: [String: Any]) throws {
        // Check required root fields
        guard jsonObject["version"] is String else {
            throw FlexError.validationError("Missing or invalid 'version' field")
        }
        
        guard jsonObject["screenId"] is String else {
            throw FlexError.validationError("Missing or invalid 'screenId' field")
        }
        
        guard let rootObject = jsonObject["root"] as? [String: Any] else {
            throw FlexError.validationError("Missing or invalid 'root' field")
        }
        
        // Validate root node
        try validateNodeSchema(rootObject)
    }
    
    /// Validate a node's JSON schema
    private static func validateNodeSchema(_ nodeObject: [String: Any]) throws {
        // Check type field
        guard nodeObject["type"] is String else {
            throw FlexError.validationError("Node missing or invalid 'type' field")
        }
        
        // Validate children if present
        if let children = nodeObject["children"] as? [[String: Any]] {
            for child in children {
                try validateNodeSchema(child)
            }
        }
        
        // Validate action if present
        if let action = nodeObject["action"] as? [String: Any] {
            try validateActionSchema(action)
        }
        
        // Validate condition if present
        if let condition = nodeObject["condition"] as? [String: Any] {
            try validateConditionSchema(condition)
        }
    }
    
    /// Validate an action's JSON schema
    private static func validateActionSchema(_ actionObject: [String: Any]) throws {
        guard actionObject["type"] is String else {
            throw FlexError.validationError("Action missing or invalid 'type' field")
        }
    }
    
    /// Validate a condition's JSON schema
    private static func validateConditionSchema(_ conditionObject: [String: Any]) throws {
        guard conditionObject["if"] is String else {
            throw FlexError.validationError("Condition missing or invalid 'if' field")
        }
    }
}