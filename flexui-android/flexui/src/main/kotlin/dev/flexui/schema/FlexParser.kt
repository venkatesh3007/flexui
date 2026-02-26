package dev.flexui.schema

import org.json.JSONException
import org.json.JSONObject

/**
 * Parses FlexUI JSON configurations into typed objects
 */
object FlexParser {
    
    /**
     * Parse a complete FlexUI configuration from JSON
     */
    @JvmStatic
    fun parseConfig(jsonString: String): FlexConfig {
        try {
            val json = JSONObject(jsonString)
            return parseConfig(json)
        } catch (e: JSONException) {
            throw FlexParseException("Invalid JSON: ${e.message}", e)
        }
    }
    
    /**
     * Parse a FlexUI configuration from JSONObject
     */
    @JvmStatic
    fun parseConfig(json: JSONObject): FlexConfig {
        try {
            val version = json.optString("version", "1.0")
            val screenId = json.getString("screenId")
            
            val theme = json.optJSONObject("theme")?.let { themeJson ->
                FlexTheme.fromJson(themeJson)
            } ?: FlexTheme.getDefault()
            
            val root = json.getJSONObject("root")
            val rootNode = FlexNode.fromJson(root)
            
            val actions = json.optJSONObject("actions")?.let { actionsJson ->
                parseActions(actionsJson)
            } ?: emptyMap()
            
            return FlexConfig(
                version = version,
                screenId = screenId,
                theme = theme,
                root = rootNode,
                actions = actions
            )
        } catch (e: JSONException) {
            throw FlexParseException("Failed to parse FlexUI config: ${e.message}", e)
        }
    }
    
    /**
     * Parse node tree from JSON
     */
    @JvmStatic
    fun parseNode(jsonString: String): FlexNode {
        try {
            val json = JSONObject(jsonString)
            return FlexNode.fromJson(json)
        } catch (e: JSONException) {
            throw FlexParseException("Failed to parse FlexNode: ${e.message}", e)
        }
    }
    
    /**
     * Parse theme from JSON
     */
    @JvmStatic
    fun parseTheme(jsonString: String): FlexTheme {
        try {
            val json = JSONObject(jsonString)
            return FlexTheme.fromJson(json)
        } catch (e: JSONException) {
            throw FlexParseException("Failed to parse FlexTheme: ${e.message}", e)
        }
    }
    
    /**
     * Parse action from JSON
     */
    @JvmStatic
    fun parseAction(jsonString: String): FlexAction {
        try {
            val json = JSONObject(jsonString)
            return FlexAction.fromJson(json)
        } catch (e: JSONException) {
            throw FlexParseException("Failed to parse FlexAction: ${e.message}", e)
        }
    }
    
    /**
     * Parse actions map from JSON object
     */
    private fun parseActions(json: JSONObject): Map<String, FlexAction> {
        val actions = mutableMapOf<String, FlexAction>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val actionJson = json.getJSONObject(key)
            actions[key] = FlexAction.fromJson(actionJson)
        }
        return actions
    }
    
    /**
     * Validate a FlexUI configuration
     */
    @JvmStatic
    fun validateConfig(jsonString: String): ValidationResult {
        return try {
            val config = parseConfig(jsonString)
            val errors = mutableListOf<String>()
            
            // Validate required fields
            if (config.screenId.isBlank()) {
                errors.add("screenId is required")
            }
            
            // Validate root node
            val nodeValidation = validateNode(config.root)
            errors.addAll(nodeValidation.errors)
            
            ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors
            )
        } catch (e: FlexParseException) {
            ValidationResult(
                isValid = false,
                errors = listOf("Parse error: ${e.message}")
            )
        }
    }
    
    /**
     * Validate a node and its children
     */
    private fun validateNode(node: FlexNode, path: String = "root"): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate node type
        if (node.type.isBlank()) {
            errors.add("$path: node type is required")
        }
        
        // Validate children if present
        node.children?.forEachIndexed { index, child ->
            val childValidation = validateNode(child, "$path.children[$index]")
            errors.addAll(childValidation.errors)
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

/**
 * Represents a complete FlexUI configuration
 */
data class FlexConfig(
    val version: String,
    val screenId: String,
    val theme: FlexTheme,
    val root: FlexNode,
    val actions: Map<String, FlexAction> = emptyMap()
)

/**
 * Result of configuration validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

/**
 * Exception thrown when parsing FlexUI configuration fails
 */
class FlexParseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)