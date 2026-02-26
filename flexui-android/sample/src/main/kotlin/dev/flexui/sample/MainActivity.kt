package dev.flexui.sample

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dev.flexui.FlexActionHandler
import dev.flexui.FlexComponentFactory
import dev.flexui.FlexUI
import dev.flexui.schema.FlexAction
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

class MainActivity : AppCompatActivity() {
    
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: DemoScreensAdapter
    
    private val tabTitles = arrayOf(
        "💰 Scratch Card",
        "🏆 Spin Result", 
        "❓ Quiz",
        "🎁 Rewards",
        "📱 Widget Demo"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        initFlexUI()
        registerCustomComponents()
        registerActionHandlers()
        setupTabs()
    }
    
    private fun setupViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        
        adapter = DemoScreensAdapter(this)
        viewPager.adapter = adapter
    }
    
    private fun initFlexUI() {
        FlexUI.init(this, "https://demo.flexui.dev")
    }
    
    private fun registerCustomComponents() {
        // Custom scratch card surface component
        FlexUI.registerComponent("scratch_card_surface") { context, props, theme ->
            val container = FrameLayout(context)
            
            // Create gradient background for scratch card effect
            val gradientDrawable = GradientDrawable()
            gradientDrawable.colors = intArrayOf(
                Color.parseColor("#FFD700"), // Gold
                Color.parseColor("#FFA500"), // Orange
                Color.parseColor("#FF6347")  // Tomato
            )
            gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            gradientDrawable.orientation = GradientDrawable.Orientation.TOP_BOTTOM
            gradientDrawable.cornerRadius = 16f
            
            container.background = gradientDrawable
            container.elevation = 8f
            container
        }
        
        // Custom progress bar component
        FlexUI.registerComponent("progress_bar") { context, props, theme ->
            val container = FrameLayout(context)
            val progress = props.getFloat("progress") ?: 0f
            
            // Background
            val bgDrawable = GradientDrawable()
            bgDrawable.setColor(Color.parseColor("#E0E0E0"))
            bgDrawable.cornerRadius = 8f
            container.background = bgDrawable
            
            // Progress indicator
            val progressView = View(context)
            val progressDrawable = GradientDrawable()
            progressDrawable.setColor(Color.parseColor("#4CAF50"))
            progressDrawable.cornerRadius = 8f
            progressView.background = progressDrawable
            
            val params = FrameLayout.LayoutParams(
                (200 * progress).toInt(), // Width based on progress
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            container.addView(progressView, params)
            
            container
        }
        
        // Custom avatar component
        FlexUI.registerComponent("avatar") { context, props, theme ->
            val container = FrameLayout(context)
            val size = props.getInt("size") ?: 40
            
            // Circular background
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Color.parseColor("#2196F3"))
            container.background = drawable
            
            // Set fixed size
            val params = FrameLayout.LayoutParams(size, size)
            container.layoutParams = params
            
            container
        }
    }
    
    private fun registerActionHandlers() {
        // Claim reward action
        FlexUI.onAction("claim_reward") { action ->
            val reward = action.getString("reward") ?: "Unknown Reward"
            val amount = action.getString("amount") ?: "0"
            Toast.makeText(this, "🎉 Claimed $reward of ₹$amount!", Toast.LENGTH_LONG).show()
        }
        
        // Share achievement action
        FlexUI.onAction("share") { action ->
            val achievement = action.getString("achievement") ?: "achievement"
            Toast.makeText(this, "📤 Shared your $achievement!", Toast.LENGTH_SHORT).show()
        }
        
        // Navigate action
        FlexUI.onAction("navigate") { action ->
            val screen = action.getString("screen") ?: "home"
            Toast.makeText(this, "🧭 Navigating to $screen...", Toast.LENGTH_SHORT).show()
        }
        
        // Quiz answer selection
        FlexUI.onAction("select_answer") { action ->
            val answer = action.getString("answer") ?: "Unknown"
            val isCorrect = action.getBoolean("correct") ?: false
            val message = if (isCorrect) "✅ Correct! Good job!" else "❌ Oops! Try again next time."
            Toast.makeText(this, "$message Answer: $answer", Toast.LENGTH_LONG).show()
        }
        
        // Skip question
        FlexUI.onAction("skip") { _ ->
            Toast.makeText(this, "⏭️ Question skipped. Moving to next...", Toast.LENGTH_SHORT).show()
        }
        
        // Redeem reward
        FlexUI.onAction("redeem") { action ->
            val reward = action.getString("reward") ?: "reward"
            val points = action.getString("points") ?: "0"
            Toast.makeText(this, "🎁 Redeemed $reward for $points pts!", Toast.LENGTH_LONG).show()
        }
        
        // View details
        FlexUI.onAction("view_details") { action ->
            val item = action.getString("item") ?: "item"
            Toast.makeText(this, "👀 Viewing details for $item", Toast.LENGTH_SHORT).show()
        }
        
        // Generic callback handler
        FlexUI.onEvent("button_clicked") { action ->
            val data = action.getCallbackData()
            val message = data?.get("message") as? String ?: "Button clicked!"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupTabs() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    // JSON configs for each demo screen
    fun getScratchCardJson(): String {
        return """
        {
          "version": "1.0",
          "screenId": "scratch_card",
          "theme": {
            "colors": {
              "primary": "#FF6B00",
              "secondary": "#1A1A2E",
              "background": "#FFFFFF",
              "text": "#333333",
              "textSecondary": "#666666",
              "success": "#4CAF50",
              "surface": "#F8F8F8",
              "gold": "#FFD700",
              "orange": "#FF9800"
            },
            "typography": {
              "headingSize": 28,
              "bodySize": 16,
              "captionSize": 12,
              "buttonSize": 18
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
              "lg": 16
            }
          },
          "root": {
            "type": "scroll",
            "props": {
              "direction": "vertical"
            },
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
                    "type": "text",
                    "props": {
                      "content": "🎉 Congratulations! 🎉",
                      "fontSize": "{{typography.headingSize}}",
                      "color": "{{colors.primary}}",
                      "textAlign": "center",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.lg}}"
                    }
                  },
                  {
                    "type": "scratch_card_surface",
                    "style": {
                      "width": 280,
                      "height": 180,
                      "marginBottom": "{{spacing.xl}}",
                      "padding": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "column",
                        "props": {
                          "alignment": "center"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "💰 WINNER! 💰",
                              "fontSize": 20,
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
                              "content": "₹500 Cashback",
                              "fontSize": 32,
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
                      "elevation": 4,
                      "cornerRadius": "{{borderRadius.md}}"
                    },
                    "style": {
                      "padding": "{{spacing.lg}}",
                      "marginBottom": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "text",
                        "props": {
                          "content": "🎁 Reward Details",
                          "fontSize": "{{typography.bodySize}}",
                          "color": "{{colors.text}}",
                          "fontWeight": "bold"
                        },
                        "style": {
                          "marginBottom": "{{spacing.md}}"
                        }
                      },
                      {
                        "type": "divider",
                        "props": {
                          "color": "{{colors.textSecondary}}"
                        },
                        "style": {
                          "marginBottom": "{{spacing.md}}"
                        }
                      },
                      {
                        "type": "text",
                        "props": {
                          "content": "✅ Valid for 7 days\n⭐ Minimum order ₹999\n🚚 Free delivery included",
                          "fontSize": "{{typography.bodySize}}",
                          "color": "{{colors.textSecondary}}"
                        }
                      }
                    ]
                  },
                  {
                    "type": "button",
                    "props": {
                      "text": "🎯 Claim Reward",
                      "type": "filled",
                      "fontSize": "{{typography.buttonSize}}"
                    },
                    "action": {
                      "type": "claim_reward",
                      "reward": "Cashback",
                      "amount": "500"
                    },
                    "style": {
                      "marginBottom": "{{spacing.lg}}"
                    }
                  },
                  {
                    "type": "button",
                    "props": {
                      "text": "📄 Terms & Conditions",
                      "type": "text"
                    },
                    "action": {
                      "type": "navigate",
                      "screen": "terms"
                    }
                  }
                ]
              }
            ]
          }
        }
        """.trimIndent()
    }
    
    fun getSpinResultJson(): String {
        return """
        {
          "version": "1.0",
          "screenId": "spin_result",
          "theme": {
            "colors": {
              "primary": "#FF6B00",
              "secondary": "#1A1A2E",
              "background": "#FFFFFF",
              "text": "#333333",
              "textSecondary": "#666666",
              "success": "#4CAF50",
              "surface": "#F8F8F8",
              "gold": "#FFD700"
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
              "sm": 8,
              "md": 12,
              "lg": 16
            }
          },
          "root": {
            "type": "scroll",
            "props": {
              "direction": "vertical"
            },
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
                    "type": "text",
                    "props": {
                      "content": "🏆 Victory! 🏆",
                      "fontSize": "{{typography.headingSize}}",
                      "color": "{{colors.primary}}",
                      "textAlign": "center",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.lg}}"
                    }
                  },
                  {
                    "type": "card",
                    "props": {
                      "elevation": 8,
                      "cornerRadius": "{{borderRadius.lg}}"
                    },
                    "style": {
                      "padding": "{{spacing.xl}}",
                      "marginBottom": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "column",
                        "props": {
                          "alignment": "center"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "🥇",
                              "fontSize": 64,
                              "textAlign": "center"
                            },
                            "style": {
                              "marginBottom": "{{spacing.md}}"
                            }
                          },
                          {
                            "type": "text",
                            "props": {
                              "content": "You won Gold Tier!",
                              "fontSize": 22,
                              "color": "{{colors.text}}",
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
                      "elevation": 4,
                      "cornerRadius": "{{borderRadius.md}}"
                    },
                    "style": {
                      "padding": "{{spacing.lg}}",
                      "marginBottom": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "text",
                        "props": {
                          "content": "🎁 Prize Breakdown",
                          "fontSize": "{{typography.bodySize}}",
                          "color": "{{colors.text}}",
                          "fontWeight": "bold"
                        },
                        "style": {
                          "marginBottom": "{{spacing.md}}"
                        }
                      },
                      {
                        "type": "row",
                        "props": {
                          "alignment": "spaceBetween",
                          "verticalAlignment": "center"
                        },
                        "style": {
                          "marginBottom": "{{spacing.sm}}"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "⚡ Points Earned:",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.textSecondary}}"
                            }
                          },
                          {
                            "type": "text",
                            "props": {
                              "content": "+500 pts",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.success}}",
                              "fontWeight": "bold"
                            }
                          }
                        ]
                      },
                      {
                        "type": "row",
                        "props": {
                          "alignment": "spaceBetween",
                          "verticalAlignment": "center"
                        },
                        "style": {
                          "marginBottom": "{{spacing.sm}}"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "🏅 Badge Unlocked:",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.textSecondary}}"
                            }
                          },
                          {
                            "type": "text",
                            "props": {
                              "content": "Gold Spinner",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.gold}}",
                              "fontWeight": "bold"
                            }
                          }
                        ]
                      },
                      {
                        "type": "row",
                        "props": {
                          "alignment": "spaceBetween",
                          "verticalAlignment": "center"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "🔥 Streak Count:",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.textSecondary}}"
                            }
                          },
                          {
                            "type": "text",
                            "props": {
                              "content": "7 days",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.primary}}",
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
                      "elevation": 4,
                      "cornerRadius": "{{borderRadius.md}}"
                    },
                    "style": {
                      "padding": "{{spacing.lg}}",
                      "marginBottom": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "text",
                        "props": {
                          "content": "🏆 Leaderboard",
                          "fontSize": "{{typography.bodySize}}",
                          "color": "{{colors.text}}",
                          "fontWeight": "bold"
                        },
                        "style": {
                          "marginBottom": "{{spacing.md}}"
                        }
                      },
                      {
                        "type": "list",
                        "props": {
                          "spacing": "{{spacing.sm}}"
                        },
                        "children": [
                          {
                            "type": "row",
                            "props": {
                              "alignment": "spaceBetween",
                              "verticalAlignment": "center"
                            },
                            "style": {
                              "padding": "{{spacing.sm}}"
                            },
                            "children": [
                              {
                                "type": "row",
                                "props": {
                                  "verticalAlignment": "center",
                                  "spacing": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🥇",
                                      "fontSize": 20
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Priya K.",
                                      "fontSize": "{{typography.bodySize}}",
                                      "color": "{{colors.text}}",
                                      "fontWeight": "bold"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "2,850 pts",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.gold}}"
                                }
                              }
                            ]
                          },
                          {
                            "type": "row",
                            "props": {
                              "alignment": "spaceBetween",
                              "verticalAlignment": "center"
                            },
                            "style": {
                              "padding": "{{spacing.sm}}"
                            },
                            "children": [
                              {
                                "type": "row",
                                "props": {
                                  "verticalAlignment": "center",
                                  "spacing": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🥈",
                                      "fontSize": 20
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Raj S.",
                                      "fontSize": "{{typography.bodySize}}",
                                      "color": "{{colors.text}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "2,450 pts",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.textSecondary}}"
                                }
                              }
                            ]
                          },
                          {
                            "type": "row",
                            "props": {
                              "alignment": "spaceBetween",
                              "verticalAlignment": "center"
                            },
                            "style": {
                              "padding": "{{spacing.sm}}"
                            },
                            "children": [
                              {
                                "type": "row",
                                "props": {
                                  "verticalAlignment": "center",
                                  "spacing": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🥉",
                                      "fontSize": 20
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Maya L.",
                                      "fontSize": "{{typography.bodySize}}",
                                      "color": "{{colors.text}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "2,100 pts",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.textSecondary}}"
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "type": "row",
                    "props": {
                      "spacing": "{{spacing.md}}"
                    },
                    "children": [
                      {
                        "type": "button",
                        "props": {
                          "text": "📤 Share",
                          "type": "outlined"
                        },
                        "action": {
                          "type": "share",
                          "achievement": "Gold Tier win"
                        },
                        "style": {
                          "flex": 1
                        }
                      },
                      {
                        "type": "button",
                        "props": {
                          "text": "🚀 Continue",
                          "type": "filled"
                        },
                        "action": {
                          "type": "navigate",
                          "screen": "home"
                        },
                        "style": {
                          "flex": 1
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
    }
    
    fun getQuizJson(): String {
        return """
        {
          "version": "1.0",
          "screenId": "quiz_challenge",
          "theme": {
            "colors": {
              "primary": "#FF6B00",
              "secondary": "#1A1A2E",
              "background": "#FFFFFF",
              "text": "#333333",
              "textSecondary": "#666666",
              "success": "#4CAF50",
              "surface": "#F8F8F8",
              "accent": "#9C27B0"
            },
            "typography": {
              "headingSize": 20,
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
              "sm": 8,
              "md": 12,
              "lg": 16
            }
          },
          "root": {
            "type": "scroll",
            "props": {
              "direction": "vertical"
            },
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
                    "type": "row",
                    "props": {
                      "alignment": "spaceBetween",
                      "verticalAlignment": "center"
                    },
                    "style": {
                      "marginBottom": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "text",
                        "props": {
                          "content": "Question 3 of 10",
                          "fontSize": "{{typography.bodySize}}",
                          "color": "{{colors.textSecondary}}"
                        }
                      },
                      {
                        "type": "card",
                        "props": {
                          "elevation": 2,
                          "cornerRadius": "{{borderRadius.sm}}"
                        },
                        "style": {
                          "padding": "{{spacing.sm}}"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "⏰ 00:23",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.primary}}",
                              "fontWeight": "bold"
                            }
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "type": "progress_bar",
                    "props": {
                      "progress": 0.3
                    },
                    "style": {
                      "height": 8,
                      "marginBottom": "{{spacing.xl}}"
                    }
                  },
                  {
                    "type": "card",
                    "props": {
                      "elevation": 4,
                      "cornerRadius": "{{borderRadius.md}}"
                    },
                    "style": {
                      "padding": "{{spacing.lg}}",
                      "marginBottom": "{{spacing.xl}}"
                    },
                    "children": [
                      {
                        "type": "text",
                        "props": {
                          "content": "🧠 Which design pattern is commonly used in Android development for decoupling UI components from business logic?",
                          "fontSize": "{{typography.headingSize}}",
                          "color": "{{colors.text}}",
                          "textAlign": "center"
                        }
                      }
                    ]
                  },
                  {
                    "type": "list",
                    "props": {
                      "spacing": "{{spacing.md}}"
                    },
                    "children": [
                      {
                        "type": "card",
                        "props": {
                          "elevation": 2,
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "action": {
                          "type": "select_answer",
                          "answer": "A. Singleton Pattern",
                          "correct": false
                        },
                        "style": {
                          "padding": "{{spacing.lg}}"
                        },
                        "children": [
                          {
                            "type": "row",
                            "props": {
                              "verticalAlignment": "center",
                              "spacing": "{{spacing.md}}"
                            },
                            "children": [
                              {
                                "type": "text",
                                "props": {
                                  "content": "A",
                                  "fontSize": 18,
                                  "color": "{{colors.primary}}",
                                  "fontWeight": "bold"
                                }
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "Singleton Pattern",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.text}}"
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
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "action": {
                          "type": "select_answer",
                          "answer": "B. MVVM Pattern",
                          "correct": true
                        },
                        "style": {
                          "padding": "{{spacing.lg}}"
                        },
                        "children": [
                          {
                            "type": "row",
                            "props": {
                              "verticalAlignment": "center",
                              "spacing": "{{spacing.md}}"
                            },
                            "children": [
                              {
                                "type": "text",
                                "props": {
                                  "content": "B",
                                  "fontSize": 18,
                                  "color": "{{colors.primary}}",
                                  "fontWeight": "bold"
                                }
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "MVVM Pattern",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.text}}"
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
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "action": {
                          "type": "select_answer",
                          "answer": "C. Factory Pattern",
                          "correct": false
                        },
                        "style": {
                          "padding": "{{spacing.lg}}"
                        },
                        "children": [
                          {
                            "type": "row",
                            "props": {
                              "verticalAlignment": "center",
                              "spacing": "{{spacing.md}}"
                            },
                            "children": [
                              {
                                "type": "text",
                                "props": {
                                  "content": "C",
                                  "fontSize": 18,
                                  "color": "{{colors.primary}}",
                                  "fontWeight": "bold"
                                }
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "Factory Pattern",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.text}}"
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
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "action": {
                          "type": "select_answer",
                          "answer": "D. Observer Pattern",
                          "correct": false
                        },
                        "style": {
                          "padding": "{{spacing.lg}}"
                        },
                        "children": [
                          {
                            "type": "row",
                            "props": {
                              "verticalAlignment": "center",
                              "spacing": "{{spacing.md}}"
                            },
                            "children": [
                              {
                                "type": "text",
                                "props": {
                                  "content": "D",
                                  "fontSize": 18,
                                  "color": "{{colors.primary}}",
                                  "fontWeight": "bold"
                                }
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "Observer Pattern",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.text}}"
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "type": "spacer",
                    "props": {
                      "size": "{{spacing.xl}}"
                    }
                  },
                  {
                    "type": "row",
                    "props": {
                      "alignment": "spaceBetween",
                      "verticalAlignment": "center"
                    },
                    "children": [
                      {
                        "type": "card",
                        "props": {
                          "elevation": 2,
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "style": {
                          "padding": "{{spacing.md}}"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "Score: 250 pts ⚡",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.success}}",
                              "fontWeight": "bold"
                            }
                          }
                        ]
                      },
                      {
                        "type": "button",
                        "props": {
                          "text": "⏭️ Skip Question",
                          "type": "text"
                        },
                        "action": {
                          "type": "skip"
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
    }
    
    fun getRewardsDashboardJson(): String {
        return """
        {
          "version": "1.0",
          "screenId": "rewards_dashboard",
          "theme": {
            "colors": {
              "primary": "#FF6B00",
              "secondary": "#1A1A2E",
              "background": "#FFFFFF",
              "text": "#333333",
              "textSecondary": "#666666",
              "success": "#4CAF50",
              "surface": "#F8F8F8",
              "accent": "#E91E63"
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
              "sm": 8,
              "md": 12,
              "lg": 16
            }
          },
          "root": {
            "type": "scroll",
            "props": {
              "direction": "vertical"
            },
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
                    "type": "card",
                    "props": {
                      "elevation": 6,
                      "cornerRadius": "{{borderRadius.lg}}"
                    },
                    "style": {
                      "padding": "{{spacing.lg}}",
                      "marginBottom": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "row",
                        "props": {
                          "alignment": "spaceBetween",
                          "verticalAlignment": "center"
                        },
                        "children": [
                          {
                            "type": "column",
                            "children": [
                              {
                                "type": "text",
                                "props": {
                                  "content": "Hey Priya! 👋",
                                  "fontSize": "{{typography.headingSize}}",
                                  "color": "{{colors.text}}",
                                  "fontWeight": "bold"
                                }
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "You're doing great!",
                                  "fontSize": "{{typography.bodySize}}",
                                  "color": "{{colors.textSecondary}}"
                                }
                              }
                            ]
                          },
                          {
                            "type": "card",
                            "props": {
                              "elevation": 4,
                              "cornerRadius": "{{borderRadius.md}}"
                            },
                            "style": {
                              "padding": "{{spacing.md}}"
                            },
                            "children": [
                              {
                                "type": "text",
                                "props": {
                                  "content": "⚡ 2,450 pts",
                                  "fontSize": 18,
                                  "color": "{{colors.primary}}",
                                  "fontWeight": "bold"
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "type": "text",
                    "props": {
                      "content": "🎯 Categories",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.text}}",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}"
                    }
                  },
                  {
                    "type": "scroll",
                    "props": {
                      "direction": "horizontal"
                    },
                    "style": {
                      "marginBottom": "{{spacing.xl}}"
                    },
                    "children": [
                      {
                        "type": "row",
                        "props": {
                          "spacing": "{{spacing.md}}"
                        },
                        "children": [
                          {
                            "type": "card",
                            "props": {
                              "elevation": 3,
                              "cornerRadius": "{{borderRadius.md}}"
                            },
                            "style": {
                              "padding": "{{spacing.md}}",
                              "width": 100
                            },
                            "children": [
                              {
                                "type": "column",
                                "props": {
                                  "alignment": "center"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "💰",
                                      "fontSize": 32,
                                      "textAlign": "center"
                                    },
                                    "style": {
                                      "marginBottom": "{{spacing.xs}}"
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Cashback",
                                      "fontSize": "{{typography.captionSize}}",
                                      "color": "{{colors.textSecondary}}",
                                      "textAlign": "center"
                                    }
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "type": "card",
                            "props": {
                              "elevation": 3,
                              "cornerRadius": "{{borderRadius.md}}"
                            },
                            "style": {
                              "padding": "{{spacing.md}}",
                              "width": 100
                            },
                            "children": [
                              {
                                "type": "column",
                                "props": {
                                  "alignment": "center"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🎫",
                                      "fontSize": 32,
                                      "textAlign": "center"
                                    },
                                    "style": {
                                      "marginBottom": "{{spacing.xs}}"
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Vouchers",
                                      "fontSize": "{{typography.captionSize}}",
                                      "color": "{{colors.textSecondary}}",
                                      "textAlign": "center"
                                    }
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "type": "card",
                            "props": {
                              "elevation": 3,
                              "cornerRadius": "{{borderRadius.md}}"
                            },
                            "style": {
                              "padding": "{{spacing.md}}",
                              "width": 100
                            },
                            "children": [
                              {
                                "type": "column",
                                "props": {
                                  "alignment": "center"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "📦",
                                      "fontSize": 32,
                                      "textAlign": "center"
                                    },
                                    "style": {
                                      "marginBottom": "{{spacing.xs}}"
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Products",
                                      "fontSize": "{{typography.captionSize}}",
                                      "color": "{{colors.textSecondary}}",
                                      "textAlign": "center"
                                    }
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "type": "card",
                            "props": {
                              "elevation": 3,
                              "cornerRadius": "{{borderRadius.md}}"
                            },
                            "style": {
                              "padding": "{{spacing.md}}",
                              "width": 100
                            },
                            "children": [
                              {
                                "type": "column",
                                "props": {
                                  "alignment": "center"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🎭",
                                      "fontSize": 32,
                                      "textAlign": "center"
                                    },
                                    "style": {
                                      "marginBottom": "{{spacing.xs}}"
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Events",
                                      "fontSize": "{{typography.captionSize}}",
                                      "color": "{{colors.textSecondary}}",
                                      "textAlign": "center"
                                    }
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "type": "text",
                    "props": {
                      "content": "🎁 Available Rewards",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.text}}",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}"
                    }
                  },
                  {
                    "type": "grid",
                    "props": {
                      "columns": 2,
                      "spacing": "{{spacing.md}}"
                    },
                    "style": {
                      "marginBottom": "{{spacing.xl}}"
                    },
                    "children": [
                      {
                        "type": "card",
                        "props": {
                          "elevation": 4,
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "style": {
                          "padding": "{{spacing.md}}"
                        },
                        "children": [
                          {
                            "type": "column",
                            "children": [
                              {
                                "type": "container",
                                "style": {
                                  "height": 80,
                                  "backgroundColor": "{{colors.surface}}",
                                  "marginBottom": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🛍️",
                                      "fontSize": 40,
                                      "textAlign": "center"
                                    },
                                    "style": {
                                      "padding": "{{spacing.md}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "₹200 Amazon Voucher",
                                  "fontSize": "{{typography.bodySize}}",
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
                                  "content": "800 pts required",
                                  "fontSize": "{{typography.captionSize}}",
                                  "color": "{{colors.textSecondary}}"
                                },
                                "style": {
                                  "marginBottom": "{{spacing.sm}}"
                                }
                              },
                              {
                                "type": "button",
                                "props": {
                                  "text": "Redeem",
                                  "type": "filled",
                                  "fontSize": "{{typography.captionSize}}"
                                },
                                "action": {
                                  "type": "redeem",
                                  "reward": "Amazon Voucher ₹200",
                                  "points": "800"
                                }
                              }
                            ]
                          }
                        ]
                      },
                      {
                        "type": "card",
                        "props": {
                          "elevation": 4,
                          "cornerRadius": "{{borderRadius.md}}"
                        },
                        "style": {
                          "padding": "{{spacing.md}}"
                        },
                        "children": [
                          {
                            "type": "column",
                            "children": [
                              {
                                "type": "container",
                                "style": {
                                  "height": 80,
                                  "backgroundColor": "{{colors.surface}}",
                                  "marginBottom": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🍕",
                                      "fontSize": 40,
                                      "textAlign": "center"
                                    },
                                    "style": {
                                      "padding": "{{spacing.md}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "Free Pizza",
                                  "fontSize": "{{typography.bodySize}}",
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
                                  "content": "1,200 pts required",
                                  "fontSize": "{{typography.captionSize}}",
                                  "color": "{{colors.textSecondary}}"
                                },
                                "style": {
                                  "marginBottom": "{{spacing.sm}}"
                                }
                              },
                              {
                                "type": "button",
                                "props": {
                                  "text": "Redeem",
                                  "type": "filled",
                                  "fontSize": "{{typography.captionSize}}"
                                },
                                "action": {
                                  "type": "redeem",
                                  "reward": "Free Pizza",
                                  "points": "1200"
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "type": "text",
                    "props": {
                      "content": "📈 Recent Activity",
                      "fontSize": "{{typography.bodySize}}",
                      "color": "{{colors.text}}",
                      "fontWeight": "bold"
                    },
                    "style": {
                      "marginBottom": "{{spacing.md}}"
                    }
                  },
                  {
                    "type": "card",
                    "props": {
                      "elevation": 2,
                      "cornerRadius": "{{borderRadius.md}}"
                    },
                    "style": {
                      "padding": "{{spacing.lg}}"
                    },
                    "children": [
                      {
                        "type": "list",
                        "props": {
                          "spacing": "{{spacing.md}}"
                        },
                        "children": [
                          {
                            "type": "row",
                            "props": {
                              "alignment": "spaceBetween",
                              "verticalAlignment": "center"
                            },
                            "children": [
                              {
                                "type": "row",
                                "props": {
                                  "verticalAlignment": "center",
                                  "spacing": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "✅",
                                      "fontSize": 16
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Quiz completed",
                                      "fontSize": "{{typography.bodySize}}",
                                      "color": "{{colors.text}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "+50 pts",
                                  "fontSize": "{{typography.captionSize}}",
                                  "color": "{{colors.success}}"
                                }
                              }
                            ]
                          },
                          {
                            "type": "divider",
                            "props": {
                              "color": "{{colors.surface}}"
                            }
                          },
                          {
                            "type": "row",
                            "props": {
                              "alignment": "spaceBetween",
                              "verticalAlignment": "center"
                            },
                            "children": [
                              {
                                "type": "row",
                                "props": {
                                  "verticalAlignment": "center",
                                  "spacing": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🎁",
                                      "fontSize": 16
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Redeemed coffee voucher",
                                      "fontSize": "{{typography.bodySize}}",
                                      "color": "{{colors.text}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "-200 pts",
                                  "fontSize": "{{typography.captionSize}}",
                                  "color": "{{colors.textSecondary}}"
                                }
                              }
                            ]
                          },
                          {
                            "type": "divider",
                            "props": {
                              "color": "{{colors.surface}}"
                            }
                          },
                          {
                            "type": "row",
                            "props": {
                              "alignment": "spaceBetween",
                              "verticalAlignment": "center"
                            },
                            "children": [
                              {
                                "type": "row",
                                "props": {
                                  "verticalAlignment": "center",
                                  "spacing": "{{spacing.sm}}"
                                },
                                "children": [
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "🏆",
                                      "fontSize": 16
                                    }
                                  },
                                  {
                                    "type": "text",
                                    "props": {
                                      "content": "Daily streak bonus",
                                      "fontSize": "{{typography.bodySize}}",
                                      "color": "{{colors.text}}"
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "text",
                                "props": {
                                  "content": "+100 pts",
                                  "fontSize": "{{typography.captionSize}}",
                                  "color": "{{colors.success}}"
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        }
        """.trimIndent()
    }
    
    fun getWidgetDemoJson(): String {
        return """
        {
          "version": "1.0",
          "screenId": "widget_demo",
          "theme": {
            "colors": {
              "primary": "#FF6B00",
              "secondary": "#1A1A2E",
              "background": "#FFFFFF",
              "text": "#333333",
              "textSecondary": "#666666",
              "success": "#4CAF50",
              "surface": "#F8F8F8"
            },
            "typography": {
              "headingSize": 18,
              "bodySize": 14,
              "captionSize": 10
            },
            "spacing": {
              "xs": 4,
              "sm": 6,
              "md": 12,
              "lg": 16,
              "xl": 20
            },
            "borderRadius": {
              "sm": 6,
              "md": 10,
              "lg": 12
            }
          },
          "root": {
            "type": "card",
            "props": {
              "elevation": 6,
              "cornerRadius": "{{borderRadius.md}}"
            },
            "style": {
              "marginTop": "{{spacing.md}}",
              "marginBottom": "{{spacing.md}}",
              "marginLeft": "{{spacing.lg}}",
              "marginRight": "{{spacing.lg}}"
            },
            "children": [
              {
                "type": "container",
                "style": {
                  "padding": "{{spacing.lg}}"
                },
                "children": [
                  {
                    "type": "column",
                    "children": [
                      {
                        "type": "row",
                        "props": {
                          "alignment": "spaceBetween",
                          "verticalAlignment": "center"
                        },
                        "style": {
                          "marginBottom": "{{spacing.md}}"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "🎁 Complete 3 more orders for a surprise reward!",
                              "fontSize": "{{typography.bodySize}}",
                              "color": "{{colors.text}}",
                              "fontWeight": "bold"
                            }
                          }
                        ]
                      },
                      {
                        "type": "progress_bar",
                        "props": {
                          "progress": 0.7
                        },
                        "style": {
                          "height": 6,
                          "marginBottom": "{{spacing.md}}"
                        }
                      },
                      {
                        "type": "row",
                        "props": {
                          "alignment": "spaceBetween",
                          "verticalAlignment": "center"
                        },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "content": "2/5 orders complete",
                              "fontSize": "{{typography.captionSize}}",
                              "color": "{{colors.textSecondary}}"
                            }
                          },
                          {
                            "type": "button",
                            "props": {
                              "text": "View Details",
                              "type": "filled",
                              "fontSize": "{{typography.captionSize}}"
                            },
                            "action": {
                              "type": "view_details",
                              "item": "reward progress"
                            }
                          }
                        ]
                      },
                      {
                        "type": "spacer",
                        "props": {
                          "size": "{{spacing.sm}}"
                        }
                      },
                      {
                        "type": "text",
                        "props": {
                          "content": "Powered by FlexUI ⚡",
                          "fontSize": "{{typography.captionSize}}",
                          "color": "{{colors.textSecondary}}",
                          "textAlign": "center"
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
    }
}