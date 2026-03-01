# FlexUI — Server-Driven Native UI SDK

[![Build Status](https://github.com/flexui/flexui/workflows/CI/badge.svg)](https://github.com/flexui/flexui/actions)
[![Android](https://img.shields.io/badge/Android-21%2B-green.svg)](https://android-arsenal.com/api?level=21)
[![iOS](https://img.shields.io/badge/iOS-13.0%2B-blue.svg)](https://developer.apple.com/ios/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Build native mobile UIs from JSON configurations fetched from your server.**

FlexUI is a lightweight SDK that renders fully native UI components from JSON configurations. Perfect for SDK developers who want to give their B2B customers UI customization capabilities without requiring app updates.

## ✨ Key Features

- 🚀 **Server-Driven** — Update UIs without app releases
- 📱 **Native Performance** — Real native views, not WebViews  
- 🎨 **Themeable** — Consistent design system with variables
- ⚡ **Lightweight** — ~200KB footprint, minimal dependencies
- 🔧 **SDK-Friendly** — Designed to embed in other SDKs
- 🎯 **Type-Safe** — Kotlin/Swift APIs with full type safety
- 📐 **Flexible** — Built-in components + custom component support

## 🏗 Architecture

```
Server (JSON config) → FlexUI SDK → Native UI Components
```

FlexUI transforms JSON configurations into native UI hierarchies:

```json
{
  "screenId": "welcome_screen",
  "root": {
    "type": "column",
    "children": [
      {
        "type": "text",
        "props": { 
          "content": "Welcome!",
          "fontSize": "{{typography.heading}}"
        }
      },
      {
        "type": "button",
        "props": { "text": "Get Started" },
        "action": { "type": "navigate", "target": "onboarding" }
      }
    ]
  }
}
```

## 🚀 Quick Start

### Android (Kotlin)

```kotlin
// 1. Initialize FlexUI
FlexUI.init(context, "https://your-api.com/flexui")

// 2. Render from JSON
val jsonConfig = """{"screenId": "demo", "root": {...}}"""
val view = FlexUI.renderFromJson(context, jsonConfig)
parentView.addView(view)

// 3. Handle actions
FlexUI.onAction("callback") { action ->
    when (action.getEvent()) {
        "button_clicked" -> handleButtonClick(action.getCallbackData())
    }
}
```

### Android (Java)

```java
// 1. Initialize FlexUI
FlexUI.init(context, "https://your-api.com/flexui");

// 2. Render from JSON
String jsonConfig = "{\"screenId\": \"demo\", \"root\": {...}}";
View view = FlexUI.renderFromJson(context, jsonConfig);
parentView.addView(view);

// 3. Handle actions
FlexUI.onAction("callback", action -> {
    String event = action.getEvent();
    if ("button_clicked".equals(event)) {
        handleButtonClick(action.getCallbackData());
    }
});
```

### iOS (Swift)

```swift
// 1. Initialize FlexUI
FlexUI.initialize(baseURL: "https://your-api.com/flexui")

// 2. Render from JSON
let jsonConfig = """{"screenId": "demo", "root": {...}}"""
let view = try FlexUI.renderFromJSON(jsonConfig)
parentView.addSubview(view)

// 3. Handle actions
FlexUI.onAction("callback") { action in
    switch action.event {
    case "button_clicked":
        handleButtonClick(action.data)
    }
}
```

### iOS (UIKit/Objective-C)

```objc
// 1. Initialize FlexUI
[FlexUI initializeWithBaseURL:@"https://your-api.com/flexui"];

// 2. Render from JSON
NSString *jsonConfig = @"{\"screenId\": \"demo\", \"root\": {...}}";
UIView *view = [FlexUI renderFromJSON:jsonConfig error:nil];
[self.view addSubview:view];

// 3. Handle actions
[FlexUI onAction:@"callback" handler:^(FlexAction *action) {
    if ([action.event isEqualToString:@"button_clicked"]) {
        [self handleButtonClick:action.data];
    }
}];
```

## 📦 Built-in Components

FlexUI includes these components out of the box:

### Layout Components
- **`container`** — Basic container with padding/margins
- **`row`** — Horizontal layout (like CSS flexbox row)
- **`column`** — Vertical layout (like CSS flexbox column) 
- **`scroll`** — Scrollable container
- **`list`** — Efficient list/recycler view
- **`grid`** — Grid layout with configurable columns

### UI Components
- **`text`** — Styled text with typography theming
- **`button`** — Tappable buttons with multiple styles
- **`card`** — Material Design cards with elevation
- **`divider`** — Horizontal/vertical separators
- **`spacer`** — Flexible spacing
- **`toggle`** — Switch/toggle controls

### Custom Components
Register your own components:

```kotlin
FlexUI.registerComponent("scratch_card") { context, props, theme ->
    ScratchCardView(context).apply {
        rewardText = props.getString("rewardText") ?: ""
        // ... configure your custom view
    }
}
```

## 🎨 Theming System

Define consistent design tokens:

```json
{
  "theme": {
    "colors": {
      "primary": "#FF6B00",
      "background": "#FFFFFF",
      "text": "#333333"
    },
    "typography": {
      "heading": 24,
      "body": 16,
      "caption": 12
    },
    "spacing": {
      "xs": 4, "sm": 8, "md": 16, "lg": 24, "xl": 32
    },
    "borderRadius": {
      "sm": 4, "md": 8, "lg": 16, "full": 999
    }
  }
}
```

Reference theme values with variables:

```json
{
  "type": "text",
  "props": {
    "content": "Hello World",
    "color": "{{colors.primary}}",
    "fontSize": "{{typography.heading}}"
  },
  "style": {
    "padding": "{{spacing.md}}",
    "backgroundColor": "{{colors.background}}"
  }
}
```

## 🎯 Actions & Interactions

Handle user interactions with a flexible action system:

```json
{
  "type": "button",
  "props": { "text": "Click Me" },
  "action": {
    "type": "callback",
    "event": "button_clicked",
    "data": {
      "buttonId": "primary_cta",
      "source": "welcome_screen"
    }
  }
}
```

Built-in action types:
- **`callback`** — Custom app callbacks
- **`navigate`** — Screen navigation  
- **`url`** — Open URLs/deep links
- **`dismiss`** — Dismiss modals

## 🔧 Integration Examples

### Flyy SDK Integration
```kotlin
// In your SDK initialization
FlexUI.init(context, "https://api.flyy.in/v1/flexui")

// Register SDK-specific components
FlexUI.registerComponent("reward_card") { context, props, theme ->
    RewardCardView(context).apply {
        // Configure with SDK data
    }
}

// Render campaign UIs
val campaignView = FlexUI.renderFromConfig(context, campaignConfig)
```

### Dynamic Form Builder
```kotlin
FlexUI.registerComponent("form_field") { context, props, theme ->
    when (props.getString("fieldType")) {
        "text" -> EditText(context)
        "select" -> Spinner(context)
        "checkbox" -> CheckBox(context)
        else -> TextView(context)
    }
}
```

## 📚 Documentation

- **[Architecture Guide](docs/architecture.md)** — Core concepts and design
- **[Component Reference](docs/components/)** — All built-in components
- **[Theming Guide](docs/theming.md)** — Design system and variables
- **[Actions Guide](docs/actions.md)** — Handling user interactions
- **[Custom Components](docs/custom-components.md)** — Building your own
- **[Integration Examples](docs/examples/)** — Real-world use cases

## 🏃 Running the Sample

### Android
```bash
cd flexui-android
./gradlew :sample:installDebug
```

The sample shows an interactive scratch card rewards screen with:
- Grid layout of reward cards
- Custom scratch-to-reveal component
- Theme-based styling
- Action handling for navigation

### iOS  
```bash
cd flexui-ios
open FlexUI.xcworkspace
```

## 🧪 Testing

### Android Unit Tests
```bash
cd flexui-android
./gradlew :flexui:test
```

Tests cover:
- JSON parsing (valid, malformed, edge cases)
- Theme resolution and variable substitution  
- Condition evaluation logic
- FlexProps type-safe accessors

### iOS Unit Tests
```bash
cd flexui-ios  
xcodebuild test -workspace FlexUI.xcworkspace -scheme FlexUI
```

## 📱 Platform Support

### Android
- **Minimum SDK:** 21 (Android 5.0)
- **Target SDK:** 35 (Android 15)
- **Java/Kotlin:** Compatible with both
- **Dependencies:** AndroidX only, no Compose requirement

### iOS  
- **Minimum Version:** iOS 13.0
- **Frameworks:** UIKit (no SwiftUI requirement)
- **Languages:** Swift 5.0+, Objective-C compatible

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
```bash
# Clone the repository
git clone https://github.com/flexui/flexui.git
cd flexui

# Android development
cd flexui-android
./gradlew build

# iOS development  
cd flexui-ios
open FlexUI.xcworkspace
```

## 📄 License

FlexUI is released under the MIT License. See [LICENSE](LICENSE) for details.

## 🏢 Created By

FlexUI is built and maintained by the team at [Flyy](https://flyy.in) — the growth platform that helps apps increase user engagement and retention.

---

**Ready to make your mobile UIs server-driven?** [Get started with FlexUI →](docs/getting-started.md)