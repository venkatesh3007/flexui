import UIKit
import Foundation

/// Registry for managing built-in and custom FlexUI component factories
public final class ComponentRegistry {
    
    // MARK: - Singleton
    
    public static let shared = ComponentRegistry()
    
    // MARK: - Properties
    
    private var componentFactories: [String: FlexComponentFactory] = [:]
    private let queue = DispatchQueue(label: "flexui.component_registry", attributes: .concurrent)
    
    // MARK: - Initialization
    
    private init() {
        setupBuiltInComponents()
    }
    
    // MARK: - Component Registration
    
    /// Register a custom component factory
    public func register(type: String, factory: @escaping FlexComponentFactory) {
        queue.async(flags: .barrier) { [weak self] in
            self?.componentFactories[type] = factory
        }
    }
    
    /// Unregister a component factory
    public func unregister(type: String) {
        queue.async(flags: .barrier) { [weak self] in
            self?.componentFactories.removeValue(forKey: type)
        }
    }
    
    /// Get a component factory for a type
    public func getFactory(for type: String) -> FlexComponentFactory? {
        return queue.sync {
            return componentFactories[type]
        }
    }
    
    /// Check if a component type is registered
    public func isRegistered(type: String) -> Bool {
        return queue.sync {
            return componentFactories[type] != nil
        }
    }
    
    /// Get all registered component types
    public func getRegisteredTypes() -> Set<String> {
        return queue.sync {
            return Set(componentFactories.keys)
        }
    }
    
    /// Resolve a component factory, using custom registry first, then built-in
    public func resolve(type: String) -> FlexComponentFactory? {
        return getFactory(for: type)
    }
    
    // MARK: - Built-in Components Setup
    
    private func setupBuiltInComponents() {
        // Layout components
        register(type: "container", factory: ContainerRenderer.factory)
        register(type: "row", factory: RowRenderer.factory)
        register(type: "column", factory: ColumnRenderer.factory)
        register(type: "scroll", factory: ScrollRenderer.factory)
        register(type: "spacer", factory: SpacerRenderer.factory)
        
        // Content components
        register(type: "text", factory: TextRenderer.factory)
        register(type: "image", factory: ImageRenderer.factory)
        register(type: "icon", factory: IconRenderer.factory)
        register(type: "divider", factory: DividerRenderer.factory)
        
        // Interactive components
        register(type: "button", factory: ButtonRenderer.factory)
        register(type: "input", factory: InputRenderer.factory)
        register(type: "toggle", factory: ToggleRenderer.factory)
        register(type: "slider", factory: SliderRenderer.factory)
        
        // Data components
        register(type: "list", factory: ListRenderer.factory)
        register(type: "grid", factory: GridRenderer.factory)
        register(type: "card", factory: CardRenderer.factory)
    }
    
    // MARK: - Bulk Operations
    
    /// Register multiple components at once
    public func registerMultiple(_ factories: [String: FlexComponentFactory]) {
        queue.async(flags: .barrier) { [weak self] in
            for (type, factory) in factories {
                self?.componentFactories[type] = factory
            }
        }
    }
    
    /// Clear custom components (keep built-in)
    public func clearCustomComponents() {
        queue.async(flags: .barrier) { [weak self] in
            let builtInTypes = Set([
                "container", "row", "column", "scroll", "spacer",
                "text", "image", "icon", "divider",
                "button", "input", "toggle", "slider",
                "list", "grid", "card"
            ])
            
            self?.componentFactories = self?.componentFactories.filter { key, _ in
                builtInTypes.contains(key)
            } ?? [:]
        }
    }
    
    /// Clear all components
    public func clearAll() {
        queue.async(flags: .barrier) { [weak self] in
            self?.componentFactories.removeAll()
            self?.setupBuiltInComponents()
        }
    }
    
    // MARK: - Registry Info
    
    /// Get information about registered components
    public func getRegistryInfo() -> ComponentRegistryInfo {
        return queue.sync {
            let builtInTypes = Set([
                "container", "row", "column", "scroll", "spacer",
                "text", "image", "icon", "divider",
                "button", "input", "toggle", "slider",
                "list", "grid", "card"
            ])
            
            let customTypes = Set(componentFactories.keys).subtracting(builtInTypes)
            
            return ComponentRegistryInfo(
                builtInComponents: builtInTypes,
                customComponents: customTypes,
                totalComponents: componentFactories.count
            )
        }
    }
    
    /// Debug print registry information
    public func debugPrint() {
        let info = getRegistryInfo()
        
        print("FlexUI Component Registry Info")
        print("=============================")
        print("Built-in Components (\\(info.builtInComponents.count)):")
        for component in info.builtInComponents.sorted() {
            print("  - \\(component)")
        }
        
        print("\\nCustom Components (\\(info.customComponents.count)):")
        for component in info.customComponents.sorted() {
            print("  - \\(component)")
        }
        
        print("\\nTotal: \\(info.totalComponents) components")
    }
}

// MARK: - Registry Info

/// Information about the current state of the component registry
public struct ComponentRegistryInfo {
    public let builtInComponents: Set<String>
    public let customComponents: Set<String>
    public let totalComponents: Int
    
    /// Get all component types
    public var allComponents: Set<String> {
        return builtInComponents.union(customComponents)
    }
}

// MARK: - Component Renderer Factories

// Layout Components

public enum ContainerRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let view = UIView()
        view.clipsToBounds = true
        return view
    }
}

public enum RowRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.distribution = .fill
        stack.alignment = .center
        stack.spacing = CGFloat(theme.getDimension("spacing_sm") ?? 8)
        return stack
    }
}

public enum ColumnRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let stack = UIStackView()
        stack.axis = .vertical
        stack.distribution = .fill
        stack.alignment = .fill
        stack.spacing = CGFloat(theme.getDimension("spacing_sm") ?? 8)
        return stack
    }
}

public enum ScrollRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let scrollView = UIScrollView()
        scrollView.showsVerticalScrollIndicator = true
        scrollView.showsHorizontalScrollIndicator = false
        
        let direction = props.getString("direction") ?? "vertical"
        if direction == "horizontal" {
            scrollView.showsVerticalScrollIndicator = false
            scrollView.showsHorizontalScrollIndicator = true
        }
        
        return scrollView
    }
}

public enum SpacerRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let view = UIView()
        
        if props.getBool("flex") == true {
            // Flexible spacer
            view.setContentHuggingPriority(.defaultLow, for: .vertical)
            view.setContentHuggingPriority(.defaultLow, for: .horizontal)
        } else if let size = props.getFloat("size") {
            // Fixed size spacer
            let constraint = view.heightAnchor.constraint(equalToConstant: CGFloat(size))
            constraint.isActive = true
        }
        
        return view
    }
}

// Content Components

public enum TextRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let label = UILabel()
        label.numberOfLines = 0
        
        if let content = props.getString("content") ?? props.getString("text") {
            label.text = content
        }
        
        if let fontSize = props.getFloat("fontSize") {
            label.font = UIFont.systemFont(ofSize: CGFloat(fontSize))
        }
        
        if let color = props.getColor("color", theme: theme) {
            label.textColor = color
        } else if let textColor = props.getColor("textColor", theme: theme) {
            label.textColor = textColor
        }
        
        label.textAlignment = props.getTextAlignment("textAlign")
        
        return label
    }
}

public enum ImageRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let imageView = UIImageView()
        imageView.contentMode = props.getContentMode("contentMode")
        
        if let src = props.getString("src") {
            // Load image from URL or asset
            if let url = URL(string: src), src.starts(with: "http") {
                DispatchQueue.global().async {
                    if let data = try? Data(contentsOf: url), let image = UIImage(data: data) {
                        DispatchQueue.main.async {
                            imageView.image = image
                        }
                    }
                }
            } else {
                // Try to load as asset
                imageView.image = UIImage(named: src)
            }
        }
        
        return imageView
    }
}

public enum IconRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let imageView = UIImageView()
        
        if let name = props.getString("name") {
            // Try to load SF Symbol first
            if #available(iOS 13.0, *) {
                imageView.image = UIImage(systemName: name)
            }
        }
        
        if let size = props.getFloat("size") {
            imageView.widthAnchor.constraint(equalToConstant: CGFloat(size)).isActive = true
            imageView.heightAnchor.constraint(equalToConstant: CGFloat(size)).isActive = true
        }
        
        if let color = props.getColor("color", theme: theme) {
            imageView.tintColor = color
        }
        
        return imageView
    }
}

public enum DividerRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let view = UIView()
        let thickness = CGFloat(props.getFloat("thickness") ?? 1)
        
        view.heightAnchor.constraint(equalToConstant: thickness).isActive = true
        
        if let color = props.getColor("color", theme: theme) {
            view.backgroundColor = color
        } else {
            view.backgroundColor = theme.resolveColor("border")
        }
        
        return view
    }
}

// Interactive Components

public enum ButtonRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let button = UIButton(type: .system)
        
        if let text = props.getString("text") {
            button.setTitle(text, for: .normal)
        }
        
        if let bgColor = props.getColor("backgroundColor", theme: theme) {
            button.backgroundColor = bgColor
            button.setTitleColor(.white, for: .normal)
        }
        
        button.layer.cornerRadius = CGFloat(theme.getDimension("radius_md") ?? 8)
        button.clipsToBounds = true
        
        let padding = CGFloat(theme.getDimension("spacing_md") ?? 16)
        button.contentEdgeInsets = UIEdgeInsets(top: padding, left: padding, bottom: padding, right: padding)
        
        return button
    }
}

public enum InputRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let textField = UITextField()
        
        if let placeholder = props.getString("placeholder") {
            textField.placeholder = placeholder
        }
        
        let inputType = props.getString("type") ?? "text"
        switch inputType {
        case "email":
            textField.keyboardType = .emailAddress
        case "number":
            textField.keyboardType = .numberPad
        case "password":
            textField.isSecureTextEntry = true
        default:
            textField.keyboardType = .default
        }
        
        textField.borderStyle = .roundedRect
        textField.font = UIFont.systemFont(ofSize: 16)
        
        return textField
    }
}

public enum ToggleRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let toggle = UISwitch()
        
        if let value = props.getBool("value") {
            toggle.isOn = value
        }
        
        if let onColor = props.getColor("onColor", theme: theme) {
            toggle.onTintColor = onColor
        }
        
        return toggle
    }
}

public enum SliderRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let slider = UISlider()
        
        slider.minimumValue = props.getFloat("min") ?? 0
        slider.maximumValue = props.getFloat("max") ?? 100
        slider.value = props.getFloat("value") ?? 50
        
        if let step = props.getFloat("step") {
            // Would need custom implementation for proper step handling
        }
        
        return slider
    }
}

// Data Components

public enum ListRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let tableView = UITableView(frame: .zero, style: .plain)
        tableView.separatorStyle = .singleLine
        return tableView
    }
}

public enum GridRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let layout = UICollectionViewFlowLayout()
        layout.minimumLineSpacing = CGFloat(theme.getDimension("spacing_md") ?? 16)
        layout.minimumInteritemSpacing = CGFloat(theme.getDimension("spacing_sm") ?? 8)
        
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        return collectionView
    }
}

public enum CardRenderer {
    public static let factory: FlexComponentFactory = { props, theme in
        let view = UIView()
        
        view.backgroundColor = theme.resolveColor("surface")
        view.layer.cornerRadius = CGFloat(theme.getDimension("radius_lg") ?? 12)
        view.layer.shadowColor = theme.resolveColor("shadow")?.cgColor
        view.layer.shadowOpacity = 0.1
        view.layer.shadowOffset = CGSize(width: 0, height: 2)
        view.layer.shadowRadius = 4
        
        return view
    }
}
