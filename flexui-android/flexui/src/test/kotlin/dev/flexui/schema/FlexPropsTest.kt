package dev.flexui.schema

import org.junit.Test
import org.junit.Assert.*

class FlexPropsTest {

    @Test
    fun testStringAccessors() {
        val data = mapOf(
            "text" to "Hello World",
            "number" to 42,
            "boolean" to true,
            "float" to 3.14,
            "emptyString" to "",
            "nullValue" to null
        )
        val props = FlexProps(data)

        // String values
        assertEquals("Hello World", props.getString("text"))
        assertEquals("Hello World", props.getString("text", "default"))

        // Number to string conversion
        assertEquals("42", props.getString("number"))
        assertEquals("3.14", props.getString("float"))

        // Boolean to string conversion
        assertEquals("true", props.getString("boolean"))

        // Empty string
        assertEquals("", props.getString("emptyString"))

        // Non-existent key with default
        assertEquals("default", props.getString("missing", "default"))
        assertNull(props.getString("missing"))

        // Null value with default
        assertEquals("default", props.getString("nullValue", "default"))
        assertNull(props.getString("nullValue"))
    }

    @Test
    fun testRequiredString() {
        val props = FlexProps(mapOf("text" to "Hello"))

        assertEquals("Hello", props.getRequiredString("text"))

        try {
            props.getRequiredString("missing")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Required property 'missing' is missing"))
        }
    }

    @Test
    fun testIntAccessors() {
        val data = mapOf(
            "intValue" to 42,
            "longValue" to 100L,
            "floatValue" to 3.14f,
            "doubleValue" to 2.718,
            "stringInt" to "123",
            "stringInvalid" to "not-a-number",
            "boolean" to true,
            "nullValue" to null
        )
        val props = FlexProps(data)

        // Direct int
        assertEquals(42, props.getInt("intValue"))

        // Number conversions
        assertEquals(100, props.getInt("longValue"))
        assertEquals(3, props.getInt("floatValue"))
        assertEquals(2, props.getInt("doubleValue"))

        // String to int conversion
        assertEquals(123, props.getInt("stringInt"))

        // Invalid string with default
        assertEquals(999, props.getInt("stringInvalid", 999))
        assertNull(props.getInt("stringInvalid"))

        // Non-number types with default
        assertEquals(999, props.getInt("boolean", 999))
        assertNull(props.getInt("boolean"))

        // Missing key with default
        assertEquals(999, props.getInt("missing", 999))
        assertNull(props.getInt("missing"))

        // Null value with default
        assertEquals(999, props.getInt("nullValue", 999))
        assertNull(props.getInt("nullValue"))
    }

    @Test
    fun testRequiredInt() {
        val props = FlexProps(mapOf("number" to 42))

        assertEquals(42, props.getRequiredInt("number"))

        try {
            props.getRequiredInt("missing")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Required property 'missing' is missing"))
        }
    }

    @Test
    fun testFloatAccessors() {
        val data = mapOf(
            "floatValue" to 3.14f,
            "doubleValue" to 2.718,
            "intValue" to 42,
            "stringFloat" to "1.23",
            "stringInvalid" to "not-a-number",
            "boolean" to true,
            "nullValue" to null
        )
        val props = FlexProps(data)

        // Direct float
        assertEquals(3.14f, props.getFloat("floatValue")!!, 0.001f)

        // Number conversions
        assertEquals(2.718f, props.getFloat("doubleValue")!!, 0.001f)
        assertEquals(42f, props.getFloat("intValue")!!, 0.001f)

        // String to float conversion
        assertEquals(1.23f, props.getFloat("stringFloat")!!, 0.001f)

        // Invalid string with default
        assertEquals(9.99f, props.getFloat("stringInvalid", 9.99f)!!, 0.001f)
        assertNull(props.getFloat("stringInvalid"))

        // Non-number types with default
        assertEquals(9.99f, props.getFloat("boolean", 9.99f)!!, 0.001f)
        assertNull(props.getFloat("boolean"))

        // Missing key with default
        assertEquals(9.99f, props.getFloat("missing", 9.99f)!!, 0.001f)
        assertNull(props.getFloat("missing"))

        // Null value with default
        assertEquals(9.99f, props.getFloat("nullValue", 9.99f)!!, 0.001f)
        assertNull(props.getFloat("nullValue"))
    }

    @Test
    fun testRequiredFloat() {
        val props = FlexProps(mapOf("number" to 3.14f))

        assertEquals(3.14f, props.getRequiredFloat("number"), 0.001f)

        try {
            props.getRequiredFloat("missing")
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Required property 'missing' is missing"))
        }
    }

    @Test
    fun testBooleanAccessors() {
        val data = mapOf(
            "trueValue" to true,
            "falseValue" to false,
            "stringTrue" to "true",
            "stringFalse" to "false",
            "stringInvalid" to "yes",
            "number" to 1,
            "nullValue" to null
        )
        val props = FlexProps(data)

        // Direct boolean values
        assertTrue(props.getBoolean("trueValue"))
        assertFalse(props.getBoolean("falseValue"))

        // String to boolean conversion
        assertTrue(props.getBoolean("stringTrue"))
        assertFalse(props.getBoolean("stringFalse"))

        // Invalid string uses default
        assertTrue(props.getBoolean("stringInvalid", true))
        assertFalse(props.getBoolean("stringInvalid")) // default is false

        // Non-boolean types use default
        assertTrue(props.getBoolean("number", true))
        assertFalse(props.getBoolean("number")) // default is false

        // Missing key uses default
        assertTrue(props.getBoolean("missing", true))
        assertFalse(props.getBoolean("missing")) // default is false

        // Null value uses default
        assertTrue(props.getBoolean("nullValue", true))
        assertFalse(props.getBoolean("nullValue")) // default is false
    }

    @Test
    fun testListAccessors() {
        val data = mapOf(
            "stringList" to listOf("a", "b", "c"),
            "mixedList" to listOf("text", 123, true),
            "emptyList" to emptyList<Any>(),
            "notList" to "not a list",
            "nullValue" to null
        )
        val props = FlexProps(data)

        // Valid list
        val stringList = props.getList("stringList")
        assertNotNull(stringList)
        assertEquals(3, stringList!!.size)
        assertEquals("a", stringList[0])

        // Mixed list
        val mixedList = props.getList("mixedList")
        assertNotNull(mixedList)
        assertEquals(3, mixedList!!.size)
        assertEquals("text", mixedList[0])
        assertEquals(123, mixedList[1])
        assertEquals(true, mixedList[2])

        // Empty list
        val emptyList = props.getList("emptyList")
        assertNotNull(emptyList)
        assertEquals(0, emptyList!!.size)

        // Not a list
        assertNull(props.getList("notList"))

        // Missing key
        assertNull(props.getList("missing"))

        // Null value
        assertNull(props.getList("nullValue"))
    }

    @Test
    fun testStringListAccessor() {
        val data = mapOf(
            "stringList" to listOf("a", "b", "c"),
            "mixedList" to listOf("text", 123, true, null),
            "emptyList" to emptyList<Any>(),
            "notList" to "not a list"
        )
        val props = FlexProps(data)

        // Pure string list
        val stringList = props.getStringList("stringList")
        assertNotNull(stringList)
        assertEquals(3, stringList!!.size)
        assertEquals(listOf("a", "b", "c"), stringList)

        // Mixed list - filters to only strings
        val mixedList = props.getStringList("mixedList")
        assertNotNull(mixedList)
        assertEquals(1, mixedList!!.size)
        assertEquals(listOf("text"), mixedList)

        // Empty list
        val emptyList = props.getStringList("emptyList")
        assertNotNull(emptyList)
        assertEquals(0, emptyList!!.size)

        // Not a list
        assertNull(props.getStringList("notList"))
    }

    @Test
    fun testMapAccessors() {
        val nestedMap = mapOf("key1" to "value1", "key2" to 42)
        val data = mapOf(
            "mapValue" to nestedMap,
            "emptyMap" to emptyMap<String, Any>(),
            "notMap" to "not a map",
            "nullValue" to null
        )
        val props = FlexProps(data)

        // Valid map
        val map = props.getMap("mapValue")
        assertNotNull(map)
        assertEquals("value1", map!!["key1"])
        assertEquals(42, map["key2"])

        // Empty map
        val emptyMap = props.getMap("emptyMap")
        assertNotNull(emptyMap)
        assertTrue(emptyMap!!.isEmpty())

        // Not a map
        assertNull(props.getMap("notMap"))

        // Missing key
        assertNull(props.getMap("missing"))

        // Null value
        assertNull(props.getMap("nullValue"))
    }

    @Test
    fun testNestedPropsAccessor() {
        val nestedData = mapOf(
            "title" to "Nested Title",
            "count" to 5,
            "enabled" to true
        )
        val data = mapOf(
            "nested" to nestedData,
            "emptyMap" to emptyMap<String, Any>(),
            "notMap" to "not a map"
        )
        val props = FlexProps(data)

        // Valid nested props
        val nestedProps = props.getProps("nested")
        assertNotNull(nestedProps)
        assertEquals("Nested Title", nestedProps!!.getString("title"))
        assertEquals(5, nestedProps.getInt("count"))
        assertTrue(nestedProps.getBoolean("enabled"))

        // Empty map to props
        val emptyProps = props.getProps("emptyMap")
        assertNotNull(emptyProps)
        assertTrue(emptyProps!!.isEmpty())

        // Not a map
        assertNull(props.getProps("notMap"))

        // Missing key
        assertNull(props.getProps("missing"))
    }

    @Test
    fun testUtilityMethods() {
        val data = mapOf(
            "text" to "hello",
            "number" to 42,
            "boolean" to true,
            "list" to listOf("a", "b"),
            "map" to mapOf("key" to "value")
        )
        val props = FlexProps(data)

        // Has method
        assertTrue(props.has("text"))
        assertTrue(props.has("number"))
        assertFalse(props.has("missing"))

        // Keys method
        val keys = props.keys()
        assertEquals(5, keys.size)
        assertTrue(keys.contains("text"))
        assertTrue(keys.contains("number"))
        assertTrue(keys.contains("boolean"))
        assertTrue(keys.contains("list"))
        assertTrue(keys.contains("map"))

        // Raw access
        assertEquals("hello", props.getRaw("text"))
        assertEquals(42, props.getRaw("number"))
        assertEquals(true, props.getRaw("boolean"))
        assertNull(props.getRaw("missing"))

        // To map
        val mapCopy = props.toMap()
        assertEquals(data, mapCopy)
        assertNotSame(data, mapCopy) // Should be a copy

        // Size and empty
        assertEquals(5, props.size())
        assertFalse(props.isEmpty())

        val emptyProps = FlexProps(emptyMap())
        assertEquals(0, emptyProps.size())
        assertTrue(emptyProps.isEmpty())
    }

    @Test
    fun testFactoryMethods() {
        // Empty factory
        val emptyProps = FlexProps.empty()
        assertTrue(emptyProps.isEmpty())
        assertEquals(0, emptyProps.size())

        // Of factory with pairs
        val props = FlexProps.of(
            "text" to "hello",
            "number" to 42,
            "boolean" to true
        )

        assertEquals("hello", props.getString("text"))
        assertEquals(42, props.getInt("number"))
        assertTrue(props.getBoolean("boolean"))
        assertEquals(3, props.size())
    }

    @Test
    fun testComplexDataStructures() {
        val complexData = mapOf(
            "user" to mapOf(
                "profile" to mapOf(
                    "name" to "John Doe",
                    "settings" to mapOf(
                        "theme" to "dark",
                        "notifications" to true
                    )
                ),
                "scores" to listOf(85, 92, 78),
                "tags" to listOf("premium", "verified")
            ),
            "metadata" to mapOf(
                "version" to "1.0",
                "timestamp" to 1234567890L
            )
        )
        val props = FlexProps(complexData)

        // Access nested data via nested props
        val userProps = props.getProps("user")
        assertNotNull(userProps)

        val profileProps = userProps!!.getProps("profile")
        assertNotNull(profileProps)
        assertEquals("John Doe", profileProps!!.getString("name"))

        val settingsProps = profileProps.getProps("settings")
        assertNotNull(settingsProps)
        assertEquals("dark", settingsProps!!.getString("theme"))
        assertTrue(settingsProps.getBoolean("notifications"))

        // Access lists
        val scores = userProps.getList("scores")
        assertNotNull(scores)
        assertEquals(3, scores!!.size)
        assertEquals(85, scores[0])

        val tags = userProps.getStringList("tags")
        assertNotNull(tags)
        assertEquals(2, tags!!.size)
        assertEquals("premium", tags[0])
        assertEquals("verified", tags[1])

        // Access metadata
        val metadataProps = props.getProps("metadata")
        assertNotNull(metadataProps)
        assertEquals("1.0", metadataProps!!.getString("version"))
        assertEquals(1234567890L, metadataProps.getRaw("timestamp"))
    }

    @Test
    fun testEdgeCases() {
        val edgeData = mapOf(
            "zeroInt" to 0,
            "zeroFloat" to 0.0f,
            "emptyString" to "",
            "falseBoolean" to false,
            "negativeInt" to -42,
            "negativeFloat" to -3.14f,
            "largeNumber" to Long.MAX_VALUE,
            "specialChars" to "!@#$%^&*()_+{}|:<>?[]\\;'\",./"
        )
        val props = FlexProps(edgeData)

        // Zero values
        assertEquals(0, props.getInt("zeroInt"))
        assertEquals(0.0f, props.getFloat("zeroFloat")!!, 0.001f)
        assertEquals("", props.getString("emptyString"))
        assertFalse(props.getBoolean("falseBoolean"))

        // Negative values
        assertEquals(-42, props.getInt("negativeInt"))
        assertEquals(-3.14f, props.getFloat("negativeFloat")!!, 0.001f)

        // Large numbers
        assertEquals(Long.MAX_VALUE, props.getRaw("largeNumber"))

        // Special characters
        assertEquals("!@#$%^&*()_+{}|:<>?[]\\;'\",./ ", props.getString("specialChars"))
    }

    @Test
    fun testTypeConversionLimits() {
        val data = mapOf(
            "doubleMax" to Double.MAX_VALUE,
            "doubleMin" to Double.MIN_VALUE,
            "longMax" to Long.MAX_VALUE,
            "longMin" to Long.MIN_VALUE
        )
        val props = FlexProps(data)

        // These conversions might lose precision but should not throw
        assertNotNull(props.getInt("longMax"))
        assertNotNull(props.getFloat("doubleMax"))

        // Edge case string conversions
        val stringData = mapOf(
            "intOverflow" to "999999999999999999999999999999",
            "floatOverflow" to "9.9999e999",
            "intScientific" to "1e5",
            "floatScientific" to "1.23e-4"
        )
        val stringProps = FlexProps(stringData)

        // Should handle overflow gracefully by returning null
        assertNull(stringProps.getInt("intOverflow"))
        assertNull(stringProps.getFloat("floatOverflow"))

        // Scientific notation
        assertEquals(100000, stringProps.getInt("intScientific"))
        assertEquals(0.000123f, stringProps.getFloat("floatScientific")!!, 0.0000001f)
    }
}