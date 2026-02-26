# FlexUI — Server-Driven Native UI SDK

## What Is This?

A lightweight library that renders native UI components from JSON configurations fetched from a server. Allows SDK developers (like Flyy) to give their B2B customers UI customization without app updates.

## Core Principle

**JSON in → Native UI out.**

The SDK ships with a set of registered components. The server sends a JSON layout describing which components to render, how to style them, and how they behave. The SDK renders them as fully native views (not WebViews).

## Design Constraint: SDK-Friendly

FlexUI is designed to be **embedded inside other SDKs** (like Flyy), not directly in end-user apps. This means:

- **No heavy dependencies** — can't force Compose/SwiftUI on SDK consumers
- **Java-compatible API** — many SDKs still have Java callers
- **Tiny footprint** — target ~200KB, not 5MB
- **View-system native** — programmatic Views on Android, UIKit on iOS
- **Drop-in replacement** for existing `LayoutInflater.inflate()` / `UIView` patterns

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
│ Render Layer │ ← Maps component tree to native Views (android.view.View / UIView)
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
- **Language**: Kotlin internals, **Java-compatible public API**
- **Min SDK**: 21 (Android 5.0)
- **UI Framework**: **View system (programmatic)** — LinearLayout, FrameLayout, TextView, ImageView, etc.
- **No Compose dependency** — zero Compose runtime overhead
- **Distribution**: Maven Central / JitPack
- **Build**: Gradle (Kotlin DSL)
- **Target size**: ~200KB

### iOS
- **Language**: Swift
- **Min iOS**: 13.0
- **UI Framework**: **UIKit (programmatic)** — UIView, UIStackView, UILabel, UIImageView, etc.
- **No SwiftUI dependency** — works in any UIKit-based SDK
- **Distribution**: Swift Package Manager (SPM) + CocoaPods
- **Build**: Xcode project + SPM package
- **Target size**: ~150KB

---

## Integration Pattern: Drop-in LayoutInflater Replacement

### How It Works Inside Flyy's Existing SDK (Android)

```java
// Inside Flyy's existing Java SDK code:

// 1. Initialize (once, in Flyy SDK init)
FlexUI.init(context, "https://config.flyy.dev/flexui");

// 2. Register Flyy's custom components
FlexUI.registerComponent("scratch_card", new ScratchCardViewFactory());
FlexUI.registerComponent("leaderboard", new LeaderboardViewFactory());

// 3. Render anywhere Flyy currently inflates a layout
View rewardScreen = FlexUI.render(context, "reward_screen", data);
container.addView(rewardScreen);

// Or replace an existing inflate call:
// OLD: View view = LayoutInflater.from(context).inflate(R.layout.reward_screen, parent, false);
// NEW: View view = FlexUI.render(context, "reward_screen", data);
```

### Custom Component Factory (Java-friendly)

```java
// Flyy registers their scratch card as a FlexUI component
public class ScratchCardViewFactory implements FlexComponentFactory {
    @Override
    public View create(Context context, FlexProps props, FlexTheme theme) {
        // Flyy's existing ScratchCardView, just themed dynamically
        ScratchCardView view = new ScratchCardView(context);
        view.setScratchColor(theme.getColor("primary"));
        view.setRevealText(props.getString("reward_text"));
        return view;
    }
}
```

### How It Works Inside Flyy's Existing SDK (iOS / UIKit)

```swift
// Inside Flyy's existing UIKit-based SDK:

// 1. Initialize (once, in Flyy SDK init)
FlexUI.configure(baseURL: "https://config.flyy.dev/flexui")

// 2. Register Flyy's custom components
FlexUI.registerComponent("scratch_card") { props, theme in
    let view = ScratchCardView()
    view.scratchColor = theme.color("primary")
    view.revealText = props.string("reward_text")
    return view
}

// 3. Render anywhere Flyy currently creates a UIView
let rewardView = FlexUI.render(screenId: "reward_screen", data: data)
container.addSubview(rewardView)

// Or replace existing view controller presentation:
let flexVC = FlexUI.viewController(screenId: "reward_screen", data: data)
present(flexVC, animated: true)
```

---

## Why View System, Not Compose/SwiftUI

| Aspect | Compose/SwiftUI | View/UIKit (chosen) |
|---|---|---|
| Primary UI | Jetpack Compose / SwiftUI | View system / UIKit (programmatic) |
| Language | Kotlin-only / Swift-only | Kotlin internals + Java-compatible API / Swift |
| Min SDK | 21 (but adds ~5MB) | 21 / iOS 13 (no overhead) |
| Rendering | Composables / SwiftUI Views | View, LinearLayout, etc. / UIView, UIStackView, etc. |
| Integration | Requires Compose in host app | Drop-in replacement for LayoutInflater / UIView |
| Size impact | ~5MB (Compose runtime) | ~200KB / ~150KB |
| Dependency conflict | Version conflicts with host app's Compose | None — View system is stable |
| Java interop | Poor — Compose is Kotlin-only | Full — Java-compatible API |

**Bottom line**: When your library lives inside another SDK, you can't dictate the UI framework. View/UIKit is the universal common denominator.

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
| `custom` | Host-app registered component | type maps to registered native view via FlexComponentFactory |

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

### Android (Kotlin internals, Java-compatible API)

```kotlin
// --- Public API (Java-callable) ---

object FlexUI {
    @JvmStatic
    fun init(context: Context, baseUrl: String) { ... }
    
    @JvmStatic
    fun init(context: Context, config: FlexConfig) { ... }
    
    @JvmStatic
    fun registerComponent(type: String, factory: FlexComponentFactory) { ... }
    
    @JvmStatic
    fun onAction(event: String, handler: FlexActionHandler) { ... }
    
    @JvmStatic
    fun render(context: Context, screenId: String, data: Map<String, Any>): View { ... }
    
    @JvmStatic
    fun renderAsync(context: Context, screenId: String, data: Map<String, Any>, 
                    callback: FlexRenderCallback) { ... }
    
    @JvmStatic
    fun refreshConfig(screenId: String) { ... }
    
    @JvmStatic
    fun clearCache() { ... }
}

// --- Interfaces (Java-friendly) ---

interface FlexComponentFactory {
    fun create(context: Context, props: FlexProps, theme: FlexTheme): View
}

interface FlexActionHandler {
    fun handle(action: FlexAction)
}

interface FlexRenderCallback {
    fun onSuccess(view: View)
    fun onError(error: FlexError)
}

// --- Config ---

class FlexConfig private constructor(
    val baseUrl: String,
    val cachePolicy: CachePolicy,
    val cacheTtlMs: Long,
    val defaultTheme: FlexTheme?,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long
) {
    class Builder(private val baseUrl: String) {
        fun cachePolicy(policy: CachePolicy): Builder = ...
        fun cacheTtlMs(ttl: Long): Builder = ...
        fun defaultTheme(theme: FlexTheme): Builder = ...
        fun build(): FlexConfig = ...
    }
}

enum class CachePolicy {
    CACHE_FIRST,      // Use cache if available, fetch in background
    NETWORK_FIRST,    // Always fetch, fallback to cache
    CACHE_ONLY,       // Never fetch (offline mode)
    NETWORK_ONLY      // Never cache
}
```

### iOS (Swift, UIKit-based)

```swift
public final class FlexUI {
    
    public static func configure(baseURL: String) { ... }
    
    public static func configure(_ config: FlexConfig) { ... }
    
    public static func registerComponent(_ type: String, 
                                          factory: @escaping (FlexProps, FlexTheme) -> UIView) { ... }
    
    public static func onAction(_ event: String, 
                                 handler: @escaping (FlexAction) -> Void) { ... }
    
    public static func render(screenId: String, data: [String: Any] = [:]) -> UIView { ... }
    
    public static func renderAsync(screenId: String, data: [String: Any] = [:],
                                    completion: @escaping (Result<UIView, FlexError>) -> Void) { ... }
    
    public static func viewController(screenId: String, data: [String: Any] = [:]) -> UIViewController { ... }
    
    public static func refreshConfig(screenId: String) { ... }
    
    public static func clearCache() { ... }
}

public struct FlexConfig {
    public var baseURL: String
    public var cachePolicy: CachePolicy = .cacheFirst
    public var cacheTTL: TimeInterval = 3600
    public var defaultTheme: FlexTheme? = nil
    public var connectTimeout: TimeInterval = 10
    public var readTimeout: TimeInterval = 30
}

public enum CachePolicy {
    case cacheFirst
    case networkFirst
    case cacheOnly
    case networkOnly
}
```

---

## Rendering Engine (Android)

The render engine maps JSON component types to programmatic View construction:

```
FlexNode (parsed JSON)
    │
    ▼
ComponentRegistry.resolve(node.type)
    │
    ├── Built-in type → FlexViewFactory creates View programmatically
    │   "container" → FrameLayout with padding/margin/background
    │   "row"       → LinearLayout(HORIZONTAL) with spacing
    │   "column"    → LinearLayout(VERTICAL) with spacing
    │   "text"      → TextView with styled text
    │   "image"     → ImageView with URL loading
    │   "button"    → AppCompatButton with action binding
    │   "scroll"    → ScrollView/HorizontalScrollView
    │   "list"      → RecyclerView with FlexAdapter
    │   "grid"      → RecyclerView with GridLayoutManager
    │   "card"      → CardView (or FrameLayout with elevation)
    │   "input"     → EditText with validation
    │   "toggle"    → SwitchCompat
    │   "divider"   → View with height=1dp
    │   "spacer"    → Space or View with layout weight
    │
    └── Custom type → FlexComponentFactory.create() (registered by host)
```

### View Construction (no XML, no Compose)

```kotlin
// Example: how "container" renders
internal class ContainerRenderer : FlexViewFactory {
    override fun create(context: Context, node: FlexNode, theme: FlexTheme, 
                        renderer: FlexRenderer): View {
        val layout = FrameLayout(context)
        
        // Apply style
        val style = node.resolvedStyle(theme)
        layout.setPadding(style.paddingLeft, style.paddingTop, style.paddingRight, style.paddingBottom)
        
        val params = ViewGroup.MarginLayoutParams(
            style.width ?: ViewGroup.LayoutParams.MATCH_PARENT,
            style.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(style.marginLeft, style.marginTop, style.marginRight, style.marginBottom)
        layout.layoutParams = params
        
        // Background
        val bg = GradientDrawable()
        bg.setColor(style.backgroundColor)
        bg.cornerRadius = style.borderRadius.toFloat()
        if (style.borderWidth > 0) {
            bg.setStroke(style.borderWidth, style.borderColor)
        }
        layout.background = bg
        
        // Shadow/elevation
        if (style.elevation > 0) {
            layout.elevation = style.elevation.toFloat()
        }
        
        // Render children recursively
        node.children?.forEach { child ->
            layout.addView(renderer.render(context, child, theme))
        }
        
        // Bind action
        node.action?.let { action ->
            layout.setOnClickListener { FlexUI.dispatchAction(action) }
        }
        
        return layout
    }
}
```

## Rendering Engine (iOS)

```
FlexNode (parsed JSON)
    │
    ▼
ComponentRegistry.resolve(node.type)
    │
    ├── Built-in type → FlexViewFactory creates UIView programmatically
    │   "container" → UIView with constraints
    │   "row"       → UIStackView(.horizontal)
    │   "column"    → UIStackView(.vertical)
    │   "text"      → UILabel with attributed text
    │   "image"     → UIImageView with URL loading
    │   "button"    → UIButton with action binding
    │   "scroll"    → UIScrollView
    │   "list"      → UITableView with FlexDataSource
    │   "grid"      → UICollectionView with FlowLayout
    │   "card"      → UIView with shadow + corner radius
    │   "input"     → UITextField with validation
    │   "toggle"    → UISwitch
    │   "divider"   → UIView with height constraint = 1
    │   "spacer"    → UIView with intrinsic content size
    │
    └── Custom type → registered closure returns UIView
```

---

## Config Fetching & Caching

```
App Launch / Screen Request
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
  Android:
  - Memory (LRU, 50 screens max)
  - Disk (SharedPreferences or internal file storage — no SQLite needed)
  
  iOS:
  - Memory (NSCache, 50 screens max)
  - Disk (FileManager, Caches directory)
  
Cache TTL: Configurable (default 1 hour)
Force refresh: FlexUI.refreshConfig(screenId)
```

---

## File Structure

### Android Library
```
flexui-android/
├── flexui/                              # Library module
│   ├── src/main/kotlin/dev/flexui/
│   │   ├── FlexUI.kt                   # Main entry point (@JvmStatic API)
│   │   ├── FlexConfig.kt               # Configuration (Builder pattern)
│   │   ├── FlexComponentFactory.kt      # Interface for custom components
│   │   ├── FlexActionHandler.kt         # Interface for action callbacks
│   │   ├── FlexRenderCallback.kt        # Async render callback
│   │   ├── config/
│   │   │   ├── ConfigFetcher.kt         # HTTP fetching (OkHttp or HttpURLConnection)
│   │   │   ├── ConfigCache.kt           # Memory + disk cache
│   │   │   └── ConfigValidator.kt       # Schema validation
│   │   ├── schema/
│   │   │   ├── FlexNode.kt              # Component tree model
│   │   │   ├── FlexTheme.kt             # Theme model
│   │   │   ├── FlexAction.kt            # Action model
│   │   │   ├── FlexProps.kt             # Properties accessor
│   │   │   ├── FlexCondition.kt         # Conditional logic
│   │   │   └── FlexParser.kt            # JSON → model (org.json, no dependencies)
│   │   ├── render/
│   │   │   ├── FlexRenderer.kt          # Core renderer (recursively builds View tree)
│   │   │   └── components/              # Built-in component renderers
│   │   │       ├── ContainerRenderer.kt
│   │   │       ├── RowRenderer.kt
│   │   │       ├── ColumnRenderer.kt
│   │   │       ├── TextRenderer.kt
│   │   │       ├── ImageRenderer.kt
│   │   │       ├── ButtonRenderer.kt
│   │   │       ├── ScrollRenderer.kt
│   │   │       ├── ListRenderer.kt
│   │   │       ├── GridRenderer.kt
│   │   │       ├── CardRenderer.kt
│   │   │       ├── InputRenderer.kt
│   │   │       ├── ToggleRenderer.kt
│   │   │       ├── DividerRenderer.kt
│   │   │       └── SpacerRenderer.kt
│   │   ├── theme/
│   │   │   ├── ThemeResolver.kt         # Variable resolution ({{...}})
│   │   │   └── ThemeDefaults.kt         # Fallback theme values
│   │   ├── actions/
│   │   │   ├── ActionDispatcher.kt      # Route actions to handlers
│   │   │   └── ActionRegistry.kt        # Registered handlers
│   │   └── registry/
│   │       └── ComponentRegistry.kt     # Built-in + custom component lookup
│   ├── src/test/                        # Unit tests
│   └── build.gradle.kts
├── sample/                              # Example app
│   ├── src/main/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### iOS Library
```
flexui-ios/
├── Sources/FlexUI/
│   ├── FlexUI.swift                     # Main entry point
│   ├── FlexConfig.swift                 # Configuration
│   ├── Config/
│   │   ├── ConfigFetcher.swift          # HTTP + caching (URLSession)
│   │   ├── ConfigCache.swift            # NSCache + FileManager
│   │   └── ConfigValidator.swift        # Schema validation
│   ├── Schema/
│   │   ├── FlexNode.swift               # Component tree model (Codable)
│   │   ├── FlexTheme.swift              # Theme model
│   │   ├── FlexAction.swift             # Action model
│   │   ├── FlexProps.swift              # Properties accessor
│   │   ├── FlexCondition.swift          # Conditional logic
│   │   └── FlexParser.swift             # JSON → model parsing
│   ├── Render/
│   │   ├── FlexRenderer.swift           # Core renderer (recursively builds UIView tree)
│   │   └── Components/                  # Built-in component renderers
│   │       ├── ContainerRenderer.swift
│   │       ├── RowRenderer.swift
│   │       ├── ColumnRenderer.swift
│   │       ├── TextRenderer.swift
│   │       ├── ImageRenderer.swift
│   │       ├── ButtonRenderer.swift
│   │       ├── ScrollRenderer.swift
│   │       ├── ListRenderer.swift
│   │       ├── GridRenderer.swift
│   │       ├── CardRenderer.swift
│   │       ├── InputRenderer.swift
│   │       ├── ToggleRenderer.swift
│   │       ├── DividerRenderer.swift
│   │       └── SpacerRenderer.swift
│   ├── Theme/
│   │   ├── ThemeResolver.swift          # Variable resolution
│   │   └── ThemeDefaults.swift          # Fallback theme values
│   ├── Actions/
│   │   ├── ActionDispatcher.swift       # Route actions to handlers
│   │   └── ActionRegistry.swift         # Registered handlers
│   └── Registry/
│       └── ComponentRegistry.swift      # Built-in + custom component lookup
├── Tests/FlexUITests/                   # Unit tests
├── Sample/                              # Example UIKit app
│   ├── FlexUISample/
│   └── FlexUISample.xcodeproj
├── Package.swift                        # SPM package definition
└── FlexUI.podspec                       # CocoaPods
```

---

## Dependencies

### Android
- **org.json** — JSON parsing (already in Android SDK, zero additional dependency)
- **java.net.HttpURLConnection** — HTTP client (built-in, zero dependency)
- **Optional**: OkHttp for HTTP (likely already in host SDK like Flyy)
- **Optional**: Glide/Picasso for image loading (likely already in host SDK)
- **If no image loader available**: Falls back to HttpURLConnection + BitmapFactory
- **Total additional dependencies: ZERO** (everything optional piggybacks on host SDK)

### iOS
- **Foundation** — JSON parsing (Codable, built-in)
- **URLSession** — HTTP client (built-in)
- **UIKit** — Rendering (built-in)
- **Total additional dependencies: ZERO**

---

## What's NOT In Scope (v1)

- Visual editor / dashboard (future product)
- Config hosting server (Flyy uses their own backend)
- Animations beyond basic transitions
- Complex gestures (drag, pinch)
- Video/media player components
- Form validation rules engine
- Offline-first with conflict resolution
- Jetpack Compose renderer (can add as optional module later)
- SwiftUI renderer (can add as optional module later)

---

## Testing Strategy

### Unit Tests
- JSON parsing (valid configs, malformed configs, edge cases)
- Theme resolution (variable substitution, defaults, overrides)
- Condition evaluation (comparisons, boolean logic)
- Action dispatch (routing, data passing)
- Cache behavior (TTL, eviction, memory/disk)

### Integration Tests
- Full render pipeline: JSON → component tree → native View/UIView hierarchy
- Config fetch → cache → render flow
- Custom component registration and rendering
- Java interop tests (Android) — all public API callable from Java

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
├── INTEGRATION_ANDROID.md       # Step-by-step Android setup (Java + Kotlin examples)
├── INTEGRATION_IOS.md           # Step-by-step iOS setup (UIKit)
├── SCHEMA_REFERENCE.md          # Complete JSON schema docs
├── COMPONENTS.md                # Every built-in component with examples
├── THEMING.md                   # Theme system guide
├── CUSTOM_COMPONENTS.md         # How to register your own components
├── ACTIONS.md                   # Action system and callbacks
├── CACHING.md                   # Config caching behavior
├── MIGRATION.md                 # Migrating from hardcoded UI
└── EXAMPLES.md                  # Real-world config examples (Flyy use cases)
```
