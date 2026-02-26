package dev.flexui.sample

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.flexui.FlexUI
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: FrameLayout
    private var currentScreen = "list" // "list" or "scratch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootContainer = findViewById(R.id.container)

        FlexUI.init(this, "https://demo.flexui.dev")
        registerComponents()
        registerActions()
        showCardList()
    }

    override fun onBackPressed() {
        if (currentScreen == "scratch") {
            showCardList()
        } else {
            super.onBackPressed()
        }
    }

    private fun registerComponents() {
        // Real interactive scratch card
        FlexUI.registerComponent("scratch_card") { context, props, theme ->
            val card = ScratchCardView(context)

            props.getString("rewardText")?.let { card.rewardText = it }
            props.getString("rewardLabel")?.let { card.rewardLabel = it }
            props.getString("topLabel")?.let { card.topLabel = it }
            props.getString("coverText")?.let { card.coverText = it }

            val c1 = props.getString("coverColor1") ?: "#C0C0C0"
            val c2 = props.getString("coverColor2") ?: "#A8A8A8"
            val c3 = props.getString("coverColor3") ?: "#909090"
            card.coverColors = intArrayOf(
                Color.parseColor(c1),
                Color.parseColor(c2),
                Color.parseColor(c3)
            )

            props.getString("rewardTextColor")?.let {
                card.rewardTextColor = Color.parseColor(it)
            }
            props.getString("rewardBgColor")?.let {
                card.rewardBgColor = Color.parseColor(it)
            }

            card.onRevealed = {
                val amount = props.getString("amount") ?: "0"
                val label = props.getString("rewardLabel") ?: "reward"
                Toast.makeText(context, "🎉 You won $amount $label!", Toast.LENGTH_LONG).show()
            }

            card
        }

        // Reward card thumbnail (for the list view) — shows a mini card with icon and status
        FlexUI.registerComponent("reward_card_thumb") { context, props, theme ->
            val density = context.resources.displayMetrics.density
            val card = LinearLayout(context)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(
                (16 * density).toInt(),
                (16 * density).toInt(),
                (16 * density).toInt(),
                (16 * density).toInt()
            )
            card.elevation = 4 * density

            val bg = GradientDrawable()
            bg.cornerRadius = 16 * density
            bg.setColor(Color.WHITE)
            card.background = bg

            // Icon area
            val iconBg = FrameLayout(context)
            val iconBgDrawable = GradientDrawable()
            val bgColor = props.getString("iconBgColor") ?: "#FFF3E0"
            iconBgDrawable.setColor(Color.parseColor(bgColor))
            iconBgDrawable.cornerRadius = 12 * density
            iconBg.background = iconBgDrawable
            val iconParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (100 * density).toInt()
            )
            iconParams.bottomMargin = (12 * density).toInt()
            iconBg.layoutParams = iconParams

            val iconText = android.widget.TextView(context)
            val emoji = props.getString("icon") ?: "🎁"
            iconText.text = emoji
            iconText.textSize = 40f
            iconText.gravity = android.view.Gravity.CENTER
            iconBg.addView(iconText, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            card.addView(iconBg)

            // Title
            val title = android.widget.TextView(context)
            title.text = props.getString("title") ?: "Scratch & Win"
            title.textSize = 15f
            title.setTextColor(Color.parseColor("#1A1A1A"))
            title.typeface = android.graphics.Typeface.DEFAULT_BOLD
            val titleParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            titleParams.bottomMargin = (4 * density).toInt()
            title.layoutParams = titleParams
            card.addView(title)

            // Subtitle
            val subtitle = android.widget.TextView(context)
            subtitle.text = props.getString("subtitle") ?: ""
            subtitle.textSize = 12f
            subtitle.setTextColor(Color.parseColor("#757575"))
            val subParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            subParams.bottomMargin = (8 * density).toInt()
            subtitle.layoutParams = subParams
            card.addView(subtitle)

            // Status badge
            val status = props.getString("status") ?: "Scratch now"
            val statusView = android.widget.TextView(context)
            statusView.text = status
            statusView.textSize = 11f
            statusView.gravity = android.view.Gravity.CENTER
            statusView.setPadding(
                (12 * density).toInt(),
                (6 * density).toInt(),
                (12 * density).toInt(),
                (6 * density).toInt()
            )
            val statusBg = GradientDrawable()
            statusBg.cornerRadius = 20 * density
            if (status.contains("Scratch", ignoreCase = true)) {
                statusBg.setColor(Color.parseColor("#FF6B00"))
                statusView.setTextColor(Color.WHITE)
            } else {
                statusBg.setColor(Color.parseColor("#E8F5E9"))
                statusView.setTextColor(Color.parseColor("#2E7D32"))
            }
            statusView.background = statusBg
            card.addView(statusView)

            card
        }
    }

    private fun registerActions() {
        FlexUI.onAction("callback") { action ->
            val event = action.getEvent() ?: ""
            val data = action.getCallbackData()
            when (event) {
                "open_scratch_card" -> {
                    val cardId = data?.get("cardId") as? String ?: "1"
                    val reward = data?.get("reward") as? String ?: "₹100"
                    val label = data?.get("label") as? String ?: "CASHBACK"
                    val coverColor = data?.get("coverColor") as? String ?: "#C0C0C0"
                    showScratchScreen(cardId, reward, label, coverColor)
                }
                "claim_reward" -> {
                    val amount = data?.get("amount") as? String ?: "0"
                    Toast.makeText(this, "🎉 ₹$amount cashback added to your wallet!", Toast.LENGTH_LONG).show()
                }
                "go_back" -> {
                    showCardList()
                }
                else -> {
                    Toast.makeText(this, "Action: $event", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCardList() {
        currentScreen = "list"
        val json = cardListScreen()
        renderJson(json)
    }

    private fun showScratchScreen(cardId: String, reward: String, label: String, coverColor: String) {
        currentScreen = "scratch"
        val json = scratchScreen(cardId, reward, label, coverColor)
        renderJson(json)
    }

    private fun renderJson(json: String) {
        try {
            val view = FlexUI.renderFromJson(this, json)
            if (view != null) {
                rootContainer.removeAllViews()
                rootContainer.addView(view)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun cardListScreen(): String = """
    {
      "version": "1.0",
      "screenId": "scratch_card_list",
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
          "headingSize": 22,
          "subheadingSize": 16,
          "bodySize": 14,
          "captionSize": 11
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
        "type": "scroll",
        "props": { "direction": "vertical" },
        "style": {
          "backgroundColor": "{{colors.background}}"
        },
        "children": [
          {
            "type": "column",
            "style": {
              "padding": "{{spacing.md}}"
            },
            "children": [

              {
                "type": "text",
                "props": {
                  "content": "Rewards",
                  "fontSize": "{{typography.headingSize}}",
                  "color": "{{colors.text}}",
                  "fontWeight": "bold"
                },
                "style": {
                  "marginBottom": "{{spacing.xs}}"
                }
              },

              {
                "type": "text",
                "props": {
                  "content": "You have 5 scratch cards waiting!",
                  "fontSize": "{{typography.bodySize}}",
                  "color": "{{colors.textSecondary}}"
                },
                "style": {
                  "marginBottom": "{{spacing.lg}}"
                }
              },

              {
                "type": "grid",
                "props": {
                  "columns": 2
                },
                "children": [

                  {
                    "type": "reward_card_thumb",
                    "props": {
                      "icon": "💰",
                      "iconBgColor": "#FFF3E0",
                      "title": "Cashback Reward",
                      "subtitle": "Earned from ₹999 order",
                      "status": "Scratch now"
                    },
                    "action": {
                      "type": "callback",
                      "event": "open_scratch_card",
                      "data": {
                        "cardId": "1",
                        "reward": "₹500",
                        "label": "CASHBACK",
                        "coverColor": "#C0C0C0"
                      }
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}",
                      "marginRight": "{{spacing.sm}}"
                    }
                  },

                  {
                    "type": "reward_card_thumb",
                    "props": {
                      "icon": "🎫",
                      "iconBgColor": "#E3F2FD",
                      "title": "Movie Voucher",
                      "subtitle": "Weekend special",
                      "status": "Scratch now"
                    },
                    "action": {
                      "type": "callback",
                      "event": "open_scratch_card",
                      "data": {
                        "cardId": "2",
                        "reward": "₹200",
                        "label": "MOVIE VOUCHER",
                        "coverColor": "#1565C0"
                      }
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}",
                      "marginLeft": "{{spacing.sm}}"
                    }
                  },

                  {
                    "type": "reward_card_thumb",
                    "props": {
                      "icon": "🍕",
                      "iconBgColor": "#FCE4EC",
                      "title": "Food Coupon",
                      "subtitle": "Order above ₹499",
                      "status": "Scratch now"
                    },
                    "action": {
                      "type": "callback",
                      "event": "open_scratch_card",
                      "data": {
                        "cardId": "3",
                        "reward": "₹150",
                        "label": "FOOD COUPON",
                        "coverColor": "#C62828"
                      }
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}",
                      "marginRight": "{{spacing.sm}}"
                    }
                  },

                  {
                    "type": "reward_card_thumb",
                    "props": {
                      "icon": "⚡",
                      "iconBgColor": "#F3E5F5",
                      "title": "Recharge Bonus",
                      "subtitle": "Mobile recharge",
                      "status": "Scratch now"
                    },
                    "action": {
                      "type": "callback",
                      "event": "open_scratch_card",
                      "data": {
                        "cardId": "4",
                        "reward": "₹50",
                        "label": "RECHARGE",
                        "coverColor": "#6A1B9A"
                      }
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}",
                      "marginLeft": "{{spacing.sm}}"
                    }
                  },

                  {
                    "type": "reward_card_thumb",
                    "props": {
                      "icon": "🏆",
                      "iconBgColor": "#FFF8E1",
                      "title": "Gold Prize",
                      "subtitle": "Loyalty reward",
                      "status": "✓ Claimed"
                    },
                    "action": {
                      "type": "callback",
                      "event": "open_scratch_card",
                      "data": {
                        "cardId": "5",
                        "reward": "₹1000",
                        "label": "GOLD PRIZE",
                        "coverColor": "#FF8F00"
                      }
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}",
                      "marginRight": "{{spacing.sm}}"
                    }
                  }

                ]
              }
            ]
          }
        ]
      }
    }
    """.trimIndent()

    private fun scratchScreen(cardId: String, reward: String, label: String, coverColor: String): String {
        // Derive gradient colors from the base cover color
        val c1 = coverColor
        val c2 = coverColor
        val c3 = coverColor

        return """
        {
          "version": "1.0",
          "screenId": "scratch_card_$cardId",
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
              "headingSize": 22,
              "subheadingSize": 16,
              "bodySize": 14,
              "captionSize": 11
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
            "type": "scroll",
            "props": { "direction": "vertical" },
            "style": {
              "backgroundColor": "{{colors.background}}"
            },
            "children": [
              {
                "type": "column",
                "style": {
                  "padding": "{{spacing.lg}}"
                },
                "children": [

                  {
                    "type": "button",
                    "props": {
                      "text": "← Back to Rewards",
                      "type": "text",
                      "fontSize": 14
                    },
                    "action": {
                      "type": "callback",
                      "event": "go_back",
                      "data": {}
                    },
                    "style": {
                      "marginBottom": "{{spacing.lg}}"
                    }
                  },

                  {
                    "type": "text",
                    "props": {
                      "content": "🎁 Scratch to reveal your reward",
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
                      "content": "Use your finger to scratch the card",
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
                      "rewardText": "$reward",
                      "rewardLabel": "$label",
                      "topLabel": "YOU WON",
                      "coverText": "SCRATCH HERE",
                      "coverColor1": "$c1",
                      "coverColor2": "$c2",
                      "coverColor3": "$c3",
                      "rewardBgColor": "#FFFDE7",
                      "rewardTextColor": "#1A1A1A",
                      "rewardId": "card_$cardId",
                      "amount": "${reward.replace("₹", "")}"
                    },
                    "style": {
                      "height": 550,
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
                          "content": "👆  Scratch the card with your finger",
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
                          "content": "💰  Tap claim to add to your wallet",
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
                        "cardId": "$cardId",
                        "amount": "${reward.replace("₹", "")}",
                        "type": "$label"
                      }
                    },
                    "style": {
                      "marginBottom": "{{spacing.xl}}"
                    }
                  }

                ]
              }
            ]
          }
        }
        """.trimIndent()
    }
}
