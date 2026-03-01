import UIKit
import Foundation

/// Handles variable resolution and theme interpolation
public final class ThemeResolver {
    
    // MARK: - Properties
    
    private let theme: FlexTheme
    
    // MARK: - Initialization
    
    public init(theme: FlexTheme) {
        self.theme = theme
    }
    
    // MARK: - Variable Resolution
    
    /// Replace theme and data variables in a string
    public func replaceVariables(_ input: String, with data: [String: Any] = [:]) -> String {
        var result = input
        
        // Replace theme variables first ({{theme.colors.primary}})
        result = replaceThemeVariables(result)
        
        // Then replace data variables ({{data.user.name}})
        result = replaceDataVariables(result, with: data)
        
        // Finally replace device/platform variables ({{device.platform}})
        result = replacePlatformVariables(result)
        
        return result
    }
    
    /// Resolve a color value (hex string, theme reference, or UIColor)
    public func resolveColor(_ value: Any?) -> UIColor? {
        return theme.resolveColor(value)
    }
    
    /// Resolve a dimension value (number, theme reference, or dimension string)
    public func resolveDimension(_ value: Any?) -> CGFloat? {
        return theme.resolveDimension(value)
    }
    
    /// Resolve a string value with variable replacement
    public func resolveString(_ value: Any?, with data: [String: Any] = [:]) -> String? {
        guard let stringValue = value as? String else { return nil }
        return replaceVariables(stringValue, with: data)
    }
    
    /// Resolve a font with theme variables
    public func resolveFont(_ value: Any?, defaultSize: Float = 16) -> UIFont? {
        if let stringValue = value as? String {
            // Check if it's a theme font reference
            if let fontDef = theme.getFont(stringValue) {
                return fontDef.toUIFont()
            }
            
            // Try as font family name
            return UIFont(name: stringValue, size: CGFloat(defaultSize))
        }
        
        return nil
    }
    
    // MARK: - Private Methods
    
    /// Replace theme variables like {{colors.primary}}, {{spacing.md}}
    private func replaceThemeVariables(_ input: String) -> String {
        let pattern = "\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)*)\\s*\\}\\}"
        
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
            return input
        }
        
        var result = input
        let range = NSRange(result.startIndex..<result.endIndex, in: result)
        let matches = regex.matches(in: result, range: range)
        
        // Process matches in reverse order to maintain string indices
        for match in matches.reversed() {
            guard let variableRange = Range(match.range(at: 1), in: result) else { continue }
            
            let variablePath = String(result[variableRange])
            if let replacement = resolveThemeVariable(variablePath) {
                let fullRange = Range(match.range, in: result)!
                result.replaceSubrange(fullRange, with: replacement)
            }
        }
        
        return result
    }
    
    /// Replace data variables like {{data.user.name}}, {{user.profile.email}}
    private func replaceDataVariables(_ input: String, with data: [String: Any]) -> String {
        let pattern = "\\{\\{\\s*(?:data\\.)?([a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)*)\\s*\\}\\}"
        
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
            return input
        }
        
        var result = input
        let range = NSRange(result.startIndex..<result.endIndex, in: result)
        let matches = regex.matches(in: result, range: range)
        
        // Process matches in reverse order
        for match in matches.reversed() {
            guard let variableRange = Range(match.range(at: 1), in: result) else { continue }
            
            let variablePath = String(result[variableRange])
            if let replacement = resolveDataVariable(variablePath, data: data) {
                let fullRange = Range(match.range, in: result)!
                result.replaceSubrange(fullRange, with: String(describing: replacement))
            }
        }
        
        return result
    }
    
    /// Replace platform/device variables like {{device.platform}}, {{screen.width}}
    private func replacePlatformVariables(_ input: String) -> String {
        let pattern = "\\{\\{\\s*(device|screen|app)\\.(\\w+)\\s*\\}\\}"
        
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
            return input
        }
        
        var result = input
        let range = NSRange(result.startIndex..<result.endIndex, in: result)
        let matches = regex.matches(in: result, range: range)
        
        for match in matches.reversed() {
            guard let categoryRange = Range(match.range(at: 1), in: result),
                  let propertyRange = Range(match.range(at: 2), in: result) else { continue }
            
            let category = String(result[categoryRange])
            let property = String(result[propertyRange])
            
            if let replacement = resolvePlatformVariable(category: category, property: property) {
                let fullRange = Range(match.range, in: result)!
                result.replaceSubrange(fullRange, with: replacement)
            }
        }
        
        return result
    }
    
    /// Resolve a theme variable path (e.g., "colors.primary", "spacing.md")
    private func resolveThemeVariable(_ path: String) -> String? {
        let components = path.split(separator: ".").map(String.init)
        
        guard !components.isEmpty else { return nil }
        
        let category = components[0]
        
        switch category {
        case "colors":
            if components.count >= 2 {
                return theme.getColor(components[1])
            }
            
        case "fonts":
            if components.count >= 2 {
                let fontName = components[1]
                if let fontDef = theme.getFont(fontName) {
                    // Return font family for now, could be extended
                    return fontDef.family
                }
            }
            
        case "dimensions", "spacing", "sizes":
            if components.count >= 2 {
                if let dimension = theme.getDimension(components[1]) {
                    return String(dimension)
                }
            }
            
        case "strings":
            if components.count >= 2 {
                return theme.getString(components[1])
            }
            
        case "icons":
            if components.count >= 2 {
                return theme.getIcon(components[1])
            }
            
        case "flags":
            if components.count >= 2 {
                if let flag = theme.getFlag(components[1]) {
                    return String(flag)
                }
            }
            
        default:
            // Try direct lookup in dimensions for backwards compatibility
            if let dimension = theme.getDimension(path) {
                return String(dimension)
            }
        }
        
        return nil
    }
    
    /// Resolve a data variable path using dot notation
    private func resolveDataVariable(_ path: String, data: [String: Any]) -> Any? {
        let components = path.split(separator: ".").map(String.init)
        var current: Any? = data
        
        for component in components {
            if let dict = current as? [String: Any] {
                current = dict[component]
            } else if let array = current as? [Any],
                      let index = Int(component),
                      index >= 0 && index < array.count {
                current = array[index]
            } else {
                return nil
            }
        }
        
        return current
    }
    
    /// Resolve platform/device variables
    private func resolvePlatformVariable(category: String, property: String) -> String? {
        switch category {
        case "device":
            switch property {
            case "platform":
                return "ios"
            case "model":
                return UIDevice.current.model
            case "name":
                return UIDevice.current.name
            case "systemVersion":
                return UIDevice.current.systemVersion
            case "idiom":
                switch UIDevice.current.userInterfaceIdiom {
                case .phone:
                    return "phone"
                case .pad:
                    return "pad"
                case .tv:
                    return "tv"
                case .carPlay:
                    return "carPlay"
                case .mac:
                    return "mac"
                case .vision:
                    return "vision"
                default:
                    return "unknown"
                }
            default:
                return nil
            }
            
        case "screen":
            let screen = UIScreen.main
            switch property {
            case "width":
                return String(Int(screen.bounds.width))
            case "height":
                return String(Int(screen.bounds.height))
            case "scale":
                return String(screen.scale)
            case "brightness":
                return String(screen.brightness)
            default:
                return nil
            }
            
        case "app":
            switch property {
            case "version":
                return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
            case "build":
                return Bundle.main.infoDictionary?["CFBundleVersion"] as? String
            case "bundleId":
                return Bundle.main.bundleIdentifier
            case "name":
                return Bundle.main.infoDictionary?["CFBundleName"] as? String
            default:
                return nil
            }
            
        default:
            return nil
        }
    }
}

// MARK: - Advanced Resolution

public extension ThemeResolver {
    
    /// Resolve a value that could be a theme variable, data variable, or literal value
    func resolveAnyValue(_ value: Any?, with data: [String: Any] = [:]) -> Any? {
        guard let value = value else { return nil }
        
        if let stringValue = value as? String {
            // Check if it looks like a template variable
            if stringValue.contains("{{") && stringValue.contains("}}") {
                let resolved = replaceVariables(stringValue, with: data)
                
                // Try to convert back to appropriate type
                if let boolValue = Bool(resolved) {
                    return boolValue
                }
                
                if let intValue = Int(resolved) {
                    return intValue
                }
                
                if let floatValue = Float(resolved) {
                    return floatValue
                }
                
                return resolved
            }
        }
        
        return value
    }
    
    /// Resolve a style dictionary with all theme variables replaced
    func resolveStyleDictionary(_ style: [String: Any], with data: [String: Any] = [:]) -> [String: Any] {
        var resolved: [String: Any] = [:]
        
        for (key, value) in style {
            resolved[key] = resolveAnyValue(value, with: data)
        }
        
        return resolved
    }
    
    /// Check if a string contains template variables
    func containsVariables(_ string: String) -> Bool {
        return string.contains("{{") && string.contains("}}")
    }
    
    /// Extract all variable references from a string
    func extractVariables(_ string: String) -> [String] {
        let pattern = "\\{\\{\\s*([^}]+)\\s*\\}\\}"
        
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
            return []
        }
        
        let range = NSRange(string.startIndex..<string.endIndex, in: string)
        let matches = regex.matches(in: string, range: range)
        
        return matches.compactMap { match in
            guard let variableRange = Range(match.range(at: 1), in: string) else { return nil }
            return String(string[variableRange]).trimmingCharacters(in: .whitespaces)
        }
    }
}