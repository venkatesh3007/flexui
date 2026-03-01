import UIKit
import Foundation

/// Provides default theme values and merging functionality for FlexUI
public final class ThemeDefaults {
    
    // MARK: - Default Theme
    
    /// The default FlexUI theme with common design tokens
    public static let defaultTheme = FlexTheme(
        colors: [
            // Primary colors
            "primary": "#007AFF",
            "primaryDark": "#0056CC",
            "primaryLight": "#66B3FF",
            
            // Secondary colors
            "secondary": "#5856D6",
            "secondaryDark": "#3F3EA3",
            "secondaryLight": "#8B8AE9",
            
            // Neutral colors
            "background": "#FFFFFF",
            "backgroundSecondary": "#F2F2F7",
            "surface": "#FFFFFF",
            "surfaceSecondary": "#F9F9FB",
            
            // Text colors
            "text": "#000000",
            "textSecondary": "#6D6D70",
            "textTertiary": "#C7C7CC",
            "textInverse": "#FFFFFF",
            
            // Status colors
            "success": "#30D158",
            "warning": "#FF9F0A",
            "error": "#FF3B30",
            "info": "#007AFF",
            
            // Border colors
            "border": "#C6C6C8",
            "borderLight": "#E5E5EA",
            
            // Shadow colors
            "shadow": "#000000",
            "shadowLight": "#00000020"
        ],
        
        fonts: [
            // System fonts
            "systemRegular": FontDefinition(family: "System", size: 16, weight: "regular"),
            "systemMedium": FontDefinition(family: "System", size: 16, weight: "medium"),
            "systemBold": FontDefinition(family: "System", size: 16, weight: "bold"),
            
            // Heading fonts
            "heading1": FontDefinition(family: "System", size: 32, weight: "bold"),
            "heading2": FontDefinition(family: "System", size: 24, weight: "bold"),
            "heading3": FontDefinition(family: "System", size: 20, weight: "semibold"),
            "heading4": FontDefinition(family: "System", size: 18, weight: "semibold"),
            
            // Body fonts
            "body": FontDefinition(family: "System", size: 16, weight: "regular"),
            "bodyBold": FontDefinition(family: "System", size: 16, weight: "bold"),
            "bodyLarge": FontDefinition(family: "System", size: 18, weight: "regular"),
            "bodySmall": FontDefinition(family: "System", size: 14, weight: "regular"),
            
            // Caption and footnote
            "caption": FontDefinition(family: "System", size: 12, weight: "regular"),
            "footnote": FontDefinition(family: "System", size: 13, weight: "regular"),
            
            // Button fonts
            "button": FontDefinition(family: "System", size: 16, weight: "medium"),
            "buttonLarge": FontDefinition(family: "System", size: 18, weight: "medium"),
            "buttonSmall": FontDefinition(family: "System", size: 14, weight: "medium")
        ],
        
        dimensions: [
            // Spacing scale
            "spacing_xs": 4,
            "spacing_sm": 8,
            "spacing_md": 16,
            "spacing_lg": 24,
            "spacing_xl": 32,
            "spacing_xxl": 48,
            
            // Short aliases
            "xs": 4,
            "sm": 8,
            "md": 16,
            "lg": 24,
            "xl": 32,
            "xxl": 48,
            
            // Border radius
            "radius_xs": 2,
            "radius_sm": 4,
            "radius_md": 8,
            "radius_lg": 12,
            "radius_xl": 16,
            "radius_full": 999,
            
            // Common sizes
            "size_xs": 16,
            "size_sm": 24,
            "size_md": 32,
            "size_lg": 48,
            "size_xl": 64,
            
            // Icon sizes
            "icon_xs": 12,
            "icon_sm": 16,
            "icon_md": 24,
            "icon_lg": 32,
            "icon_xl": 48,
            
            // Button heights
            "button_sm": 32,
            "button_md": 44,
            "button_lg": 56,
            
            // Border widths
            "border_thin": 0.5,
            "border_normal": 1,
            "border_thick": 2,
            
            // Shadow properties
            "shadow_xs": 2,
            "shadow_sm": 4,
            "shadow_md": 8,
            "shadow_lg": 16,
            "shadow_xl": 24
        ],
        
        strings: [
            // Common UI strings
            "ok": "OK",
            "cancel": "Cancel",
            "done": "Done",
            "save": "Save",
            "delete": "Delete",
            "edit": "Edit",
            "close": "Close",
            "back": "Back",
            "next": "Next",
            "previous": "Previous",
            "loading": "Loading...",
            "error": "Error",
            "retry": "Retry",
            "refresh": "Refresh",
            
            // Form strings
            "required": "Required",
            "optional": "Optional",
            "invalid": "Invalid",
            "valid": "Valid",
            
            // Status messages
            "success": "Success",
            "warning": "Warning",
            "info": "Information",
            
            // Empty states
            "no_data": "No data available",
            "empty_list": "No items found",
            "search_empty": "No results found"
        ],
        
        icons: [
            // Common system icons (SF Symbols names)
            "check": "checkmark",
            "close": "xmark",
            "arrow_left": "arrow.left",
            "arrow_right": "arrow.right",
            "arrow_up": "arrow.up",
            "arrow_down": "arrow.down",
            "chevron_left": "chevron.left",
            "chevron_right": "chevron.right",
            "chevron_up": "chevron.up",
            "chevron_down": "chevron.down",
            "plus": "plus",
            "minus": "minus",
            "search": "magnifyingglass",
            "settings": "gearshape",
            "profile": "person.circle",
            "home": "house",
            "star": "star",
            "star_filled": "star.fill",
            "heart": "heart",
            "heart_filled": "heart.fill",
            "share": "square.and.arrow.up",
            "edit": "pencil",
            "trash": "trash",
            "info": "info.circle",
            "warning": "exclamationmark.triangle",
            "error": "xmark.circle",
            "success": "checkmark.circle"
        ],
        
        flags: [
            "debug": false,
            "animations_enabled": true,
            "haptic_feedback": true,
            "accessibility_enabled": true,
            "dark_mode_supported": true
        ]
    )
    
    // MARK: - Dark Theme
    
    /// Default dark theme variant
    public static let darkTheme = FlexTheme(
        colors: [
            // Primary colors (slightly adjusted for dark mode)
            "primary": "#0A84FF",
            "primaryDark": "#0056CC",
            "primaryLight": "#66B3FF",
            
            // Secondary colors
            "secondary": "#5E5CE6",
            "secondaryDark": "#3F3EA3",
            "secondaryLight": "#8B8AE9",
            
            // Neutral colors (inverted)
            "background": "#000000",
            "backgroundSecondary": "#1C1C1E",
            "surface": "#1C1C1E",
            "surfaceSecondary": "#2C2C2E",
            
            // Text colors (inverted)
            "text": "#FFFFFF",
            "textSecondary": "#8E8E93",
            "textTertiary": "#48484A",
            "textInverse": "#000000",
            
            // Status colors
            "success": "#30D158",
            "warning": "#FF9F0A",
            "error": "#FF453A",
            "info": "#64D2FF",
            
            // Border colors
            "border": "#38383A",
            "borderLight": "#2C2C2E",
            
            // Shadow colors
            "shadow": "#000000",
            "shadowLight": "#00000040"
        ]
    )
    
    // MARK: - Theme Merging
    
    /// Merge a theme with default values, with the provided theme taking precedence
    public static func merge(theme: FlexTheme) -> FlexTheme {
        return defaultTheme.merged(with: theme)
    }
    
    /// Merge multiple themes, with later themes taking precedence
    public static func merge(themes: [FlexTheme]) -> FlexTheme {
        guard !themes.isEmpty else { return defaultTheme }
        
        return themes.reduce(defaultTheme) { result, theme in
            result.merged(with: theme)
        }
    }
    
    /// Get theme appropriate for current interface style
    public static func themeForInterfaceStyle(_ style: UIUserInterfaceStyle) -> FlexTheme {
        switch style {
        case .dark:
            return darkTheme
        case .light:
            return defaultTheme
        case .unspecified:
            return defaultTheme
        @unknown default:
            return defaultTheme
        }
    }
    
    /// Get adaptive theme that switches based on system appearance
    public static func adaptiveTheme(light: FlexTheme? = nil, dark: FlexTheme? = nil) -> FlexTheme {
        let lightTheme = light ?? defaultTheme
        let darkTheme = dark ?? ThemeDefaults.darkTheme
        
        if #available(iOS 13.0, *) {
            if UITraitCollection.current.userInterfaceStyle == .dark {
                return darkTheme
            }
        }
        
        return lightTheme
    }
    
    // MARK: - Semantic Mappings
    
    /// Get semantic color mappings for common UI elements
    public static func semanticColors() -> [String: String] {
        return [
            "buttonPrimary": "primary",
            "buttonSecondary": "secondary",
            "buttonDestructive": "error",
            "textPrimary": "text",
            "textSecondary": "textSecondary",
            "backgroundPrimary": "background",
            "backgroundSecondary": "backgroundSecondary",
            "borderPrimary": "border",
            "statusSuccess": "success",
            "statusWarning": "warning",
            "statusError": "error",
            "statusInfo": "info"
        ]
    }
    
    /// Get semantic spacing mappings
    public static func semanticSpacing() -> [String: String] {
        return [
            "padding": "md",
            "margin": "md",
            "gap": "sm",
            "buttonPadding": "md",
            "cardPadding": "lg",
            "screenPadding": "lg",
            "sectionSpacing": "xl"
        ]
    }
    
    // MARK: - Validation
    
    /// Validate that a theme has all required properties
    public static func validate(theme: FlexTheme) -> [String] {
        var errors: [String] = []
        
        // Check essential colors
        let requiredColors = ["primary", "background", "text", "error"]
        for color in requiredColors {
            if theme.getColor(color) == nil {
                errors.append("Missing required color: \\(color)")
            }
        }
        
        // Check essential dimensions
        let requiredDimensions = ["sm", "md", "lg"]
        for dimension in requiredDimensions {
            if theme.getDimension(dimension) == nil {
                errors.append("Missing required dimension: \\(dimension)")
            }
        }
        
        // Validate color format
        for (key, colorValue) in theme.colors {
            if UIColor(hex: colorValue) == nil {
                errors.append("Invalid color format for \\(key): \\(colorValue)")
            }
        }
        
        // Validate font sizes
        for (key, fontDef) in theme.fonts {
            if fontDef.size <= 0 {
                errors.append("Invalid font size for \\(key): \\(fontDef.size)")
            }
        }
        
        // Validate dimensions
        for (key, dimensionValue) in theme.dimensions {
            if dimensionValue < 0 {
                errors.append("Invalid dimension value for \\(key): \\(dimensionValue)")
            }
        }
        
        return errors
    }
    
    // MARK: - Theme Generation
    
    /// Generate a theme based on a primary color
    public static func generateTheme(primaryColor: String) -> FlexTheme {
        guard let primaryUIColor = UIColor(hex: primaryColor) else {
            return defaultTheme
        }
        
        // Generate complementary colors
        let colors = generateColorPalette(from: primaryUIColor)
        
        return FlexTheme(
            colors: colors,
            fonts: defaultTheme.fonts,
            dimensions: defaultTheme.dimensions,
            strings: defaultTheme.strings,
            icons: defaultTheme.icons,
            flags: defaultTheme.flags
        )
    }
    
    /// Generate a color palette from a primary color
    private static func generateColorPalette(from primary: UIColor) -> [String: String] {
        var colors = defaultTheme.colors
        
        colors["primary"] = primary.hexString
        colors["primaryDark"] = primary.darker(by: 0.2)?.hexString ?? colors["primary"]
        colors["primaryLight"] = primary.lighter(by: 0.2)?.hexString ?? colors["primary"]
        
        return colors
    }
}

// MARK: - UIColor Extensions

private extension UIColor {
    
    /// Create a darker version of this color
    func darker(by percentage: CGFloat) -> UIColor? {
        return adjustBrightness(by: -abs(percentage))
    }
    
    /// Create a lighter version of this color
    func lighter(by percentage: CGFloat) -> UIColor? {
        return adjustBrightness(by: abs(percentage))
    }
    
    /// Adjust the brightness of this color
    func adjustBrightness(by percentage: CGFloat) -> UIColor? {
        var hue: CGFloat = 0
        var saturation: CGFloat = 0
        var brightness: CGFloat = 0
        var alpha: CGFloat = 0
        
        guard getHue(&hue, saturation: &saturation, brightness: &brightness, alpha: &alpha) else {
            return nil
        }
        
        brightness = max(0, min(1, brightness + percentage))
        
        return UIColor(hue: hue, saturation: saturation, brightness: brightness, alpha: alpha)
    }
}

// MARK: - Theme Presets

public extension ThemeDefaults {
    
    /// Predefined theme presets
    enum Preset {
        case `default`
        case dark
        case minimal
        case colorful
        case corporate
        
        var theme: FlexTheme {
            switch self {
            case .default:
                return ThemeDefaults.defaultTheme
            case .dark:
                return ThemeDefaults.darkTheme
            case .minimal:
                return minimalTheme
            case .colorful:
                return colorfulTheme
            case .corporate:
                return corporateTheme
            }
        }
    }
    
    /// Minimal theme with reduced visual noise
    static var minimalTheme: FlexTheme {
        var colors = defaultTheme.colors
        colors["primary"] = "#000000"
        colors["secondary"] = "#666666"
        colors["border"] = "#E0E0E0"
        colors["borderLight"] = "#F5F5F5"
        
        return FlexTheme(
            colors: colors,
            fonts: defaultTheme.fonts,
            dimensions: defaultTheme.dimensions,
            strings: defaultTheme.strings,
            icons: defaultTheme.icons,
            flags: defaultTheme.flags
        )
    }
    
    /// Colorful theme with vibrant colors
    static var colorfulTheme: FlexTheme {
        var colors = defaultTheme.colors
        colors["primary"] = "#FF6B6B"
        colors["secondary"] = "#4ECDC4"
        colors["success"] = "#51CF66"
        colors["warning"] = "#FFD93D"
        colors["error"] = "#FF6B6B"
        colors["info"] = "#74C0FC"
        
        return FlexTheme(
            colors: colors,
            fonts: defaultTheme.fonts,
            dimensions: defaultTheme.dimensions,
            strings: defaultTheme.strings,
            icons: defaultTheme.icons,
            flags: defaultTheme.flags
        )
    }
    
    /// Corporate theme with professional colors
    static var corporateTheme: FlexTheme {
        var colors = defaultTheme.colors
        colors["primary"] = "#2E5BBA"
        colors["secondary"] = "#8BC34A"
        colors["background"] = "#FAFAFA"
        colors["text"] = "#2C3E50"
        colors["textSecondary"] = "#7F8C8D"
        
        return FlexTheme(
            colors: colors,
            fonts: defaultTheme.fonts,
            dimensions: defaultTheme.dimensions,
            strings: defaultTheme.strings,
            icons: defaultTheme.icons,
            flags: defaultTheme.flags
        )
    }
}