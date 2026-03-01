package dev.flexui.schema

import org.junit.Test
import org.junit.Assert.*

class FlexParserTest {

    @Test
    fun testValidConfig() {
        val json = """
        {
            "version": "1.0",
            "screenId": "test_screen",
            "theme": {
                "colors": {
                    "primary": "#FF6B00",
                    "background": "#FFFFFF"
                }
            },
            "root": {
                "type": "container",
                "props": { "padding": 16 }
            }
        }
        """.trimIndent()

        val config = FlexParser.parseConfig(json)

        assertEquals("1.0", config.version)
        assertEquals("test_screen", config.screenId)
        assertEquals("container", config.root.type)
        assertEquals("#FF6B00", config.theme.getColor("primary"))
        assertEquals("#FFFFFF", config.theme.getColor("background"))
    }

    @Test
    fun testMinimalConfig() {
        val json = """
        {
            "screenId": "minimal",
            "root": {
                "type": "text",
                "props": { "content": "Hello" }
            }
        }
        """.trimIndent()

        val config = FlexParser.parseConfig(json)

        assertEquals("1.0", config.version) // default version
        assertEquals("minimal", config.screenId)
        assertEquals("text", config.root.type)
        assertNotNull(config.theme) // default theme
        assertTrue(config.actions.isEmpty())
    }

    @Test(expected = FlexParseException::class)
    fun testInvalidJson() {
        val invalidJson = """{ invalid json """
        FlexParser.parseConfig(invalidJson)
    }

    @Test(expected = FlexParseException::class)
    fun testMissingScreenId() {
        val json = """
        {
            "version": "1.0",
            "root": {
                "type": "text",
                "props": { "content": "Hello" }
            }
        }
        """.trimIndent()

        FlexParser.parseConfig(json)
    }

    @Test(expected = FlexParseException::class)
    fun testMissingRoot() {
        val json = """
        {
            "version": "1.0",
            "screenId": "test"
        }
        """.trimIndent()

        FlexParser.parseConfig(json)
    }

    @Test
    fun testConfigWithActions() {
        val json = """
        {
            "screenId": "action_test",
            "root": {
                "type": "button",
                "props": { "text": "Click me" }
            },
            "actions": {
                "click_action": {
                    "type": "callback",
                    "event": "button_clicked",
                    "data": { "button_id": "btn1" }
                }
            }
        }
        """.trimIndent()

        val config = FlexParser.parseConfig(json)

        assertEquals(1, config.actions.size)
        assertTrue(config.actions.containsKey("click_action"))
        
        val action = config.actions["click_action"]!!
        assertEquals("callback", action.type)
        assertEquals("button_clicked", action.event)
    }

    @Test
    fun testParseNode() {
        val json = """
        {
            "type": "column",
            "props": { "spacing": 8 },
            "children": [
                {
                    "type": "text",
                    "props": { "content": "Hello" }
                },
                {
                    "type": "text", 
                    "props": { "content": "World" }
                }
            ]
        }
        """.trimIndent()

        val node = FlexParser.parseNode(json)

        assertEquals("column", node.type)
        assertEquals(8, node.props.getInt("spacing"))
        assertEquals(2, node.children?.size)
        assertEquals("Hello", node.children?.get(0)?.props?.getString("content"))
        assertEquals("World", node.children?.get(1)?.props?.getString("content"))
    }

    @Test
    fun testParseTheme() {
        val json = """
        {
            "colors": {
                "primary": "#FF6B00",
                "secondary": "#1A1A2E",
                "background": "#FFFFFF"
            },
            "typography": {
                "headingSize": 24,
                "bodySize": 14
            },
            "spacing": {
                "sm": 8,
                "md": 16,
                "lg": 24
            }
        }
        """.trimIndent()

        val theme = FlexParser.parseTheme(json)

        assertEquals("#FF6B00", theme.getColor("primary"))
        assertEquals("#1A1A2E", theme.getColor("secondary"))
        assertEquals("#FFFFFF", theme.getColor("background"))
        assertEquals(24f, theme.getFontSize("headingSize"))
        assertEquals(14f, theme.getFontSize("bodySize"))
        assertEquals(8, theme.getSpacing("sm"))
        assertEquals(16, theme.getSpacing("md"))
        assertEquals(24, theme.getSpacing("lg"))
    }

    @Test
    fun testParseAction() {
        val json = """
        {
            "type": "navigate",
            "target": "screen2",
            "data": {
                "userId": 123,
                "category": "products"
            }
        }
        """.trimIndent()

        val action = FlexParser.parseAction(json)

        assertEquals("navigate", action.type)
        assertEquals("screen2", action.target)
        assertEquals(123, action.data?.get("userId"))
        assertEquals("products", action.data?.get("category"))
    }

    @Test
    fun testValidateValidConfig() {
        val json = """
        {
            "screenId": "valid_screen",
            "root": {
                "type": "container",
                "props": { "padding": 16 }
            }
        }
        """.trimIndent()

        val result = FlexParser.validateConfig(json)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testValidateInvalidConfig() {
        val json = """
        {
            "screenId": "",
            "root": {
                "type": "",
                "props": {}
            }
        }
        """.trimIndent()

        val result = FlexParser.validateConfig(json)

        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("screenId is required") })
        assertTrue(result.errors.any { it.contains("node type is required") })
    }

    @Test
    fun testValidateWithParseError() {
        val invalidJson = """{ invalid """

        val result = FlexParser.validateConfig(invalidJson)

        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.any { it.contains("Parse error") })
    }

    @Test
    fun testComplexNestedStructure() {
        val json = """
        {
            "screenId": "complex",
            "root": {
                "type": "scroll",
                "children": [
                    {
                        "type": "column",
                        "children": [
                            {
                                "type": "row",
                                "children": [
                                    {
                                        "type": "text",
                                        "props": { "content": "Left" }
                                    },
                                    {
                                        "type": "text", 
                                        "props": { "content": "Right" }
                                    }
                                ]
                            },
                            {
                                "type": "list",
                                "props": { "itemCount": 5 }
                            }
                        ]
                    }
                ]
            }
        }
        """.trimIndent()

        val config = FlexParser.parseConfig(json)

        assertEquals("scroll", config.root.type)
        val column = config.root.children?.get(0)
        assertEquals("column", column?.type)
        assertEquals(2, column?.children?.size)
        
        val row = column?.children?.get(0)
        assertEquals("row", row?.type)
        assertEquals(2, row?.children?.size)
        
        val list = column?.children?.get(1)
        assertEquals("list", list?.type)
        assertEquals(5, list?.props?.getInt("itemCount"))
    }

    @Test
    fun testEdgeCases() {
        // Empty props object
        val json1 = """
        {
            "screenId": "edge1",
            "root": {
                "type": "container",
                "props": {}
            }
        }
        """.trimIndent()

        val config1 = FlexParser.parseConfig(json1)
        assertTrue(config1.root.props.isEmpty())

        // No children array
        val json2 = """
        {
            "screenId": "edge2",
            "root": {
                "type": "text",
                "props": { "content": "Solo" }
            }
        }
        """.trimIndent()

        val config2 = FlexParser.parseConfig(json2)
        assertNull(config2.root.children)

        // Empty children array
        val json3 = """
        {
            "screenId": "edge3",
            "root": {
                "type": "column",
                "props": {},
                "children": []
            }
        }
        """.trimIndent()

        val config3 = FlexParser.parseConfig(json3)
        assertEquals(0, config3.root.children?.size)
    }
}