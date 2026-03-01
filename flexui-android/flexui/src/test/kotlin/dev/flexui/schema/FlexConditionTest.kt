package dev.flexui.schema

import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject

class FlexConditionTest {

    private val sampleData = mapOf(
        "user" to mapOf(
            "name" to "John Doe",
            "age" to 25,
            "isActive" to true,
            "score" to 85.5,
            "tags" to listOf("premium", "verified"),
            "profile" to mapOf(
                "city" to "New York",
                "country" to "USA"
            )
        ),
        "product" to mapOf(
            "price" to 99.99,
            "category" to "electronics",
            "inStock" to true,
            "reviews" to listOf(
                mapOf("rating" to 5, "comment" to "Excellent"),
                mapOf("rating" to 4, "comment" to "Good")
            )
        ),
        "emptyString" to "",
        "emptyList" to emptyList<Any>(),
        "emptyMap" to emptyMap<String, Any>(),
        "nullValue" to null
    )

    private val sampleTheme = FlexTheme.getDefault()

    @Test
    fun testEqualityOperators() {
        // String equality
        val condition1 = FlexCondition("{{user.name}}", "==", "John Doe")
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{user.name}}", "!=", "Jane Doe") 
        assertTrue(condition2.evaluate(sampleData, sampleTheme))

        val condition3 = FlexCondition("{{user.name}}", "==", "Jane Doe")
        assertFalse(condition3.evaluate(sampleData, sampleTheme))

        // Number equality
        val condition4 = FlexCondition("{{user.age}}", "==", 25)
        assertTrue(condition4.evaluate(sampleData, sampleTheme))

        // Boolean equality
        val condition5 = FlexCondition("{{user.isActive}}", "==", true)
        assertTrue(condition5.evaluate(sampleData, sampleTheme))

        // Alternative equals syntax
        val condition6 = FlexCondition("{{user.age}}", "=", 25)
        assertTrue(condition6.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testComparisonOperators() {
        // Greater than
        val condition1 = FlexCondition("{{user.age}}", ">", 20)
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{user.age}}", ">", 30)
        assertFalse(condition2.evaluate(sampleData, sampleTheme))

        // Greater than or equal
        val condition3 = FlexCondition("{{user.age}}", ">=", 25)
        assertTrue(condition3.evaluate(sampleData, sampleTheme))

        // Less than
        val condition4 = FlexCondition("{{user.age}}", "<", 30)
        assertTrue(condition4.evaluate(sampleData, sampleTheme))

        // Less than or equal
        val condition5 = FlexCondition("{{user.age}}", "<=", 25)
        assertTrue(condition5.evaluate(sampleData, sampleTheme))

        // Float comparison
        val condition6 = FlexCondition("{{user.score}}", ">", 80.0)
        assertTrue(condition6.evaluate(sampleData, sampleTheme))

        // String to number comparison
        val condition7 = FlexCondition("{{user.age}}", ">", "20")
        assertTrue(condition7.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testStringOperators() {
        // Contains
        val condition1 = FlexCondition("{{user.name}}", "contains", "John")
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{user.name}}", "contains", "Jane")
        assertFalse(condition2.evaluate(sampleData, sampleTheme))

        // Starts with
        val condition3 = FlexCondition("{{user.name}}", "startswith", "John")
        assertTrue(condition3.evaluate(sampleData, sampleTheme))

        val condition4 = FlexCondition("{{user.name}}", "startswith", "Doe")
        assertFalse(condition4.evaluate(sampleData, sampleTheme))

        // Ends with
        val condition5 = FlexCondition("{{user.name}}", "endswith", "Doe")
        assertTrue(condition5.evaluate(sampleData, sampleTheme))

        val condition6 = FlexCondition("{{user.name}}", "endswith", "John")
        assertFalse(condition6.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testExistenceOperators() {
        // Exists
        val condition1 = FlexCondition("{{user.name}}", "exists", true)
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{user.nonexistent}}", "exists", true)
        assertFalse(condition2.evaluate(sampleData, sampleTheme))

        // Empty
        val condition3 = FlexCondition("{{emptyString}}", "empty", true)
        assertTrue(condition3.evaluate(sampleData, sampleTheme))

        val condition4 = FlexCondition("{{emptyList}}", "empty", true)
        assertTrue(condition4.evaluate(sampleData, sampleTheme))

        val condition5 = FlexCondition("{{emptyMap}}", "empty", true)
        assertTrue(condition5.evaluate(sampleData, sampleTheme))

        val condition6 = FlexCondition("{{user.name}}", "empty", true)
        assertFalse(condition6.evaluate(sampleData, sampleTheme))

        // Not empty
        val condition7 = FlexCondition("{{user.name}}", "notempty", true)
        assertTrue(condition7.evaluate(sampleData, sampleTheme))

        val condition8 = FlexCondition("{{emptyString}}", "notempty", true)
        assertFalse(condition8.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testCollectionOperators() {
        // List contains
        val condition1 = FlexCondition("{{user.tags}}", "contains", "premium")
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{user.tags}}", "contains", "basic")
        assertFalse(condition2.evaluate(sampleData, sampleTheme))

        // Map contains key
        val condition3 = FlexCondition("{{user}}", "contains", "name")
        assertTrue(condition3.evaluate(sampleData, sampleTheme))

        val condition4 = FlexCondition("{{user}}", "contains", "email")
        assertFalse(condition4.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testNestedDataAccess() {
        // Deep nesting
        val condition1 = FlexCondition("{{user.profile.city}}", "==", "New York")
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{user.profile.country}}", "!=", "Canada")
        assertTrue(condition2.evaluate(sampleData, sampleTheme))

        // List access by index
        val condition3 = FlexCondition("{{user.tags.0}}", "==", "premium")
        assertTrue(condition3.evaluate(sampleData, sampleTheme))

        val condition4 = FlexCondition("{{user.tags.1}}", "==", "verified")
        assertTrue(condition4.evaluate(sampleData, sampleTheme))

        val condition5 = FlexCondition("{{user.tags.2}}", "exists", true)
        assertFalse(condition5.evaluate(sampleData, sampleTheme)) // Index out of bounds
    }

    @Test
    fun testThemeDataAccess() {
        // Access theme colors
        val condition1 = FlexCondition("{{theme.colors.primary}}", "==", "#FF6B00")
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        // Access theme spacing
        val condition2 = FlexCondition("{{theme.spacing.md}}", ">=", 16)
        assertTrue(condition2.evaluate(sampleData, sampleTheme))

        // Access theme typography
        val condition3 = FlexCondition("{{theme.typography.headingSize}}", ">", 20)
        assertTrue(condition3.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testAndConditions() {
        val mainCondition = FlexCondition(
            ifExpression = "{{user.age}}",
            operator = ">",
            value = 18,
            and = listOf(
                FlexCondition("{{user.isActive}}", "==", true),
                FlexCondition("{{user.score}}", ">=", 80)
            )
        )

        assertTrue(mainCondition.evaluate(sampleData, sampleTheme))

        // Failing AND condition
        val failingCondition = FlexCondition(
            ifExpression = "{{user.age}}",
            operator = ">",
            value = 18,
            and = listOf(
                FlexCondition("{{user.isActive}}", "==", false) // This will fail
            )
        )

        assertFalse(failingCondition.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testOrConditions() {
        val mainCondition = FlexCondition(
            ifExpression = "{{user.age}}",
            operator = "<",
            value = 18, // This will fail
            or = listOf(
                FlexCondition("{{user.isActive}}", "==", true), // This will pass
                FlexCondition("{{user.score}}", "<", 50) // This will fail
            )
        )

        assertTrue(mainCondition.evaluate(sampleData, sampleTheme))

        // All OR conditions fail
        val failingCondition = FlexCondition(
            ifExpression = "{{user.age}}",
            operator = "<",
            value = 18, // This will fail
            or = listOf(
                FlexCondition("{{user.isActive}}", "==", false), // This will fail
                FlexCondition("{{user.score}}", "<", 50) // This will fail
            )
        )

        assertFalse(failingCondition.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testComplexLogic() {
        // Main condition AND (sub-condition OR another-sub-condition)
        val complexCondition = FlexCondition(
            ifExpression = "{{user.isActive}}",
            operator = "==",
            value = true,
            and = listOf(
                FlexCondition(
                    ifExpression = "{{user.age}}",
                    operator = "<",
                    value = 20, // This will fail
                    or = listOf(
                        FlexCondition("{{user.score}}", ">", 80) // This will pass
                    )
                )
            )
        )

        assertTrue(complexCondition.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testFromJsonParsing() {
        val json = JSONObject("""
        {
            "if": "{{user.age}}",
            "operator": ">=",
            "value": 18,
            "and": [
                {
                    "if": "{{user.isActive}}",
                    "operator": "==",
                    "value": true
                }
            ],
            "or": [
                {
                    "if": "{{user.score}}",
                    "operator": ">",
                    "value": 90
                }
            ]
        }
        """.trimIndent())

        val condition = FlexCondition.fromJson(json)

        assertEquals("{{user.age}}", condition.ifExpression)
        assertEquals(">=", condition.operator)
        assertEquals(18, condition.value)
        assertEquals(1, condition.and?.size)
        assertEquals(1, condition.or?.size)

        assertTrue(condition.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testFactoryMethods() {
        // Equals
        val equalsCondition = FlexCondition.equals("user.name", "John Doe")
        assertEquals("{{user.name}}", equalsCondition.ifExpression)
        assertEquals("==", equalsCondition.operator)
        assertEquals("John Doe", equalsCondition.value)
        assertTrue(equalsCondition.evaluate(sampleData, sampleTheme))

        // Exists
        val existsCondition = FlexCondition.exists("user.name")
        assertEquals("{{user.name}}", existsCondition.ifExpression)
        assertEquals("exists", existsCondition.operator)
        assertEquals(true, existsCondition.value)
        assertTrue(existsCondition.evaluate(sampleData, sampleTheme))

        // Greater than
        val gtCondition = FlexCondition.greaterThan("user.age", 20)
        assertEquals("{{user.age}}", gtCondition.ifExpression)
        assertEquals(">", gtCondition.operator)
        assertEquals(20, gtCondition.value)
        assertTrue(gtCondition.evaluate(sampleData, sampleTheme))

        // Not empty
        val notEmptyCondition = FlexCondition.notEmpty("user.name")
        assertEquals("{{user.name}}", notEmptyCondition.ifExpression)
        assertEquals("notempty", notEmptyCondition.operator)
        assertEquals(true, notEmptyCondition.value)
        assertTrue(notEmptyCondition.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testInvalidOperators() {
        val condition = FlexCondition("{{user.age}}", "invalid_operator", 25)
        assertFalse(condition.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testNonExistentPaths() {
        val condition1 = FlexCondition("{{user.nonexistent}}", "==", "value")
        assertFalse(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("{{nonexistent.path}}", "exists", true)
        assertFalse(condition2.evaluate(sampleData, sampleTheme))

        val condition3 = FlexCondition("{{user.profile.nonexistent}}", "empty", true)
        assertTrue(condition3.evaluate(sampleData, sampleTheme)) // null is considered empty
    }

    @Test
    fun testCaseInsensitiveOperators() {
        val condition1 = FlexCondition("{{user.age}}", "GT", 20)
        assertFalse(condition1.evaluate(sampleData, sampleTheme)) // Should be case-sensitive

        val condition2 = FlexCondition("{{user.name}}", "CONTAINS", "John")
        assertFalse(condition2.evaluate(sampleData, sampleTheme)) // Should be case-sensitive
        
        // Test actual case insensitive behavior in operator processing
        val condition3 = FlexCondition("{{user.age}}", ">=", 20)
        assertTrue(condition3.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testDirectValueComparison() {
        // Test direct string comparison without data path
        val condition1 = FlexCondition("literal_value", "==", "literal_value")
        assertTrue(condition1.evaluate(sampleData, sampleTheme))

        val condition2 = FlexCondition("literal_value", "!=", "other_value")
        assertTrue(condition2.evaluate(sampleData, sampleTheme))

        val condition3 = FlexCondition("literal_value", "contains", "literal")
        assertTrue(condition3.evaluate(sampleData, sampleTheme))
    }

    @Test
    fun testEmptyDataAndTheme() {
        val emptyData = emptyMap<String, Any>()
        val emptyTheme = FlexTheme(
            colors = emptyMap(),
            typography = emptyMap(),
            spacing = emptyMap(),
            borderRadius = emptyMap()
        )

        val condition = FlexCondition("{{user.name}}", "exists", true)
        assertFalse(condition.evaluate(emptyData, emptyTheme))

        val themeCondition = FlexCondition("{{theme.colors.primary}}", "exists", true)
        assertFalse(themeCondition.evaluate(emptyData, emptyTheme))
    }
}