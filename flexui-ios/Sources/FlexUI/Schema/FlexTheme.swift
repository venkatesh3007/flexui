import UIKit
import Foundation

/// Represents theme configuration for FlexUI components
public struct FlexTheme: Codable {
    
    // MARK: - Properties
    
    /// Color palette definitions
    public var colors: [String: String]
    
    /// Font definitions
    public var fonts: [String: FontDefinition]
    
    /// Dimension definitions (spacing, sizes)
    public var dimensions: [String: Float]
    
    /// String constants and localization
    public var strings: [String: String]
    
    /// Icon mappings
    public var icons: [String: String]
    
    /// Boolean flags
    public var flags: [String: Bool]
    
    // MARK: - Initialization
    
    public init(
        colors: [String: String] = [:],
        fonts: [String: FontDefinition] = [:],
        dimensions: [String: Float] = [:],
        strings: [String: String] = [:],
        icons: [String: String] = [:],
        flags: [String: Bool] = [:]
    ) {
        self.colors = colors
        self.fonts = fonts
        self.dimensions = dimensions
        self.strings = strings
        self.icons = icons
        self.flags = flags
    }
    
    // MARK: - Color Access
    
    /// Get color value by name
    public func getColor(_ name: String) -> String? {
        return colors[name]
    }
    
    /// Get resolved UIColor for a color name or hex string
    public func resolveColor(_ value: Any?) -> UIColor? {
        guard let colorValue = value else { return nil }
        
        if let colorName = colorValue as? String {
            // Check if it's a theme color reference
            if let themeColor = getColor(colorName) {
                return UIColor(hex: themeColor)
            }
            // Try to parse as hex color directly
            return UIColor(hex: colorName)
        }
        
        return nil
    }
    
    // MARK: - Font Access
    
    /// Get font definition by name
    public func getFont(_ name: String) -> FontDefinition? {
        return fonts[name]
    }
    
    /// Get font size by name
    public func getFontSize(_ name: String) -> Float? {
        return getFont(name)?.size ?? dimensions[name]
    }
    
    /// Get resolved UIFont for a font definition
    public func resolveFont(_ fontName: String?, size: Float) -> UIFont {
        if let fontName = fontName, let fontDef = getFont(fontName) {
            return fontDef.toUIFont()
        }
        
        if let fontName = fontName {
            return UIFont(name: fontName, size: CGFloat(size)) ?? UIFont.systemFont(ofSize: CGFloat(size))
        }
        
        return UIFont.systemFont(ofSize: CGFloat(size))
    }
    
    // MARK: - Dimension Access
    
    /// Get dimension value by name
    public func getDimension(_ name: String) -> Float? {
        return dimensions[name]
    }
    
    /// Resolve a dimension value considering theme references
    public func resolveDimension(_ value: Any?) -> CGFloat? {
        guard let dimValue = value else { return nil }
        
        if let dimensionName = dimValue as? String {
            // Check if it's a theme dimension reference
            if let themeDimension = getDimension(dimensionName) {
                return CGFloat(themeDimension)
            }
            
            // Try to parse as number with unit
            return parseDimensionString(dimensionName)
        }
        
        if let number = dimValue as? NSNumber {
            return CGFloat(number.floatValue)
        }
        
        return nil
    }
    
    // MARK: - String Access
    
    /// Get string value by key
    public func getString(_ key: String) -> String? {
        return strings[key]
    }
    
    /// Resolve string with theme variables replaced
    public func resolveString(_ value: String) -> String {
        var resolved = value
        
        // Replace theme string references like {{stringKey}}
        let pattern = "\\{\\{([^}]+)\\}\\}"
        if let regex = try? NSRegularExpression(pattern: pattern) {
            let range = NSRange(resolved.startIndex..<resolved.endIndex, in: resolved)
            let matches = regex.matches(in: resolved, range: range)
            
            for match in matches.reversed() {
                if let keyRange = Range(match.range(at: 1), in: resolved) {
                    let key = String(resolved[keyRange])
                    if let replacement = getString(key) {
                        let fullRange = Range(match.range, in: resolved)!
                        resolved.replaceSubrange(fullRange, with: replacement)
                    }
                }
            }
        }
        
        return resolved
    }
    
    // MARK: - Icon Access
    
    /// Get icon name by key
    public func getIcon(_ key: String) -> String? {
        return icons[key]
    }
    
    // MARK: - Flag Access
    
    /// Get boolean flag by key
    public func getFlag(_ key: String) -> Bool? {
        return flags[key]
    }
    
    // MARK: - Private Helpers
    
    /// Parse dimension string with units (e.g., "16px", "1.5rem", "20dp")
    private func parseDimensionString(_ value: String) -> CGFloat? {
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        
        // Extract numeric part
        let pattern = "^(-?\\d*\\.?\\d+)\\s*(px|dp|pt|sp|rem|em|%)?$"
        guard let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) else {
            return nil
        }
        
        let range = NSRange(trimmed.startIndex..<trimmed.endIndex, in: trimmed)
        guard let match = regex.firstMatch(in: trimmed, range: range) else {
            return nil
        }
        
        guard let numberRange = Range(match.range(at: 1), in: trimmed) else {
            return nil
        }
        
        let numberString = String(trimmed[numberRange])
        guard let number = Float(numberString) else {
            return nil
        }
        
        // Handle units
        let unitRange = match.range(at: 2)
        if unitRange.location != NSNotFound, let unitStringRange = Range(unitRange, in: trimmed) {
            let unit = String(trimmed[unitStringRange]).lowercased()
            
            switch unit {
            case "px", "dp", "pt":
                return CGFloat(number)
            case "sp":
                // Scaled pixels - could be adjusted based on accessibility settings
                return CGFloat(number)
            case "rem":
                // Relative to root element font size (assume 16px)
                return CGFloat(number * 16)
            case "em":
                // Relative to current font size (assume 16px for now)
                return CGFloat(number * 16)
            case "%":
                // Percentage values need context - return as-is for now
                return CGFloat(number)
            default:
                return CGFloat(number)
            }
        }
        
        return CGFloat(number)
    }
}

// MARK: - FontDefinition

/// Represents a font definition in the theme
public struct FontDefinition: Codable {
    public let family: String
    public let size: Float
    public let weight: String?
    public let style: String?
    
    public init(family: String, size: Float, weight: String? = nil, style: String? = nil) {
        self.family = family
        self.size = size
        self.weight = weight
        self.style = style
    }
    
    /// Convert to UIFont
    public func toUIFont() -> UIFont {
        var font: UIFont
        
        // Try to create font with family name
        if let customFont = UIFont(name: family, size: CGFloat(size)) {
            font = customFont
        } else {
            font = UIFont.systemFont(ofSize: CGFloat(size))
        }
        
        // Apply weight if specified
        if let weight = weight {
            font = applyWeight(to: font, weight: weight)
        }
        
        // Apply style if specified
        if let style = style {
            font = applyStyle(to: font, style: style)
        }
        
        return font
    }
    
    private func applyWeight(to font: UIFont, weight: String) -> UIFont {
        let fontWeight: UIFont.Weight
        
        switch weight.lowercased() {
        case "ultralight":
            fontWeight = .ultraLight
        case "thin":
            fontWeight = .thin
        case "light":
            fontWeight = .light
        case "regular", "normal":
            fontWeight = .regular
        case "medium":
            fontWeight = .medium
        case "semibold":
            fontWeight = .semibold
        case "bold":
            fontWeight = .bold
        case "heavy":
            fontWeight = .heavy
        case "black":
            fontWeight = .black
        default:
            fontWeight = .regular
        }
        
        return UIFont.systemFont(ofSize: font.pointSize, weight: fontWeight)
    }
    
    private func applyStyle(to font: UIFont, style: String) -> UIFont {
        switch style.lowercased() {
        case "italic":
            let descriptor = font.fontDescriptor.withSymbolicTraits(.traitItalic)
            return descriptor != nil ? UIFont(descriptor: descriptor!, size: font.pointSize) : font
        default:
            return font
        }
    }
}

// MARK: - UIColor Extension

public extension UIColor {
    
    /// Create UIColor from hex string (e.g., "#FF0000", "FF0000", "#RGB", "RGB")
    convenience init?(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        
        Scanner(string: hex).scanHexInt64(&int)
        
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            return nil
        }
        
        self.init(
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            alpha: Double(a) / 255
        )
    }
    
    /// Convert UIColor to hex string
    var hexString: String {
        let components = cgColor.components
        let r = Float(components?[0] ?? 0)
        let g = Float(components?[1] ?? 0)
        let b = Float(components?[2] ?? 0)
        let a = Float(components?[3] ?? 1)
        
        if a < 1.0 {
            return String(format: "#%02lX%02lX%02lX%02lX",
                         lroundf(a * 255),
                         lroundf(r * 255),
                         lroundf(g * 255),
                         lroundf(b * 255))
        } else {
            return String(format: "#%02lX%02lX%02lX",
                         lroundf(r * 255),
                         lroundf(g * 255),
                         lroundf(b * 255))
        }
    }
}

// MARK: - Theme Merging

public extension FlexTheme {
    
    /// Merge this theme with another theme, with the other theme taking precedence
    func merged(with other: FlexTheme) -> FlexTheme {
        return FlexTheme(
            colors: colors.merging(other.colors) { _, new in new },
            fonts: fonts.merging(other.fonts) { _, new in new },
            dimensions: dimensions.merging(other.dimensions) { _, new in new },
            strings: strings.merging(other.strings) { _, new in new },
            icons: icons.merging(other.icons) { _, new in new },
            flags: flags.merging(other.flags) { _, new in new }
        )
    }
}