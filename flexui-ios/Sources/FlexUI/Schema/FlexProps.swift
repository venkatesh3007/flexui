import Foundation
import UIKit

/// Provides type-safe access to component properties with theme resolution
public struct FlexProps {
    
    // MARK: - Properties
    
    private let properties: [String: Any]
    
    // MARK: - Initialization
    
    public init(properties: [String: Any]) {
        self.properties = properties
    }
    
    // MARK: - Basic Value Getters
    
    /// Get string value
    public func getString(_ key: String, default defaultValue: String? = nil) -> String? {
        return (properties[key] as? String) ?? defaultValue
    }
    
    /// Get required string value (throws if missing)
    public func requireString(_ key: String) throws -> String {
        guard let value = getString(key) else {
            throw FlexPropsError.missingRequiredProperty(key, type: "String")
        }
        return value
    }
    
    /// Get integer value
    public func getInt(_ key: String, default defaultValue: Int? = nil) -> Int? {
        if let intValue = properties[key] as? Int {
            return intValue
        }
        if let floatValue = properties[key] as? Float {
            return Int(floatValue)
        }
        if let doubleValue = properties[key] as? Double {
            return Int(doubleValue)
        }
        if let stringValue = properties[key] as? String, let intValue = Int(stringValue) {
            return intValue
        }
        return defaultValue
    }
    
    /// Get float value
    public func getFloat(_ key: String, default defaultValue: Float? = nil) -> Float? {
        if let floatValue = properties[key] as? Float {
            return floatValue
        }
        if let doubleValue = properties[key] as? Double {
            return Float(doubleValue)
        }
        if let intValue = properties[key] as? Int {
            return Float(intValue)
        }
        if let stringValue = properties[key] as? String, let floatValue = Float(stringValue) {
            return floatValue
        }
        return defaultValue
    }
    
    /// Get CGFloat value
    public func getCGFloat(_ key: String, default defaultValue: CGFloat? = nil) -> CGFloat? {
        if let floatValue = getFloat(key) {
            return CGFloat(floatValue)
        }
        return defaultValue
    }
    
    /// Get boolean value
    public func getBool(_ key: String, default defaultValue: Bool? = nil) -> Bool? {
        if let boolValue = properties[key] as? Bool {
            return boolValue
        }
        if let stringValue = properties[key] as? String {
            switch stringValue.lowercased() {
            case "true", "yes", "1":
                return true
            case "false", "no", "0":
                return false
            default:
                break
            }
        }
        if let intValue = properties[key] as? Int {
            return intValue != 0
        }
        return defaultValue
    }
    
    /// Get dictionary value
    public func getDictionary(_ key: String) -> [String: Any]? {
        return properties[key] as? [String: Any]
    }
    
    /// Get array value
    public func getArray(_ key: String) -> [Any]? {
        return properties[key] as? [Any]
    }
    
    /// Get string array value
    public func getStringArray(_ key: String) -> [String]? {
        guard let array = properties[key] as? [Any] else { return nil }
        return array.compactMap { $0 as? String }
    }
    
    // MARK: - UI-Specific Getters
    
    /// Get UIColor value (supports hex strings and theme references)
    public func getColor(_ key: String, theme: FlexTheme? = nil) -> UIColor? {
        guard let colorValue = properties[key] else { return nil }
        
        if let theme = theme {
            return theme.resolveColor(colorValue)
        } else if let colorString = colorValue as? String {
            return UIColor(hex: colorString)
        }
        
        return nil
    }
    
    /// Get UIFont value
    public func getFont(_ key: String, theme: FlexTheme? = nil, defaultSize: Float = 16) -> UIFont? {
        guard let fontValue = properties[key] else { return nil }
        
        if let fontString = fontValue as? String {
            // Check if it's a theme font reference
            if let theme = theme, let fontDef = theme.getFont(fontString) {
                return fontDef.toUIFont()
            }
            
            // Try as font family name
            return UIFont(name: fontString, size: CGFloat(defaultSize)) ?? UIFont.systemFont(ofSize: CGFloat(defaultSize))
        }
        
        if let fontDict = fontValue as? [String: Any] {
            let family = fontDict["family"] as? String ?? "System"
            let size = (fontDict["size"] as? Float) ?? defaultSize
            let weight = fontDict["weight"] as? String
            let style = fontDict["style"] as? String
            
            let fontDef = FontDefinition(family: family, size: size, weight: weight, style: style)
            return fontDef.toUIFont()
        }
        
        return nil
    }
    
    /// Get text alignment value
    public func getTextAlignment(_ key: String = "textAlign") -> NSTextAlignment {
        guard let alignString = getString(key) else { return .left }
        
        switch alignString.lowercased() {
        case "left", "start":
            return .left
        case "right", "end":
            return .right
        case "center":
            return .center
        case "justify", "justified":
            return .justified
        case "natural":
            return .natural
        default:
            return .left
        }
    }
    
    /// Get content mode for images
    public func getContentMode(_ key: String = "contentMode") -> UIView.ContentMode {
        guard let modeString = getString(key) else { return .scaleAspectFit }
        
        switch modeString.lowercased() {
        case "scaletofill", "fill":
            return .scaleToFill
        case "scaleaspectfit", "fit":
            return .scaleAspectFit
        case "scaleaspectfill", "crop":
            return .scaleAspectFill
        case "center":
            return .center
        case "top":
            return .top
        case "bottom":
            return .bottom
        case "left":
            return .left
        case "right":
            return .right
        case "topleft":
            return .topLeft
        case "topright":
            return .topRight
        case "bottomleft":
            return .bottomLeft
        case "bottomright":
            return .bottomRight
        default:
            return .scaleAspectFit
        }
    }
    
    /// Get edge insets (for padding, margin, etc.)
    public func getEdgeInsets(_ key: String, theme: FlexTheme? = nil) -> UIEdgeInsets {
        guard let value = properties[key] else { return .zero }
        
        if let floatValue = value as? Float {
            // Single value for all edges
            let inset = CGFloat(floatValue)
            return UIEdgeInsets(top: inset, left: inset, bottom: inset, right: inset)
        }
        
        if let stringValue = value as? String, let theme = theme {
            // Theme reference or dimension string
            if let dimension = theme.resolveDimension(stringValue) {
                return UIEdgeInsets(top: dimension, left: dimension, bottom: dimension, right: dimension)
            }
        }
        
        if let dictValue = value as? [String: Any] {
            // Individual values for each edge
            let top = getDimensionFromDict(dictValue, key: "top", theme: theme) ?? 0
            let left = getDimensionFromDict(dictValue, key: "left", theme: theme) ?? 0
            let bottom = getDimensionFromDict(dictValue, key: "bottom", theme: theme) ?? 0
            let right = getDimensionFromDict(dictValue, key: "right", theme: theme) ?? 0
            
            return UIEdgeInsets(top: top, left: left, bottom: bottom, right: right)
        }
        
        return .zero
    }
    
    /// Get CGSize value
    public func getSize(_ key: String, theme: FlexTheme? = nil) -> CGSize? {
        guard let value = properties[key] else { return nil }
        
        if let dictValue = value as? [String: Any] {
            let width = getDimensionFromDict(dictValue, key: "width", theme: theme) ?? 0
            let height = getDimensionFromDict(dictValue, key: "height", theme: theme) ?? 0
            return CGSize(width: width, height: height)
        }
        
        return nil
    }
    
    /// Get CGPoint value
    public func getPoint(_ key: String, theme: FlexTheme? = nil) -> CGPoint? {
        guard let value = properties[key] else { return nil }
        
        if let dictValue = value as? [String: Any] {
            let x = getDimensionFromDict(dictValue, key: "x", theme: theme) ?? 0
            let y = getDimensionFromDict(dictValue, key: "y", theme: theme) ?? 0
            return CGPoint(x: x, y: y)
        }
        
        return nil
    }
    
    // MARK: - Convenience Methods
    
    /// Check if a property exists
    public func has(_ key: String) -> Bool {
        return properties[key] != nil
    }
    
    /// Get all property keys
    public var keys: [String] {
        return Array(properties.keys)
    }
    
    /// Get all properties as dictionary
    public var allProperties: [String: Any] {
        return properties
    }
    
    /// Get a subset of properties with a given prefix
    public func getPropertiesWithPrefix(_ prefix: String) -> [String: Any] {
        return properties.filter { key, _ in
            key.hasPrefix(prefix)
        }
    }
    
    /// Resolve properties with theme variables
    public func resolved(with theme: FlexTheme, data: [String: Any] = [:]) -> FlexProps {
        let resolver = ThemeResolver(theme: theme)
        let resolvedProperties = properties.mapValues { value in
            if let stringValue = value as? String {
                return resolver.replaceVariables(stringValue, with: data)
            }
            return value
        }
        
        return FlexProps(properties: resolvedProperties)
    }
    
    // MARK: - Private Helpers
    
    private func getDimensionFromDict(_ dict: [String: Any], key: String, theme: FlexTheme?) -> CGFloat? {
        guard let value = dict[key] else { return nil }
        
        if let theme = theme {
            return theme.resolveDimension(value)
        } else if let floatValue = value as? Float {
            return CGFloat(floatValue)
        } else if let doubleValue = value as? Double {
            return CGFloat(doubleValue)
        } else if let intValue = value as? Int {
            return CGFloat(intValue)
        }
        
        return nil
    }
}

// MARK: - Error Types

public enum FlexPropsError: Error, LocalizedError {
    case missingRequiredProperty(String, type: String)
    case invalidPropertyType(String, expected: String, actual: String)
    
    public var errorDescription: String? {
        switch self {
        case .missingRequiredProperty(let key, let type):
            return "Missing required property '\(key)' of type \(type)"
        case .invalidPropertyType(let key, let expected, let actual):
            return "Invalid type for property '\(key)': expected \(expected), got \(actual)"
        }
    }
}

// MARK: - Builder Pattern

public extension FlexProps {
    
    /// Builder for creating FlexProps with a fluent interface
    class Builder {
        private var properties: [String: Any] = [:]
        
        public init() {}
        
        public func set(_ key: String, value: Any) -> Builder {
            properties[key] = value
            return self
        }
        
        public func setString(_ key: String, value: String) -> Builder {
            properties[key] = value
            return self
        }
        
        public func setInt(_ key: String, value: Int) -> Builder {
            properties[key] = value
            return self
        }
        
        public func setFloat(_ key: String, value: Float) -> Builder {
            properties[key] = value
            return self
        }
        
        public func setBool(_ key: String, value: Bool) -> Builder {
            properties[key] = value
            return self
        }
        
        public func setColor(_ key: String, value: UIColor) -> Builder {
            properties[key] = value.hexString
            return self
        }
        
        public func build() -> FlexProps {
            return FlexProps(properties: properties)
        }
    }
}