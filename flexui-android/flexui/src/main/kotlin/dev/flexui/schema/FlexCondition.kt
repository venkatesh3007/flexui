package dev.flexui.schema

import org.json.JSONObject

/**
 * Represents a conditional logic expression for component visibility
 */
data class FlexCondition(
    val ifExpression: String,
    val operator: String,
    val value: Any,
    val and: List<FlexCondition>? = null,
    val or: List<FlexCondition>? = null
) {
    
    /**
     * Evaluate this condition against the provided data and theme context
     */
    fun evaluate(data: Map<String, Any>, theme: FlexTheme): Boolean {
        val leftValue = resolveValue(ifExpression, data, theme)
        val rightValue = value
        
        val result = when (operator.lowercase()) {
            "=", "==" -> leftValue == rightValue
            "!=" -> leftValue != rightValue
            ">" -> compareNumbers(leftValue, rightValue) { a, b -> a > b }
            ">=" -> compareNumbers(leftValue, rightValue) { a, b -> a >= b }
            "<" -> compareNumbers(leftValue, rightValue) { a, b -> a < b }
            "<=" -> compareNumbers(leftValue, rightValue) { a, b -> a <= b }
            "contains" -> containsCheck(leftValue, rightValue)
            "startswith" -> startsWithCheck(leftValue, rightValue)
            "endswith" -> endsWithCheck(leftValue, rightValue)
            "exists" -> leftValue != null
            "empty" -> isEmptyCheck(leftValue)
            "notempty" -> !isEmptyCheck(leftValue)
            else -> false
        }
        
        // Apply AND conditions
        val andResult = and?.all { it.evaluate(data, theme) } ?: true
        
        // Apply OR conditions
        val orResult = or?.any { it.evaluate(data, theme) } ?: true
        
        return result && andResult && orResult
    }
    
    private fun resolveValue(expression: String, data: Map<String, Any>, theme: FlexTheme): Any? {
        if (expression.startsWith("{{") && expression.endsWith("}}")) {
            val path = expression.substring(2, expression.length - 2)
            return resolveDataPath(path, data, theme)
        }
        return expression
    }
    
    private fun resolveDataPath(path: String, data: Map<String, Any>, theme: FlexTheme): Any? {
        val parts = path.split(".")
        var current: Any? = when {
            path.startsWith("data.") -> data
            path.startsWith("theme.") -> mapOf(
                "colors" to theme.colors,
                "typography" to theme.typography,
                "spacing" to theme.spacing,
                "borderRadius" to theme.borderRadius
            )
            else -> data
        }
        
        val actualParts = if (path.startsWith("data.") || path.startsWith("theme.")) {
            parts.drop(1)
        } else {
            parts
        }
        
        for (part in actualParts) {
            current = when (current) {
                is Map<*, *> -> current[part]
                is List<*> -> {
                    val index = part.toIntOrNull()
                    if (index != null && index >= 0 && index < current.size) {
                        current[index]
                    } else null
                }
                else -> null
            }
            if (current == null) break
        }
        
        return current
    }
    
    private fun compareNumbers(left: Any?, right: Any?, comparison: (Double, Double) -> Boolean): Boolean {
        val leftNum = when (left) {
            is Number -> left.toDouble()
            is String -> left.toDoubleOrNull()
            else -> null
        }
        
        val rightNum = when (right) {
            is Number -> right.toDouble()
            is String -> right.toDoubleOrNull()
            else -> null
        }
        
        return if (leftNum != null && rightNum != null) {
            comparison(leftNum, rightNum)
        } else false
    }
    
    private fun containsCheck(left: Any?, right: Any?): Boolean {
        return when {
            left is String && right is String -> left.contains(right)
            left is List<*> -> left.contains(right)
            left is Map<*, *> -> left.containsKey(right)
            else -> false
        }
    }
    
    private fun startsWithCheck(left: Any?, right: Any?): Boolean {
        return if (left is String && right is String) {
            left.startsWith(right)
        } else false
    }
    
    private fun endsWithCheck(left: Any?, right: Any?): Boolean {
        return if (left is String && right is String) {
            left.endsWith(right)
        } else false
    }
    
    private fun isEmptyCheck(value: Any?): Boolean {
        return when (value) {
            null -> true
            is String -> value.isEmpty()
            is List<*> -> value.isEmpty()
            is Map<*, *> -> value.isEmpty()
            is Array<*> -> value.isEmpty()
            else -> false
        }
    }
    
    companion object {
        @JvmStatic
        fun fromJson(json: JSONObject): FlexCondition {
            val ifExpression = json.getString("if")
            val operator = json.getString("operator")
            val value = json.get("value")
            
            val andConditions = json.optJSONArray("and")?.let { andArray ->
                (0 until andArray.length()).map { i ->
                    fromJson(andArray.getJSONObject(i))
                }
            }
            
            val orConditions = json.optJSONArray("or")?.let { orArray ->
                (0 until orArray.length()).map { i ->
                    fromJson(orArray.getJSONObject(i))
                }
            }
            
            return FlexCondition(
                ifExpression = ifExpression,
                operator = operator,
                value = value,
                and = andConditions,
                or = orConditions
            )
        }
        
        /**
         * Create a simple equality condition
         */
        @JvmStatic
        fun equals(path: String, value: Any): FlexCondition {
            return FlexCondition(
                ifExpression = "{{$path}}",
                operator = "==",
                value = value
            )
        }
        
        /**
         * Create a simple existence condition
         */
        @JvmStatic
        fun exists(path: String): FlexCondition {
            return FlexCondition(
                ifExpression = "{{$path}}",
                operator = "exists",
                value = true
            )
        }
        
        /**
         * Create a simple greater than condition
         */
        @JvmStatic
        fun greaterThan(path: String, value: Number): FlexCondition {
            return FlexCondition(
                ifExpression = "{{$path}}",
                operator = ">",
                value = value
            )
        }
        
        /**
         * Create a simple not empty condition
         */
        @JvmStatic
        fun notEmpty(path: String): FlexCondition {
            return FlexCondition(
                ifExpression = "{{$path}}",
                operator = "notempty",
                value = true
            )
        }
    }
}