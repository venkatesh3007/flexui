import Foundation

/// Represents a UI component node in the FlexUI tree.
/// This is the core model that gets parsed from JSON configs.
public struct FlexNode: Codable {
    
    // MARK: - Properties
    
    /// The component type (e.g., "container", "text", "button")
    public let type: String
    
    /// Optional unique identifier for the node
    public let id: String?
    
    /// Style properties for the component
    public let style: [String: AnyCodable]?
    
    /// Child nodes for container components
    public let children: [FlexNode]?
    
    /// Visibility state of the component
    public let visibility: String
    
    /// Action to execute when the component is interacted with
    public let action: FlexAction?
    
    /// Conditional logic for rendering this node
    public let condition: FlexCondition?
    
    /// Component-specific properties
    public let props: [String: AnyCodable]?
    
    // MARK: - Initialization
    
    public init(
        type: String,
        id: String? = nil,
        style: [String: Any]? = nil,
        children: [FlexNode]? = nil,
        visibility: String = "visible",
        action: FlexAction? = nil,
        condition: FlexCondition? = nil,
        props: [String: Any]? = nil
    ) {
        self.type = type
        self.id = id
        self.style = style?.mapValues { AnyCodable($0) }
        self.children = children
        self.visibility = visibility
        self.action = action
        self.condition = condition
        self.props = props?.mapValues { AnyCodable($0) }
    }
    
    // MARK: - Public Methods
    
    /// Get a resolved style considering theme variables
    public func resolvedStyle(theme: FlexTheme) -> ResolvedStyle {
        return ResolvedStyle.from(style: style, theme: theme)
    }
    
    /// Get props accessor for this node
    public func getProps() -> FlexProps {
        let propsDict = props?.mapValues { $0.value } ?? [:]
        return FlexProps(properties: propsDict)
    }
    
    /// Check if this node should be rendered given the current context
    public func shouldRender(data: [String: Any], theme: FlexTheme) -> Bool {
        // Check visibility
        if visibility == "gone" || visibility == "hidden" {
            return false
        }
        
        // Check condition
        if let condition = condition {
            return condition.evaluate(data: data, theme: theme)
        }
        
        return true
    }
    
    /// Check if this node is a container (has children)
    public var isContainer: Bool {
        return children != nil && !children!.isEmpty
    }
    
    /// Get all child nodes that should be rendered
    public func getVisibleChildren(data: [String: Any], theme: FlexTheme) -> [FlexNode] {
        return children?.filter { child in
            child.shouldRender(data: data, theme: theme)
        } ?? []
    }
}

// MARK: - Codable Implementation

extension FlexNode {
    
    private enum CodingKeys: String, CodingKey {
        case type
        case id
        case style
        case children
        case visibility
        case action
        case condition
        case props
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        type = try container.decode(String.self, forKey: .type)
        id = try container.decodeIfPresent(String.self, forKey: .id)
        visibility = try container.decodeIfPresent(String.self, forKey: .visibility) ?? "visible"
        
        // Decode style as a dictionary of Any values
        if container.contains(.style) {
            let styleDict = try container.decode([String: AnyCodable].self, forKey: .style)
            style = styleDict
        } else {
            style = nil
        }
        
        // Decode props as a dictionary of Any values
        if container.contains(.props) {
            let propsDict = try container.decode([String: AnyCodable].self, forKey: .props)
            props = propsDict
        } else {
            props = nil
        }
        
        children = try container.decodeIfPresent([FlexNode].self, forKey: .children)
        action = try container.decodeIfPresent(FlexAction.self, forKey: .action)
        condition = try container.decodeIfPresent(FlexCondition.self, forKey: .condition)
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        
        try container.encode(type, forKey: .type)
        try container.encodeIfPresent(id, forKey: .id)
        try container.encode(visibility, forKey: .visibility)
        try container.encodeIfPresent(style, forKey: .style)
        try container.encodeIfPresent(props, forKey: .props)
        try container.encodeIfPresent(children, forKey: .children)
        try container.encodeIfPresent(action, forKey: .action)
        try container.encodeIfPresent(condition, forKey: .condition)
    }
}

// MARK: - AnyCodable Helper

/// A type-erased wrapper for Codable values
public struct AnyCodable: Codable {
    public let value: Any
    
    public init(_ value: Any) {
        self.value = value
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        
        if container.decodeNil() {
            value = NSNull()
        } else if let bool = try? container.decode(Bool.self) {
            value = bool
        } else if let int = try? container.decode(Int.self) {
            value = int
        } else if let double = try? container.decode(Double.self) {
            value = double
        } else if let string = try? container.decode(String.self) {
            value = string
        } else if let array = try? container.decode([AnyCodable].self) {
            value = array.map { $0.value }
        } else if let dictionary = try? container.decode([String: AnyCodable].self) {
            value = dictionary.mapValues { $0.value }
        } else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "Unsupported type")
        }
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        
        if value is NSNull {
            try container.encodeNil()
        } else if let bool = value as? Bool {
            try container.encode(bool)
        } else if let int = value as? Int {
            try container.encode(int)
        } else if let double = value as? Double {
            try container.encode(double)
        } else if let float = value as? Float {
            try container.encode(Double(float))
        } else if let string = value as? String {
            try container.encode(string)
        } else if let array = value as? [Any] {
            let codableArray = array.map { AnyCodable($0) }
            try container.encode(codableArray)
        } else if let dictionary = value as? [String: Any] {
            let codableDictionary = dictionary.mapValues { AnyCodable($0) }
            try container.encode(codableDictionary)
        } else {
            throw EncodingError.invalidValue(value, EncodingError.Context(codingPath: container.codingPath, debugDescription: "Unsupported type"))
        }
    }
}

// MARK: - ResolvedStyle

/// Style properties resolved with theme variables and converted to appropriate types
public struct ResolvedStyle {
    
    // Layout properties
    public let width: CGFloat?
    public let height: CGFloat?
    public let minWidth: CGFloat?
    public let minHeight: CGFloat?
    public let maxWidth: CGFloat?
    public let maxHeight: CGFloat?
    
    // Margin properties  
    public let marginTop: CGFloat
    public let marginLeft: CGFloat
    public let marginBottom: CGFloat
    public let marginRight: CGFloat
    
    // Padding properties
    public let paddingTop: CGFloat
    public let paddingLeft: CGFloat
    public let paddingBottom: CGFloat
    public let paddingRight: CGFloat
    
    // Background properties
    public let backgroundColor: UIColor?
    public let backgroundImage: String?
    
    // Border properties
    public let borderWidth: CGFloat
    public let borderColor: UIColor?
    public let borderRadius: CGFloat
    
    // Shadow properties
    public let shadowRadius: CGFloat
    public let shadowOffset: CGSize
    public let shadowColor: UIColor?
    public let shadowOpacity: Float
    
    // Other properties
    public let opacity: Float
    public let visibility: String
    
    // MARK: - Factory Method
    
    static func from(style: [String: AnyCodable]?, theme: FlexTheme) -> ResolvedStyle {
        let resolver = ThemeResolver(theme: theme)
        
        return ResolvedStyle(
            width: resolver.resolveDimension(style?["width"]?.value),
            height: resolver.resolveDimension(style?["height"]?.value),
            minWidth: resolver.resolveDimension(style?["minWidth"]?.value),
            minHeight: resolver.resolveDimension(style?["minHeight"]?.value),
            maxWidth: resolver.resolveDimension(style?["maxWidth"]?.value),
            maxHeight: resolver.resolveDimension(style?["maxHeight"]?.value),
            
            marginTop: resolver.resolveDimension(getMarginValue(style, key: "marginTop")) ?? 0,
            marginLeft: resolver.resolveDimension(getMarginValue(style, key: "marginLeft")) ?? 0,
            marginBottom: resolver.resolveDimension(getMarginValue(style, key: "marginBottom")) ?? 0,
            marginRight: resolver.resolveDimension(getMarginValue(style, key: "marginRight")) ?? 0,
            
            paddingTop: resolver.resolveDimension(getPaddingValue(style, key: "paddingTop")) ?? 0,
            paddingLeft: resolver.resolveDimension(getPaddingValue(style, key: "paddingLeft")) ?? 0,
            paddingBottom: resolver.resolveDimension(getPaddingValue(style, key: "paddingBottom")) ?? 0,
            paddingRight: resolver.resolveDimension(getPaddingValue(style, key: "paddingRight")) ?? 0,
            
            backgroundColor: resolver.resolveColor(style?["backgroundColor"]?.value),
            backgroundImage: style?["backgroundImage"]?.value as? String,
            
            borderWidth: resolver.resolveDimension(style?["borderWidth"]?.value) ?? 0,
            borderColor: resolver.resolveColor(style?["borderColor"]?.value),
            borderRadius: resolver.resolveDimension(style?["borderRadius"]?.value) ?? 0,
            
            shadowRadius: resolver.resolveDimension(style?["shadowRadius"]?.value) ?? 0,
            shadowOffset: CGSize(
                width: resolver.resolveDimension(style?["shadowOffsetX"]?.value) ?? 0,
                height: resolver.resolveDimension(style?["shadowOffsetY"]?.value) ?? 0
            ),
            shadowColor: resolver.resolveColor(style?["shadowColor"]?.value),
            shadowOpacity: (style?["shadowOpacity"]?.value as? Float) ?? 0,
            
            opacity: (style?["opacity"]?.value as? Float) ?? 1,
            visibility: (style?["visibility"]?.value as? String) ?? "visible"
        )
    }
    
    // Helper methods for extracting margin values
    private static func getMarginValue(_ style: [String: AnyCodable]?, key: String) -> Any? {
        if let value = style?[key]?.value {
            return value
        }
        
        // Check for shorthand margin property
        if let margin = style?["margin"]?.value {
            return margin
        }
        
        return nil
    }
    
    // Helper methods for extracting padding values
    private static func getPaddingValue(_ style: [String: AnyCodable]?, key: String) -> Any? {
        if let value = style?[key]?.value {
            return value
        }
        
        // Check for shorthand padding property
        if let padding = style?["padding"]?.value {
            return padding
        }
        
        return nil
    }
}

// MARK: - Convenience Extensions

public extension FlexNode {
    
    /// Create a simple text node
    static func text(_ content: String, style: [String: Any]? = nil) -> FlexNode {
        var props: [String: Any] = ["content": content]
        return FlexNode(type: "text", style: style, props: props)
    }
    
    /// Create a simple container node
    static func container(children: [FlexNode], style: [String: Any]? = nil) -> FlexNode {
        return FlexNode(type: "container", style: style, children: children)
    }
    
    /// Create a simple button node
    static func button(_ text: String, action: FlexAction? = nil, style: [String: Any]? = nil) -> FlexNode {
        var props: [String: Any] = ["text": text]
        return FlexNode(type: "button", style: style, action: action, props: props)
    }
}