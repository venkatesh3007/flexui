# FlexUI iOS - Server-Driven Native UI SDK

A lightweight Swift library that renders native UIKit components from JSON configurations fetched from a server. Perfect for SDK developers who need to deliver UI customization to B2B customers without app updates.

## Features

- 🎨 **14 Built-in Components** - Layouts, content, interactions, and data components
- 📱 **UIKit Native** - Programmatic views, no SwiftUI or WebView dependencies
- 🎯 **Zero External Dependencies** - Built entirely on Foundation + UIKit
- 🔧 **Custom Components** - Register your own component factories
- 🎭 **Theme System** - Comprehensive theme support with variable resolution
- 🚀 **Server-Driven** - Fetch configs from your backend
- 💾 **Smart Caching** - Memory and disk caching with TTL
- 🔐 **Type-Safe** - Full Swift/Codable support
- 📐 **Flexible Styling** - Support for colors, fonts, spacing, borders, shadows
- 🎬 **Actions** - Navigate, callbacks, open URLs, animations, and more

## Installation

### Swift Package Manager

```swift
dependencies: [
    .package(url: "https://github.com/flexui/flexui.git", from: "1.0.0")
]
```

### CocoaPods

```ruby
pod 'FlexUI', '~> 1.0.0'
```

## Quick Start

### 1. Initialize FlexUI

```swift
import FlexUI

FlexUI.configure(baseURL: "https://api.example.com/flexui")
```

### 2. Register Custom Components (Optional)

```swift
FlexUI.registerComponent("scratch_card") { props, theme in
    let view = ScratchCardView()
    view.scratchColor = theme.resolveColor("primary")
    return view
}
```

### 3. Handle Actions (Optional)

```swift
FlexUI.onEvent("claim_reward") { action in
    let rewardId = action.getString("rewardId")
    print("User claimed reward: \(rewardId ?? "")")
}
```

### 4. Render a Screen

```swift
// Synchronously
if let flexView = FlexUI.render(screenId: "reward_screen", data: ["userId": "123"]) {
    container.addSubview(flexView)
}

// Asynchronously
FlexUI.renderAsync(screenId: "reward_screen") { result in
    switch result {
    case .success(let view):
        self.container.addSubview(view)
    case .failure(let error):
        print("Render error: \(error)")
    }
}

// As a view controller
let vc = FlexUI.viewController(screenId: "reward_screen")
present(vc, animated: true)
```

## Configuration

```swift
let config = FlexConfig(
    baseURL: "https://api.example.com/flexui",
    cachePolicy: .cacheFirst,
    cacheTTL: 3600,
    defaultTheme: customTheme,
    connectTimeout: 10,
    readTimeout: 30
)

FlexUI.configure(config)
```

## Built-in Components

### Layout Components
- **container** - Box/frame with styling
- **row** - Horizontal stack
- **column** - Vertical stack
- **scroll** - Scrollable container
- **spacer** - Flexible/fixed space

### Content Components
- **text** - Styled text/label
- **image** - Image from URL or asset
- **icon** - SF Symbols
- **divider** - Horizontal/vertical line

### Interactive Components
- **button** - Tappable button
- **input** - Text input field
- **toggle** - Switch/toggle
- **slider** - Value slider

### Data Components
- **list** - Repeating items (table)
- **grid** - Grid layout (collection)
- **card** - Elevated card container

## Example JSON Config

```json
{
  "version": "1.0",
  "screenId": "reward_screen",
  "theme": {
    "colors": {
      "primary": "#FF6B00",
      "text": "#333333"
    },
    "dimensions": {
      "spacing": 16,
      "radius": 8
    }
  },
  "root": {
    "type": "column",
    "style": {
      "padding": "{{spacing}}",
      "backgroundColor": "#FFFFFF"
    },
    "children": [
      {
        "type": "text",
        "props": {
          "content": "Congratulations!",
          "fontSize": 24
        }
      },
      {
        "type": "button",
        "props": {
          "text": "Claim Reward"
        },
        "action": {
          "type": "callback",
          "data": {
            "event": "claim_reward",
            "rewardId": "{{data.rewardId}}"
          }
        }
      }
    ]
  }
}
```

## Theme System

### Using Default Theme

```swift
let theme = ThemeDefaults.defaultTheme
// Comes with colors, fonts, spacing, and more
```

### Custom Theme

```swift
let customTheme = FlexTheme(
    colors: [
        "primary": "#007AFF",
        "secondary": "#5856D6"
    ],
    fonts: [
        "heading": FontDefinition(family: "System", size: 24, weight: "bold")
    ],
    dimensions: [
        "spacing_sm": 8,
        "spacing_md": 16,
        "spacing_lg": 24
    ]
)

FlexUI.setDefaultTheme(customTheme)
```

### Theme Merging

```swift
let baseTheme = ThemeDefaults.defaultTheme
let customTheme = FlexTheme(colors: ["primary": "#FF0000"])
let merged = baseTheme.merged(with: customTheme)
```

## Variable Resolution

### Theme Variables

```json
{
  "style": {
    "backgroundColor": "{{colors.primary}}",
    "padding": "{{spacing_md}}"
  }
}
```

### Data Variables

```json
{
  "type": "text",
  "props": {
    "content": "Welcome, {{user.name}}!"
  }
}
```

### Device Variables

```json
{
  "style": {
    "height": "{{device.platform}}"
  }
}
```

## Conditional Rendering

```json
{
  "type": "container",
  "condition": {
    "if": "{{data.isPremium}}",
    "operator": "==",
    "value": true
  }
}
```

Supported operators: `==`, `!=`, `>`, `<`, `>=`, `<=`, `contains`, `in`, `startsWith`, `endsWith`, `regex`

## Actions

### Navigate

```swift
FlexUI.onAction("navigate") { action in
    guard let screen = action.getString("screen") else { return }
    // Navigate to screen
}
```

### Callback

```swift
FlexUI.onEvent("button_clicked") { action in
    print("Button clicked: \(action.getAllData())")
}
```

### Open URL

```json
{
  "action": {
    "type": "openUrl",
    "data": {
      "url": "https://example.com",
      "external": true
    }
  }
}
```

### Custom Actions

```swift
FlexUI.registerComponent("custom_component") { props, theme in
    let button = UIButton()
    
    // Custom action type
    button.addAction(UIAction { _ in
        let action = FlexAction.custom(
            type: "myCustomAction",
            data: ["param": "value"]
        )
        ActionDispatcher.shared.dispatch(action)
    }, for: .touchUpInside)
    
    return button
}
```

## Caching

### Cache Policies

```swift
// Cache first, fetch in background
FlexUI.configure(baseURL: "...", cachePolicy: .cacheFirst)

// Network first, fallback to cache
FlexUI.configure(baseURL: "...", cachePolicy: .networkFirst)

// Cache only (offline)
FlexUI.configure(baseURL: "...", cachePolicy: .cacheOnly)

// Network only (no caching)
FlexUI.configure(baseURL: "...", cachePolicy: .networkOnly)
```

### Manual Cache Management

```swift
// Clear specific config
FlexUI.refreshConfig(screenId: "reward_screen")

// Clear all cache
FlexUI.clearCache()

// Get cache stats
let stats = ConfigCache.shared.getCacheStats()
print("Disk cache: \(stats.diskSizeMB)MB")
```

## Error Handling

```swift
do {
    let config = try FlexParser.parseConfig(jsonString)
    try FlexParser.validateConfig(config)
} catch let error as FlexError {
    switch error {
    case .parseError(let message):
        print("Parse error: \(message)")
    case .renderError(let message, let underlying):
        print("Render error: \(message)")
    case .networkError(let message):
        print("Network error: \(message)")
    default:
        break
    }
}
```

## Testing

Run tests with:

```bash
swift test
```

Tests cover:
- JSON parsing
- Theme resolution  
- Condition evaluation
- Component rendering
- Action dispatch
- Custom components

## Performance

- **Minimal footprint** - ~150KB framework size
- **Zero external dependencies** - UIKit only
- **Smart caching** - Memory + disk cache
- **Efficient rendering** - Reuses UIView system

## Debugging

### Enable Debug Logging

```swift
let theme = ThemeDefaults.defaultTheme
theme.flags["debug"] = true
```

### Inspect Registry

```swift
ComponentRegistry.shared.debugPrint()
ActionRegistry.shared.debugPrint()
```

### Cache Stats

```swift
let stats = ConfigCache.shared.getCacheStats()
print("Cached screens: \(stats.diskEntries)")
```

## Best Practices

1. **Initialize once** - Call `FlexUI.configure()` in your app delegate
2. **Register components early** - Do custom registration before rendering
3. **Use caching** - Let FlexUI handle offline mode
4. **Validate configs** - Test JSON configs before deployment
5. **Handle errors gracefully** - Always provide fallback UIs
6. **Theme consistency** - Use theme variables instead of hardcoded values

## Troubleshooting

### "Component type not found"

Make sure the component is registered:

```swift
FlexUI.registerComponent("my_component") { props, theme in
    // Factory
}
```

### "Variable not resolved"

Check variable syntax:
- Theme: `{{colors.primary}}`
- Data: `{{user.name}}`
- Device: `{{device.platform}}`

### Poor performance

- Enable caching: `.cacheFirst` policy
- Use images wisely
- Limit nesting depth
- Profile with Instruments

## API Reference

See [ARCHITECTURE.md](../ARCHITECTURE.md) for complete API reference and implementation details.

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md)

## License

MIT License - See LICENSE file

## Support

- 📖 [Documentation](../docs)
- 🐛 [Issue Tracker](https://github.com/flexui/flexui/issues)
- 💬 [Discussions](https://github.com/flexui/flexui/discussions)

---

**Built for SDK developers who need UI flexibility without UI overhead.**