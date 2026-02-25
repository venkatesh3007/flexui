# FlexUI — Server-Driven Native UI SDK

## What Is This?

A lightweight library that renders native UI components from JSON configurations fetched from a server. Allows SDK developers (like Flyy) to give their B2B customers UI customization without app updates.

## Core Principle

**JSON in → Native UI out.**

The SDK ships with a set of registered components. The server sends a JSON layout describing which components to render, how to style them, and how they behave. The SDK renders them as fully native views (not WebViews).

---

## Architecture

```
Server (JSON config)
       │
       ▼
┌──────────────┐
│  FlexUI SDK  │
├──────────────┤
│ Config Layer │ ← Fetches, caches, validates JSON configs
├──────────────┤
│ Schema Layer │ ← Parses JSON into typed component tree
├──────────────┤
│ Render Layer │ ← Maps component tree to native views
├──────────────┤
│ Theme Layer  │ ← Applies colors, fonts, spacing, borders
├──────────────┤
│ Action Layer │ ← Handles taps, navigations, callbacks to host app
├──────────────┤
│ Component    │ ← Registry of built-in + custom components
│ Registry     │
└──────────────┘
```

---

## Platforms

### Android
- **Language**: Kotlin
- **Min SDK**: 21 (Android 5.0)
- **UI Framework**: Jetpack Compose (primary) + View XML fallback
- **Distribution**: Maven Central / JitPack
- **Build**: Gradle (Kotlin DSL)

### iOS
- **Language**: Swift
- **Min iOS**: 15.0
- **UI Framework**: SwiftUI (primary) + UIKit fallback
- **Distribution**: Swift Package Manager (SPM)
- **Build**: Xcode project + SPM package

---

## JSON Schema

### Root Config

```json
{
  "version": "1.0",
  "screenId": "scratch_card_reward",
  "theme": { ... },
  "root": { ... },
  "actions": { ... }
}
```

### Theme Object

```json
{
  "theme": {
    "colors": {
      "primary": "#FF6B00",
      "secondary": "#1A1A2E",
      "background": "#FFFFFF",
      "text": "#333333",
      "textSecondary": "#999999",
      "success": "#4CAF50",
      "error": "#F44336"
    },
    "typography": {
      "headingFont": "Poppins-Bold",
      "bodyFont": "Poppins-Regular",
      "headingSize": 24,
      "bodySize": 14,
      "captionSize": 12
    },
    "spacing": {
      "xs": 4,
      "sm": 8,
      "md": 16,
      "lg": 24,
      "xl": 32
    },
    "borderRadius": {
      "sm": 4,
      "md": 8,
      "lg": 16,
      "full": 999
    }
  }
}
```

### Component Node

Every UI element is a node with this structure:

```json
{
  "type": "container",
  "id": "hero_section",
  "style": {
    "padding": "{{spacing.md}}",
    "backgroundColor": "{{colors.background}}",
    "borderRadius": "{{borderRadius.lg}}",
    "margin": { "top": 8, "bottom": 8, "left": 16, "right": 16 }
  },
  "children": [ ... ],
  "visibility": "visible",
  "action": { ... }
}
```

### Built-in Component Types

#### Layout Components
| Type | Description | Properties |
|---|---|---|
| `container` | Box/frame with styling | padding, margin, background, border, shadow, children |
| `row` | Horizontal stack | spacing, alignment, children |
| `column` | Vertical stack | spacing, alignment, children |
| `scroll` | Scrollable container | direction (vertical/horizontal), children |
| `spacer` | Flexible space | size (fixed) or flex (flexible) |

#### Content Components
| Type | Description | Properties |
|---|---|---|
| `text` | Label/text | content, style (font, size, color, alignment, maxLines) |
| `image` | Image (URL or asset) | src, placeholder, aspectRatio, contentMode |
| `icon` | Icon from bundled set | name, size, color |
| `divider` | Horizontal/vertical line | color, thickness, margin |

#### Interactive Components
| Type | Description | Properties |
|---|---|---|
| `button` | Tappable button | text, style, action, loading state |
| `input` | Text input field | placeholder, type (text/email/number), validation |
| `toggle` | Switch/toggle | value, onChange action |
| `slider` | Value slider | min, max, step, value |

#### Data Components
| Type | Description | Properties |
|---|---|---|
| `list` | Repeating items | items (data array), template (component tree), separator |
| `grid` | Grid layout | columns, items, template |
| `card` | Card container | elevated, children |

#### Special Components (Custom Registry)
| Type | Description | Notes |
|---|---|---|
| `custom` | Host-app registered component | type maps to registered native view |

### Actions

```json
{
  "action": {
    "type": "navigate",
    "screen": "wallet"
  }
}

{
  "action": {
    "type": "callback",
    "event": "claim_reward",
    "data": { "rewardId": "{{reward.id}}" }
  }
}

{
  "action": {
    "type": "openUrl",
    "url": "https://example.com/terms"
  }
}

{
  "action": {
    "type": "dismiss"
  }
}
```

### Template Variables

Variables use `{{key}}` syntax and are resolved at render time:

- `{{theme.colors.primary}}` → Theme value
- `{{data.user.name}}` → Data binding
- `{{campaign.banner_url}}` → Server-provided data
- `{{device.platform}}` → Runtime context (android/ios)

### Conditional Rendering

```json
{
  "type": "container",
  "condition": {
    "if": "{{data.rewards.count}}",
    "operator": ">",
    "value": 0
  },
  "children": [ ... ]
}
```

---

## SDK API Design

### Android (Kotlin)

```kotlin
// 1. Initialize
FlexUI.init(context) {
    baseUrl("https://config.flyy.dev/flexui")
    cachePolicy(CachePolicy.CACHE_FIRST)
    defaultTheme(FlexTheme.fromAsset("default_theme.json"))
}

// 2. Register custom components
FlexUI.registerComponent("scratch_card") { props, theme ->
    ScratchCardView(props, theme)  // Your native composable/view
}

// 3. Register action handlers
FlexUI.onAction("claim_reward") { data ->
    // Handle in host app
    rewardManager.claim(data["rewardId"])
}

// 4. Render a screen
// In Compose:
@Composable
fun RewardScreen(campaignId: String) {
    FlexScreen(
        screenId = "scratch_card_reward",
        data = mapOf("campaignId" to campaignId),
        onAction = { action -> handleAction(action) },
        loading = { CircularProgressIndicator() },
        error = { FlexErrorView(it) }
    )
}

// In XML/View system:
val flexView = FlexUI.renderView(
    context = this,
    screenId = "scratch_card_reward",
    data = mapOf("campaignId" to campaignId)
)
container.addView(flexView)
```

### iOS (Swift)

```swift
// 1. Initialize
FlexUI.configure {
    $0.baseURL = URL(string: "https://config.flyy.dev/flexui")!
    $0.cachePolicy = .cacheFirst
    $0.defaultTheme = FlexTheme.fromBundle("default_theme")
}

// 2. Register custom components
FlexUI.registerComponent("scratch_card") { props, theme in
    ScratchCardView(props: props, theme: theme)  // Your native SwiftUI view
}

// 3. Register action handlers
FlexUI.onAction("claim_reward") { data in
    RewardManager.shared.claim(rewardId: data["rewardId"])
}

// 4. Render a screen
// In SwiftUI:
struct RewardScreen: View {
    let campaignId: String
    
    var body: some View {
        FlexScreen(
            screenId: "scratch_card_reward",
            data: ["campaignId": campaignId]
        ) { action in
            handleAction(action)
        }
    }
}

// In UIKit:
let flexVC = FlexUI.viewController(
    screenId: "scratch_card_reward",
    data: ["campaignId": campaignId]
)
present(flexVC, animated: true)
```

---

## Config Fetching & Caching

```
App Launch
    │
    ▼
Check local cache
    │
    ├── Cache hit + not expired → Use cached config
    │
    ├── Cache hit + expired → Show cached, fetch new in background
    │
    └── Cache miss → Fetch from server, show loading
    
Fetch: GET {baseUrl}/screens/{screenId}?version={appVersion}&platform={android|ios}

Response cached to:
  - Memory (LRU, 50 screens max)
  - Disk (SQLite on Android, UserDefaults/FileManager on iOS)
  
Cache TTL: Configurable (default 1 hour)
Force refresh: FlexUI.refreshConfig(screenId)
```

---

## File Structure

### Android Library
```
flexui-android/
├── flexui/                          # Library module
│   ├── src/main/kotlin/dev/flexui/
│   │   ├── FlexUI.kt               # Main entry point
│   │   ├── FlexScreen.kt           # Compose entry point
│   │   ├── config/
│   │   │   ├── ConfigFetcher.kt    # HTTP + caching
│   │   │   ├── ConfigCache.kt      # Disk + memory cache
│   │   │   └── ConfigValidator.kt  # Schema validation
│   │   ├── schema/
│   │   │   ├── FlexNode.kt         # Component tree model
│   │   │   ├── FlexTheme.kt        # Theme model
│   │   │   ├── FlexAction.kt       # Action model
│   │   │   ├── FlexCondition.kt    # Conditional logic
│   │   │   └── FlexParser.kt       # JSON → model parsing
│   │   ├── render/
│   │   │   ├── FlexRenderer.kt     # Compose renderer
│   │   │   ├── FlexViewRenderer.kt # XML/View renderer (fallback)
│   │   │   └── components/         # Built-in component renderers
│   │   │       ├── ContainerRenderer.kt
│   │   │       ├── TextRenderer.kt
│   │   │       ├── ImageRenderer.kt
│   │   │       ├── ButtonRenderer.kt
│   │   │       ├── ListRenderer.kt
│   │   │       └── ...
│   │   ├── theme/
│   │   │   ├── ThemeResolver.kt    # Variable resolution
│   │   │   └── ThemeDefaults.kt    # Default theme values
│   │   ├── actions/
│   │   │   ├── ActionHandler.kt    # Action dispatch
│   │   │   └── ActionRegistry.kt   # Custom action handlers
│   │   └── registry/
│   │       └── ComponentRegistry.kt # Custom component registration
│   ├── src/test/                    # Unit tests
│   └── build.gradle.kts
├── sample/                          # Example app
│   ├── src/main/
│   └── build.gradle.kts
├── build.gradle.kts                 # Root build file
├── settings.gradle.kts
└── gradle.properties
```

### iOS Library
```
flexui-ios/
├── Sources/FlexUI/
│   ├── FlexUI.swift                 # Main entry point
│   ├── FlexScreen.swift             # SwiftUI entry point
│   ├── Config/
│   │   ├── ConfigFetcher.swift      # HTTP + caching
│   │   ├── ConfigCache.swift        # Disk + memory cache
│   │   └── ConfigValidator.swift    # Schema validation
│   ├── Schema/
│   │   ├── FlexNode.swift           # Component tree model
│   │   ├── FlexTheme.swift          # Theme model
│   │   ├── FlexAction.swift         # Action model
│   │   ├── FlexCondition.swift      # Conditional logic
│   │   └── FlexParser.swift         # JSON → model parsing
│   ├── Render/
│   │   ├── FlexRenderer.swift       # SwiftUI renderer
│   │   ├── FlexUIKitRenderer.swift  # UIKit renderer (fallback)
│   │   └── Components/             # Built-in component renderers
│   │       ├── ContainerRenderer.swift
│   │       ├── TextRenderer.swift
│   │       ├── ImageRenderer.swift
│   │       ├── ButtonRenderer.swift
│   │       ├── ListRenderer.swift
│   │       └── ...
│   ├── Theme/
│   │   ├── ThemeResolver.swift      # Variable resolution
│   │   └── ThemeDefaults.swift      # Default theme values
│   ├── Actions/
│   │   ├── ActionHandler.swift      # Action dispatch
│   │   └── ActionRegistry.swift     # Custom action handlers
│   └── Registry/
│       └── ComponentRegistry.swift  # Custom component registration
├── Tests/FlexUITests/               # Unit tests
├── Sample/                          # Example Xcode project
│   ├── FlexUISample/
│   └── FlexUISample.xcodeproj
├── Package.swift                    # SPM package definition
└── FlexUI.podspec                   # CocoaPods (optional)
```

---

## Dependencies

### Android
- **kotlinx.serialization** — JSON parsing (no Gson/Moshi needed)
- **kotlinx.coroutines** — Async config fetching
- **OkHttp** — HTTP client (most Android apps already have it)
- **Coil** — Image loading (lightweight, Compose-native)
- Zero Compose dependencies beyond what's in the app already

### iOS
- **Foundation** — JSON parsing (Codable, no third-party)
- **Combine** — Async config fetching
- **URLSession** — HTTP client (built-in)
- **SwiftUI** — Rendering (built-in)
- **Zero third-party dependencies**

---

## What's NOT In Scope (v1)

- Visual editor / dashboard (future product)
- Config hosting server (Flyy uses their own backend)
- Animations beyond basic transitions
- Complex gestures (drag, pinch)
- Video/media player components
- Form validation rules engine
- Offline-first with conflict resolution

---

## Testing Strategy

### Unit Tests
- JSON parsing (valid configs, malformed configs, edge cases)
- Theme resolution (variable substitution, defaults, overrides)
- Condition evaluation (comparisons, boolean logic)
- Action dispatch (routing, data passing)
- Cache behavior (TTL, eviction, memory/disk)

### Integration Tests
- Full render pipeline: JSON → component tree → native view hierarchy
- Config fetch → cache → render flow
- Custom component registration and rendering

### Example App
- Shows all built-in components with live config editing
- Demonstrates custom component registration
- Theme switching (light/dark/custom)
- Action handling examples

---

## Documentation Structure

```
docs/
├── README.md                    # Quick start (< 5 minutes to first render)
├── INTEGRATION_ANDROID.md       # Step-by-step Android setup
├── INTEGRATION_IOS.md           # Step-by-step iOS setup
├── SCHEMA_REFERENCE.md          # Complete JSON schema docs
├── COMPONENTS.md                # Every built-in component with examples
├── THEMING.md                   # Theme system guide
├── CUSTOM_COMPONENTS.md         # How to register your own components
├── ACTIONS.md                   # Action system and callbacks
├── CACHING.md                   # Config caching behavior
├── MIGRATION.md                 # Migrating from hardcoded UI
└── EXAMPLES.md                  # Real-world config examples
```
