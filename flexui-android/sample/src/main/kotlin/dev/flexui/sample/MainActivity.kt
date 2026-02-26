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
        // Real interactive scratch card — touch to scratch, auto-reveals at 40%
        FlexUI.registerComponent("scratch_card") { context, props, theme ->
            val card = ScratchCardView(context)

            // Configure from JSON props
            props.getString("rewardText")?.let { card.rewardText = it }
            props.getString("rewardLabel")?.let { card.rewardLabel = it }
            props.getString("topLabel")?.let { card.topLabel = it }
            props.getString("coverText")?.let { card.coverText = it }

            // Cover colors
            val coverColor1 = props.getString("coverColor1") ?: "#C0C0C0"
            val coverColor2 = props.getString("coverColor2") ?: "#A8A8A8"
            val coverColor3 = props.getString("coverColor3") ?: "#909090"
            card.coverColors = intArrayOf(
                Color.parseColor(coverColor1),
                Color.parseColor(coverColor2),
                Color.parseColor(coverColor3)
            )

            props.getString("rewardTextColor")?.let {
                card.rewardTextColor = Color.parseColor(it)
            }
            props.getString("rewardBgColor")?.let {
                card.rewardBgColor = Color.parseColor(it)
            }

            // When fully revealed, dispatch the reveal callback
            card.onRevealed = {
                val rewardId = props.getString("rewardId") ?: "unknown"
                val amount = props.getString("amount") ?: "0"
                Toast.makeText(
                    context,
                    "🎉 You won ₹$amount!",
                    Toast.LENGTH_LONG
                ).show()
            }

            card
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
            val fillColor = props.getString("color") ?: "#FF6B00"
            fillBg.setColor(Color.parseColor(fillColor))
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
                    Toast.makeText(this, "🎉 ₹$amount cashback claimed! Check your wallet.", Toast.LENGTH_LONG).show()
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
          "success": "#2E7D32"
        },
        "typography": {
          "headingSize": 26,
          "subheadingSize": 18,
          "bodySize": 15,
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
            "props": { "size": 16 }
          },

          {
            "type": "text",
            "props": {
              "content": "🎁 You have a reward!",
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
              "content": "Scratch the card below to reveal your prize",
              "fontSize": "{{typography.bodySize}}",
              "color": "{{colors.textSecondary}}",
              "textAlign": "center"
            },
            "style": {
              "marginBottom": "{{spacing.xl}}"
            }
          },

          {
            "type": "scratch_card",
            "props": {
              "rewardText": "₹500",
              "rewardLabel": "CASHBACK",
              "topLabel": "YOU WON",
              "coverText": "SCRATCH HERE",
              "coverColor1": "#C0C0C0",
              "coverColor2": "#A0A0A0",
              "coverColor3": "#808080",
              "rewardBgColor": "#FFF8E1",
              "rewardTextColor": "#1A1A1A",
              "rewardId": "scratch_500",
              "amount": "500"
            },
            "style": {
              "height": 220,
              "marginBottom": "{{spacing.xl}}"
            }
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
                  "content": "How it works",
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
                "type": "text",
                "props": {
                  "content": "👆  Scratch the silver area with your finger",
                  "fontSize": "{{typography.bodySize}}",
                  "color": "{{colors.text}}"
                },
                "style": {
                  "marginBottom": "{{spacing.sm}}"
                }
              },
              {
                "type": "text",
                "props": {
                  "content": "🎯  Reveal 40% to uncover your reward",
                  "fontSize": "{{typography.bodySize}}",
                  "color": "{{colors.text}}"
                },
                "style": {
                  "marginBottom": "{{spacing.sm}}"
                }
              },
              {
                "type": "text",
                "props": {
                  "content": "💰  Claim your cashback instantly",
                  "fontSize": "{{typography.bodySize}}",
                  "color": "{{colors.text}}"
                }
              }
            ]
          },

          {
            "type": "button",
            "props": {
              "text": "Claim Reward",
              "type": "filled",
              "fontSize": 17
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
              "marginBottom": "{{spacing.lg}}"
            }
          }

        ]
      }
    }
    """.trimIndent()
}
