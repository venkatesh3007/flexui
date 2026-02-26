package dev.flexui.sample

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.flexui.FlexUI
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

class MainActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)

        FlexUI.init(this, "https://demo.flexui.dev")
        registerComponents()
        registerActions()
        renderScreen()
    }

    private fun registerComponents() {
        // Scratch card surface — gold gradient card
        FlexUI.registerComponent("scratch_card_surface") { context, props, theme ->
            val frame = FrameLayout(context)
            val gd = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#FFD700"),
                    Color.parseColor("#FF8C00"),
                    Color.parseColor("#FF4500")
                )
            )
            gd.cornerRadius = 24f
            frame.background = gd
            frame.elevation = 12f
            frame
        }

        // Progress bar — colored fill inside grey track
        FlexUI.registerComponent("progress_bar") { context, props, theme ->
            val track = FrameLayout(context)
            val trackBg = GradientDrawable()
            trackBg.setColor(Color.parseColor("#E8E8E8"))
            trackBg.cornerRadius = 12f
            track.background = trackBg

            val fill = View(context)
            val fillBg = GradientDrawable()
            fillBg.setColor(Color.parseColor("#FF6B00"))
            fillBg.cornerRadius = 12f
            fill.background = fillBg

            val progress = props.getFloat("progress") ?: 0.5f
            fill.post {
                val params = fill.layoutParams as FrameLayout.LayoutParams
                params.width = (track.width * progress).toInt()
                fill.layoutParams = params
            }

            track.addView(fill, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            track
        }
    }

    private fun registerActions() {
        FlexUI.onAction("callback") { action ->
            val event = action.getEvent() ?: ""
            val data = action.getCallbackData()
            when (event) {
                "claim_reward" -> {
                    val amount = data?.get("amount") as? String ?: "0"
                    Toast.makeText(this, "🎉 Claimed ₹$amount cashback!", Toast.LENGTH_LONG).show()
                }
                "view_terms" -> {
                    Toast.makeText(this, "Opening Terms & Conditions...", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Action: $event", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderScreen() {
        val json = scratchCardScreen()
        try {
            val view = FlexUI.renderFromJson(this, json)
            if (view != null) {
                container.removeAllViews()
                container.addView(view)
            } else {
                Toast.makeText(this, "Render returned null", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun scratchCardScreen(): String = """
    {
      "version": "1.0",
      "screenId": "scratch_card_reward",
      "theme": {
        "colors": {
          "primary": "#FF6B00",
          "background": "#F5F5F5",
          "surface": "#FFFFFF",
          "text": "#1A1A1A",
          "textSecondary": "#757575",
          "success": "#2E7D32",
          "gold": "#FFB300"
        },
        "typography": {
          "headingSize": 28,
          "subheadingSize": 20,
          "bodySize": 15,
          "captionSize": 12
        },
        "spacing": {
          "xs": 4,
          "sm": 8,
          "md": 16,
          "lg": 24,
          "xl": 32,
          "xxl": 48
        },
        "borderRadius": {
          "sm": 8,
          "md": 12,
          "lg": 20
        }
      },
      "root": {
        "type": "column",
        "style": {
          "backgroundColor": "{{colors.background}}",
          "padding": "{{spacing.lg}}"
        },
        "children": [

          {
            "type": "spacer",
            "props": { "size": 24 }
          },

          {
            "type": "text",
            "props": {
              "content": "🎉",
              "fontSize": 56,
              "textAlign": "center"
            },
            "style": {
              "marginBottom": "{{spacing.sm}}"
            }
          },

          {
            "type": "text",
            "props": {
              "content": "Congratulations!",
              "fontSize": "{{typography.headingSize}}",
              "color": "{{colors.text}}",
              "textAlign": "center",
              "fontWeight": "bold"
            },
            "style": {
              "marginBottom": "{{spacing.xs}}"
            }
          },

          {
            "type": "text",
            "props": {
              "content": "You scratched a winning card",
              "fontSize": "{{typography.bodySize}}",
              "color": "{{colors.textSecondary}}",
              "textAlign": "center"
            },
            "style": {
              "marginBottom": "{{spacing.xl}}"
            }
          },

          {
            "type": "scratch_card_surface",
            "style": {
              "height": 200,
              "marginBottom": "{{spacing.xl}}",
              "padding": "{{spacing.lg}}"
            },
            "children": [
              {
                "type": "column",
                "style": {
                  "padding": "{{spacing.lg}}"
                },
                "children": [
                  {
                    "type": "text",
                    "props": {
                      "content": "YOU WON",
                      "fontSize": 14,
                      "color": "#FFFFFF",
                      "textAlign": "center",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.sm}}"
                    }
                  },
                  {
                    "type": "text",
                    "props": {
                      "content": "₹500",
                      "fontSize": 48,
                      "color": "#FFFFFF",
                      "textAlign": "center",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.xs}}"
                    }
                  },
                  {
                    "type": "text",
                    "props": {
                      "content": "CASHBACK",
                      "fontSize": 18,
                      "color": "#FFFFFF",
                      "textAlign": "center",
                      "fontWeight": "bold"
                    }
                  }
                ]
              }
            ]
          },

          {
            "type": "card",
            "props": {
              "elevation": 2,
              "cornerRadius": 12
            },
            "style": {
              "padding": "{{spacing.lg}}",
              "marginBottom": "{{spacing.lg}}"
            },
            "children": [
              {
                "type": "text",
                "props": {
                  "content": "Reward Details",
                  "fontSize": "{{typography.subheadingSize}}",
                  "color": "{{colors.text}}",
                  "fontWeight": "bold"
                },
                "style": {
                  "marginBottom": "{{spacing.md}}"
                }
              },
              {
                "type": "divider",
                "style": {
                  "marginBottom": "{{spacing.md}}"
                }
              },
              {
                "type": "row",
                "style": {
                  "marginBottom": "{{spacing.sm}}"
                },
                "children": [
                  {
                    "type": "text",
                    "props": {
                      "content": "✅  Valid for 7 days",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.text}}"
                    }
                  }
                ]
              },
              {
                "type": "row",
                "style": {
                  "marginBottom": "{{spacing.sm}}"
                },
                "children": [
                  {
                    "type": "text",
                    "props": {
                      "content": "🛒  Min. order ₹999",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.text}}"
                    }
                  }
                ]
              },
              {
                "type": "row",
                "children": [
                  {
                    "type": "text",
                    "props": {
                      "content": "🚚  Free delivery included",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.text}}"
                    }
                  }
                ]
              }
            ]
          },

          {
            "type": "button",
            "props": {
              "text": "Claim Reward",
              "type": "filled",
              "fontSize": 18
            },
            "action": {
              "type": "callback",
              "event": "claim_reward",
              "data": {
                "rewardId": "scratch_500",
                "amount": "500",
                "type": "cashback"
              }
            },
            "style": {
              "marginBottom": "{{spacing.md}}"
            }
          },

          {
            "type": "button",
            "props": {
              "text": "Terms & Conditions",
              "type": "text",
              "fontSize": 13
            },
            "action": {
              "type": "callback",
              "event": "view_terms",
              "data": {}
            },
            "style": {
              "marginBottom": "{{spacing.xl}}"
            }
          }

        ]
      }
    }
    """.trimIndent()
}
