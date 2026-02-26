package dev.flexui.sample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.flexui.FlexActionHandler
import dev.flexui.FlexComponentFactory
import dev.flexui.FlexError
import dev.flexui.FlexRenderCallback
import dev.flexui.FlexUI
import dev.flexui.schema.FlexAction
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

class MainActivity : AppCompatActivity() {
    
    private lateinit var container: FrameLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        container = findViewById(R.id.container)
        
        // Initialize FlexUI
        initFlexUI()
        
        // Register custom components and actions
        registerCustomComponents()
        registerActionHandlers()
        
        // Render demo content
        renderDemoContent()
    }
    
    private fun initFlexUI() {
        // For demo purposes, we'll render from JSON directly instead of fetching from server
        FlexUI.init(this, "https://demo.flexui.dev") // Dummy URL since we're using JSON directly
    }
    
    private fun registerCustomComponents() {
        // Register a custom component for demonstration
        FlexUI.registerComponent("custom_card") { context, props, theme ->
            val cardView = androidx.cardview.widget.CardView(context)
            cardView.radius = 16f
            cardView.cardElevation = 8f
            cardView.setCardBackgroundColor(theme.getColorInt("surface") ?: 0xFFFFFFFF.toInt())
            cardView
        }
    }
    
    private fun registerActionHandlers() {
        // Register navigation handler
        FlexUI.onAction("navigate") { action ->
            val screen = action.getScreen()
            Toast.makeText(this, "Navigate to: $screen", Toast.LENGTH_SHORT).show()
        }
        
        // Register callback handler
        FlexUI.onEvent("button_clicked") { action ->
            val data = action.getCallbackData()
            val message = data?.get("message") as? String ?: "Button clicked!"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        
        // Register dismiss handler
        FlexUI.onAction("dismiss") {
            finish()
        }
    }
    
    private fun renderDemoContent() {
        val demoJson = """
        {
          "version": "1.0",
          "screenId": "demo_screen",
          "theme": {
            "colors": {
              "primary": "#FF6B00",
              "secondary": "#1A1A2E",
              "background": "#FFFFFF",
              "text": "#333333",
              "textSecondary": "#999999",
              "surface": "#F8F8F8"
            },
            "typography": {
              "headingSize": 24,
              "bodySize": 16,
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
              "lg": 16
            }
          },
          "root": {
            "type": "column",
            "style": {
              "padding": "{{spacing.md}}",
              "backgroundColor": "{{colors.background}}"
            },
            "children": [
              {
                "type": "text",
                "props": {
                  "content": "FlexUI Demo",
                  "fontSize": "{{typography.headingSize}}",
                  "color": "{{colors.text}}",
                  "textAlign": "center"
                },
                "style": {
                  "marginBottom": "{{spacing.lg}}"
                }
              },
              {
                "type": "card",
                "style": {
                  "padding": "{{spacing.md}}",
                  "marginBottom": "{{spacing.md}}"
                },
                "children": [
                  {
                    "type": "text",
                    "props": {
                      "content": "Welcome to FlexUI!",
                      "fontSize": 18,
                      "color": "{{colors.text}}"
                    },
                    "style": {
                      "marginBottom": "{{spacing.sm}}"
                    }
                  },
                  {
                    "type": "text",
                    "props": {
                      "content": "This is a server-driven UI demo showing various components rendered from JSON configuration.",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.textSecondary}}"
                    }
                  }
                ]
              },
              {
                "type": "row",
                "style": {
                  "marginBottom": "{{spacing.md}}"
                },
                "children": [
                  {
                    "type": "button",
                    "props": {
                      "text": "Primary Button",
                      "type": "filled"
                    },
                    "action": {
                      "type": "callback",
                      "event": "button_clicked",
                      "data": {
                        "message": "Primary button was clicked!"
                      }
                    },
                    "style": {
                      "marginRight": "{{spacing.sm}}"
                    }
                  },
                  {
                    "type": "button",
                    "props": {
                      "text": "Secondary",
                      "type": "outlined"
                    },
                    "action": {
                      "type": "callback",
                      "event": "button_clicked",
                      "data": {
                        "message": "Secondary button clicked!"
                      }
                    }
                  }
                ]
              },
              {
                "type": "divider",
                "props": {
                  "color": "{{colors.textSecondary}}"
                },
                "style": {
                  "margin": "{{spacing.md}}"
                }
              },
              {
                "type": "image",
                "props": {
                  "src": "@drawable/ic_launcher_foreground",
                  "scaleType": "centerInside"
                },
                "style": {
                  "width": 120,
                  "height": 120,
                  "marginBottom": "{{spacing.md}}"
                }
              },
              {
                "type": "input",
                "props": {
                  "placeholder": "Enter your name",
                  "inputType": "text"
                },
                "style": {
                  "marginBottom": "{{spacing.md}}"
                }
              },
              {
                "type": "toggle",
                "props": {
                  "text": "Enable notifications",
                  "checked": true
                },
                "style": {
                  "marginBottom": "{{spacing.lg}}"
                }
              },
              {
                "type": "button",
                "props": {
                  "text": "Navigate to Details",
                  "type": "filled"
                },
                "action": {
                  "type": "navigate",
                  "screen": "details"
                }
              }
            ]
          }
        }
        """.trimIndent()
        
        try {
            val view = FlexUI.renderFromJson(this, demoJson)
            if (view != null) {
                container.addView(view)
            } else {
                showError("Failed to render demo content")
            }
        } catch (e: Exception) {
            showError("Error rendering demo: ${e.message}")
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}