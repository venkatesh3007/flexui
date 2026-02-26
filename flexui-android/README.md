# FlexUI Android SDK

A server-driven native UI library that renders JSON configurations into native Android Views.

## Features

- **Pure View System**: No Jetpack Compose dependency, uses traditional Android Views
- **Java Compatible**: Full Java interoperability with `@JvmStatic` APIs
- **Zero Dependencies**: Only uses Android SDK built-ins (org.json, HttpURLConnection, BitmapFactory)
- **Embeddable**: Designed to work inside other SDKs (like Flyy's gamification SDK)
- **Theme Support**: Rich theming with variable substitution (`{{colors.primary}}`)
- **Custom Components**: Register your own component factories
- **Action Handling**: Built-in support for navigation, callbacks, URL opening, and custom actions
- **Caching**: Memory + disk caching with configurable TTL
- **Offline Support**: Cache-first operation with network fallback

## Quick Start

### 1. Add Dependency

Add to your `build.gradle.kts`:

```kotlin
implementation(project(":flexui"))
```

### 2. Initialize FlexUI

```kotlin
// Simple initialization
FlexUI.init(context, "https://your-api.com/flexui")

// Or with configuration
val config = FlexConfig.Builder("https://your-api.com/flexui")
    .cachePolicy(CachePolicy.CACHE_FIRST)
    .cacheTtlMinutes(60)
    .connectTimeoutSeconds(10)
    .build()
FlexUI.init(context, config)
```

### 3. Render UI

```kotlin
// Synchronous rendering
val view = FlexUI.render(context, "welcome_screen")
container.addView(view)

// Asynchronous rendering
FlexUI.renderAsync(context, "welcome_screen", mapOf(), object : FlexRenderCallback {
    override fun onSuccess(view: View) {
        container.addView(view)
    }
    
    override fun onError(error: FlexError) {
        // Handle error
    }
})

// Render from JSON directly
val jsonConfig = """{"version": "1.0", "screenId": "test", ...}"""
val view = FlexUI.renderFromJson(context, jsonConfig)
```

## JSON Configuration Format

### Basic Structure

```json
{
  "version": "1.0",
  "screenId": "welcome_screen",
  "theme": {
    "colors": {
      "primary": "#FF6B00",
      "background": "#FFFFFF",
      "text": "#333333"
    },
    "spacing": {
      "sm": 8,
      "md": 16,
      "lg": 24
    },
    "borderRadius": {
      "md": 8,
      "lg": 16
    }
  },
  "root": {
    "type": "column",
    "children": [...]
  }
}
```

### Built-in Components

#### Layout Components
- `container` - FrameLayout wrapper
- `row` - Horizontal LinearLayout
- `column` - Vertical LinearLayout  
- `scroll` - ScrollView (vertical/horizontal)

#### Content Components
- `text` - TextView with rich styling
- `image` - ImageView with URL loading
- `divider` - Simple line separator
- `spacer` - Flexible or fixed spacing

#### Interactive Components
- `button` - Button with multiple styles
- `input` - EditText with validation support
- `toggle` - Switch component

#### Data Components
- `list` - LinearLayout for lists (use RecyclerView for performance)
- `grid` - GridLayout for grid layouts
- `card` - Elevated container

### Example Component

```json
{
  "type": "button",
  "props": {
    "text": "Click Me",
    "type": "filled"
  },
  "style": {
    "padding": "{{spacing.md}}",
    "backgroundColor": "{{colors.primary}}",
    "borderRadius": "{{borderRadius.md}}"
  },
  "action": {
    "type": "callback",
    "event": "button_clicked",
    "data": {
      "message": "Hello World!"
    }
  }
}
```

## Custom Components

Register your own component types:

```kotlin
// Kotlin
FlexUI.registerComponent("scratch_card") { context, props, theme ->
    val view = ScratchCardView(context)
    view.setScratchColor(theme.getColorInt("primary") ?: Color.BLUE)
    view.setRevealText(props.getString("reward_text", "Win!"))
    view
}
```

```java
// Java
FlexUI.registerComponent("scratch_card", new FlexComponentFactory() {
    @Override
    public View create(Context context, FlexProps props, FlexTheme theme) {
        ScratchCardView view = new ScratchCardView(context);
        Integer primaryColor = theme.getColorInt("primary");
        if (primaryColor != null) {
            view.setScratchColor(primaryColor);
        }
        view.setRevealText(props.getString("reward_text", "Win!"));
        return view;
    }
});
```

## Action Handling

Handle user interactions:

```kotlin
// Navigation
FlexUI.onAction("navigate") { action ->
    val screen = action.getScreen()
    startActivity(Intent(this, getActivityForScreen(screen)))
}

// Custom events
FlexUI.onEvent("purchase_item") { action ->
    val itemId = action.getCallbackData()?.get("itemId") as? String
    purchaseItem(itemId)
}

// URL opening (handled automatically, but you can override)
FlexUI.onAction("openUrl") { action ->
    val url = action.getUrl()
    // Custom URL handling logic
}
```

## Theme Variables

Use `{{variable}}` syntax for theme values:

```json
{
  "style": {
    "backgroundColor": "{{colors.primary}}",
    "padding": "{{spacing.md}}",
    "borderRadius": "{{borderRadius.lg}}"
  }
}
```

## Caching

FlexUI includes built-in caching:

```kotlin
// Cache policies
CachePolicy.CACHE_FIRST     // Use cache, fetch in background
CachePolicy.NETWORK_FIRST   // Network first, cache fallback
CachePolicy.CACHE_ONLY      // Offline mode
CachePolicy.NETWORK_ONLY    // No caching

// Manual cache management
FlexUI.refreshConfig("screen_id")  // Force refresh
FlexUI.clearCache()                // Clear all cache
```

## Error Handling

```kotlin
FlexUI.renderAsync(context, "screen_id", mapOf(), object : FlexRenderCallback {
    override fun onError(error: FlexError) {
        when (error.code) {
            FlexError.ErrorCode.NETWORK_ERROR -> // Handle network issues
            FlexError.ErrorCode.PARSE_ERROR -> // Handle JSON parsing issues
            FlexError.ErrorCode.UNKNOWN_COMPONENT -> // Handle missing components
            else -> // Handle other errors
        }
    }
})
```

## Proguard Rules

Add to your `proguard-rules.pro`:

```
-keep class dev.flexui.** { *; }
-keepclassmembers class dev.flexui.** { *; }
```

## Architecture

```
┌─────────────────┐
│   Your App      │
├─────────────────┤
│   FlexUI SDK    │
├─────────────────┤
│ Config  │ Theme │
│ Fetch   │ Layer │
├─────────┼───────┤
│ Render  │ Action│
│ Layer   │ Layer │
├─────────┼───────┤
│   Component     │
│   Registry      │
├─────────────────┤
│ Android Views   │
└─────────────────┘
```

## Sample App

Run the sample app to see FlexUI in action:

```bash
./gradlew :sample:installDebug
```

The sample demonstrates:
- All built-in components
- Custom component registration
- Action handling
- Theme variables
- JSON configuration

## License

[Add your license here]