package dev.flexui.schema

import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject

class FlexThemeTest {

    @Test
    fun testDefaultTheme() {
        val theme = FlexTheme.getDefault()

        // Test default colors
        assertEquals("#FF6B00", theme.getColor("primary"))
        assertEquals("#1A1A2E", theme.getColor("secondary"))
        assertEquals("#FFFFFF", theme.getColor("background"))
        assertEquals("#333333", theme.getColor("text"))
        assertEquals("#999999", theme.getColor("textSecondary"))
        assertEquals("#4CAF50", theme.getColor("success"))
        assertEquals("#F44336", theme.getColor("error"))
        assertEquals("#00000000", theme.getColor("transparent"))

        // Test default typography
        assertEquals("sans-serif", theme.getFontFamily("headingFont"))
        assertEquals("sans-serif", theme.getFontFamily("bodyFont"))
        assertEquals(24f, theme.getFontSize("headingSize"))
        assertEquals(14f, theme.getFontSize("bodySize"))
        assertEquals(12f, theme.getFontSize("captionSize"))

        // Test default spacing
        assertEquals(4, theme.getSpacing("xs"))
        assertEquals(8, theme.getSpacing("sm"))
        assertEquals(16, theme.getSpacing("md"))
        assertEquals(24, theme.getSpacing("lg"))
        assertEquals(32, theme.getSpacing("xl"))

        // Test default border radius
        assertEquals(4, theme.getBorderRadius("sm"))
        assertEquals(8, theme.getBorderRadius("md"))
        assertEquals(16, theme.getBorderRadius("lg"))
        assertEquals(999, theme.getBorderRadius("full"))
    }

    @Test
    fun testColorResolution() {
        val json = JSONObject("""
        {
            "colors": {
                "primary": "#FF6B00",
                "secondary": "#1A1A2E",
                "accent": "FF5722",
                "invalid": "not-a-color"
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        // Test valid hex colors
        assertEquals("#FF6B00", theme.getColor("primary"))
        assertEquals("#1A1A2E", theme.getColor("secondary"))
        assertEquals("FF5722", theme.getColor("accent"))

        // Test color parsing as Android Color int
        assertEquals(-38912, theme.getColorInt("primary")) // #FF6B00 as int
        assertEquals(-15592402, theme.getColorInt("secondary")) // #1A1A2E as int
        
        // Test hex without # prefix
        assertEquals(-10972926, theme.getColorInt("accent")) // #FF5722 as int

        // Test invalid color
        assertEquals("not-a-color", theme.getColor("invalid"))
        assertNull(theme.getColorInt("invalid"))

        // Test non-existent color
        assertNull(theme.getColor("nonexistent"))
        assertNull(theme.getColorInt("nonexistent"))
    }

    @Test
    fun testTypographyResolution() {
        val json = JSONObject("""
        {
            "typography": {
                "headingSize": 24,
                "bodySize": 14.5,
                "captionSize": "12",
                "invalidSize": "not-a-number",
                "headingFont": "Georgia",
                "bodyFont": "Roboto",
                "mixed": 18
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        // Test font size parsing
        assertEquals(24f, theme.getFontSize("headingSize"))
        assertEquals(14.5f, theme.getFontSize("bodySize"))
        assertEquals(12f, theme.getFontSize("captionSize"))
        assertNull(theme.getFontSize("invalidSize"))
        assertNull(theme.getFontSize("nonexistent"))

        // Test font family parsing
        assertEquals("Georgia", theme.getFontFamily("headingFont"))
        assertEquals("Roboto", theme.getFontFamily("bodyFont"))
        assertNull(theme.getFontFamily("mixed")) // Number, not string
        assertNull(theme.getFontFamily("nonexistent"))

        // Test raw typography access
        assertEquals(18, theme.getTypography("mixed"))
        assertEquals("Georgia", theme.getTypography("headingFont"))
    }

    @Test
    fun testSpacingResolution() {
        val json = JSONObject("""
        {
            "spacing": {
                "xs": 4,
                "sm": 8,
                "md": 16,
                "lg": 24,
                "xl": 32,
                "invalid": "not-a-number",
                "float": 12.5
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        assertEquals(4, theme.getSpacing("xs"))
        assertEquals(8, theme.getSpacing("sm"))
        assertEquals(16, theme.getSpacing("md"))
        assertEquals(24, theme.getSpacing("lg"))
        assertEquals(32, theme.getSpacing("xl"))
        assertEquals(12, theme.getSpacing("float")) // Float converted to int
        assertNull(theme.getSpacing("invalid"))
        assertNull(theme.getSpacing("nonexistent"))
    }

    @Test
    fun testBorderRadiusResolution() {
        val json = JSONObject("""
        {
            "borderRadius": {
                "none": 0,
                "sm": 4,
                "md": 8,
                "lg": 16,
                "full": 999,
                "custom": 12,
                "invalid": "not-a-number"
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        assertEquals(0, theme.getBorderRadius("none"))
        assertEquals(4, theme.getBorderRadius("sm"))
        assertEquals(8, theme.getBorderRadius("md"))
        assertEquals(16, theme.getBorderRadius("lg"))
        assertEquals(999, theme.getBorderRadius("full"))
        assertEquals(12, theme.getBorderRadius("custom"))
        assertNull(theme.getBorderRadius("invalid"))
        assertNull(theme.getBorderRadius("nonexistent"))
    }

    @Test
    fun testEmptyTheme() {
        val json = JSONObject("{}")
        val theme = FlexTheme.fromJson(json)

        assertTrue(theme.colors.isEmpty())
        assertTrue(theme.typography.isEmpty())
        assertTrue(theme.spacing.isEmpty())
        assertTrue(theme.borderRadius.isEmpty())

        assertNull(theme.getColor("primary"))
        assertNull(theme.getSpacing("md"))
        assertNull(theme.getBorderRadius("sm"))
        assertNull(theme.getFontSize("bodySize"))
    }

    @Test
    fun testPartialTheme() {
        val json = JSONObject("""
        {
            "colors": {
                "primary": "#FF6B00"
            },
            "spacing": {
                "md": 16
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        assertEquals("#FF6B00", theme.getColor("primary"))
        assertNull(theme.getColor("secondary"))
        
        assertEquals(16, theme.getSpacing("md"))
        assertNull(theme.getSpacing("lg"))
        
        assertTrue(theme.typography.isEmpty())
        assertTrue(theme.borderRadius.isEmpty())
    }

    @Test
    fun testColorFormats() {
        val json = JSONObject("""
        {
            "colors": {
                "withHash": "#FF6B00",
                "withoutHash": "FF6B00",
                "shortHex": "#F60",
                "rgba": "#FF6B00FF",
                "empty": "",
                "invalid": "xyz123"
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        // Valid colors should return the string as-is
        assertEquals("#FF6B00", theme.getColor("withHash"))
        assertEquals("FF6B00", theme.getColor("withoutHash"))
        assertEquals("#F60", theme.getColor("shortHex"))
        assertEquals("#FF6B00FF", theme.getColor("rgba"))

        // Color int parsing should handle # prefix properly
        assertNotNull(theme.getColorInt("withHash"))
        assertNotNull(theme.getColorInt("withoutHash"))
        
        // Invalid colors should return null for int parsing
        assertNull(theme.getColorInt("empty"))
        assertNull(theme.getColorInt("invalid"))
        
        // But should return the raw string for string access
        assertEquals("", theme.getColor("empty"))
        assertEquals("xyz123", theme.getColor("invalid"))
    }

    @Test
    fun testThemeOverrides() {
        val json = JSONObject("""
        {
            "colors": {
                "primary": "#FF6B00",
                "secondary": "#1A1A2E"
            },
            "typography": {
                "headingSize": 28,
                "bodySize": 16
            },
            "spacing": {
                "md": 20,
                "lg": 30
            },
            "borderRadius": {
                "md": 12,
                "lg": 20
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        // Verify custom values override defaults
        assertEquals("#FF6B00", theme.getColor("primary"))
        assertEquals("#1A1A2E", theme.getColor("secondary"))
        assertEquals(28f, theme.getFontSize("headingSize"))
        assertEquals(16f, theme.getFontSize("bodySize"))
        assertEquals(20, theme.getSpacing("md"))
        assertEquals(30, theme.getSpacing("lg"))
        assertEquals(12, theme.getBorderRadius("md"))
        assertEquals(20, theme.getBorderRadius("lg"))
    }

    @Test
    fun testComplexTypographyValues() {
        val json = JSONObject("""
        {
            "typography": {
                "fontWeight": "bold",
                "textAlign": "center",
                "lineHeight": 1.5,
                "letterSpacing": 0.5,
                "isItalic": true,
                "complexValue": {
                    "fontSize": 16,
                    "fontFamily": "Roboto"
                }
            }
        }
        """.trimIndent())

        val theme = FlexTheme.fromJson(json)

        assertEquals("bold", theme.getTypography("fontWeight"))
        assertEquals("center", theme.getTypography("textAlign"))
        assertEquals(1.5, theme.getTypography("lineHeight"))
        assertEquals(0.5, theme.getTypography("letterSpacing"))
        assertEquals(true, theme.getTypography("isItalic"))
        
        // Complex object should be preserved
        val complexValue = theme.getTypography("complexValue")
        assertNotNull(complexValue)
        
        // Font size/family should work for simple values
        assertEquals(1.5f, theme.getFontSize("lineHeight"))
        assertEquals(0.5f, theme.getFontSize("letterSpacing"))
        assertNull(theme.getFontSize("complexValue")) // Can't parse object as float
        
        assertEquals("bold", theme.getFontFamily("fontWeight"))
        assertEquals("center", theme.getFontFamily("textAlign"))
        assertNull(theme.getFontFamily("lineHeight")) // Number, not string
    }
}