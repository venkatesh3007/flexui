package dev.flexui.schema

import dev.flexui.theme.ThemeResolver
import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a UI component node in the FlexUI tree.
 * This is the core model that gets parsed from JSON configs.
 */
data class FlexNode(
    val type: String,
    val id: String? = null,
    val style: Map<String, Any>? = null,
    val children: List<FlexNode>? = null,
    val visibility: String = "visible",
    val action: FlexAction? = null,
    val condition: FlexCondition? = null,
    val props: Map<String, Any>? = null
) {
    /**
     * Get a resolved style value considering theme variables
     */
    fun resolvedStyle(theme: FlexTheme): ResolvedStyle {
        return ResolvedStyle.from(style, theme)
    }
    
    /**
     * Get props accessor for this node
     */
    fun getProps(): FlexProps {
        return FlexProps(props ?: emptyMap())
    }
    
    /**
     * Check if this node should be visible given the current context
     */
    fun shouldRender(data: Map<String, Any>, theme: FlexTheme): Boolean {
        if (visibility == "gone" || visibility == "hidden") return false
        
        condition?.let { condition ->
            return condition.evaluate(data, theme)
        }
        
        return true
    }
    
    companion object {
        @JvmStatic
        fun fromJson(json: JSONObject): FlexNode {
            val type = json.getString("type")
            val id = json.optString("id", null)
            val visibility = json.optString("visibility", "visible")
            
            val style = json.optJSONObject("style")?.let { styleJson ->
                jsonObjectToMap(styleJson)
            }
            
            val props = json.optJSONObject("props")?.let { propsJson ->
                jsonObjectToMap(propsJson)
            }
            
            val children = json.optJSONArray("children")?.let { childrenArray ->
                (0 until childrenArray.length()).map { i ->
                    fromJson(childrenArray.getJSONObject(i))
                }
            }
            
            val action = json.optJSONObject("action")?.let { actionJson ->
                FlexAction.fromJson(actionJson)
            }
            
            val condition = json.optJSONObject("condition")?.let { conditionJson ->
                FlexCondition.fromJson(conditionJson)
            }
            
            return FlexNode(
                type = type,
                id = id,
                style = style,
                children = children,
                visibility = visibility,
                action = action,
                condition = condition,
                props = props
            )
        }
        
        private fun jsonObjectToMap(json: JSONObject): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = when (val jsonValue = json.get(key)) {
                    is JSONObject -> jsonObjectToMap(jsonValue)
                    is JSONArray -> jsonArrayToList(jsonValue)
                    else -> jsonValue
                }
                map[key] = value
            }
            return map
        }
        
        private fun jsonArrayToList(json: JSONArray): List<Any> {
            return (0 until json.length()).map { i ->
                when (val jsonValue = json.get(i)) {
                    is JSONObject -> jsonObjectToMap(jsonValue)
                    is JSONArray -> jsonArrayToList(jsonValue)
                    else -> jsonValue
                }
            }
        }
    }
}

/**
 * Resolved style properties with theme variables substituted
 */
data class ResolvedStyle(
    val width: Int? = null,
    val height: Int? = null,
    val paddingTop: Int = 0,
    val paddingBottom: Int = 0,
    val paddingLeft: Int = 0,
    val paddingRight: Int = 0,
    val marginTop: Int = 0,
    val marginBottom: Int = 0,
    val marginLeft: Int = 0,
    val marginRight: Int = 0,
    val backgroundColor: Int = 0x00000000,
    val borderColor: Int = 0x00000000,
    val borderWidth: Int = 0,
    val borderRadius: Int = 0,
    val elevation: Int = 0,
    val textColor: Int = 0xFF000000.toInt(),
    val fontSize: Float = 14f,
    val fontWeight: String = "normal",
    val textAlignment: String = "left",
    val maxLines: Int = Int.MAX_VALUE,
    val spacing: Int = 0,
    val alignment: String = "start"
) {
    companion object {
        fun from(styleMap: Map<String, Any>?, theme: FlexTheme): ResolvedStyle {
            if (styleMap == null) return ResolvedStyle()
            
            val resolver = ThemeResolver(theme)
            
            return ResolvedStyle(
                width = styleMap["width"]?.let { parseSize(it) },
                height = styleMap["height"]?.let { parseSize(it) },
                paddingTop = parsePadding(styleMap, "paddingTop", "padding", resolver),
                paddingBottom = parsePadding(styleMap, "paddingBottom", "padding", resolver),
                paddingLeft = parsePadding(styleMap, "paddingLeft", "padding", resolver),
                paddingRight = parsePadding(styleMap, "paddingRight", "padding", resolver),
                marginTop = parseMargin(styleMap, "marginTop", "margin", resolver),
                marginBottom = parseMargin(styleMap, "marginBottom", "margin", resolver),
                marginLeft = parseMargin(styleMap, "marginLeft", "margin", resolver),
                marginRight = parseMargin(styleMap, "marginRight", "margin", resolver),
                backgroundColor = parseColor(styleMap["backgroundColor"], resolver),
                borderColor = parseColor(styleMap["borderColor"], resolver),
                borderWidth = parseInt(styleMap["borderWidth"], resolver),
                borderRadius = parseInt(styleMap["borderRadius"], resolver),
                elevation = parseInt(styleMap["elevation"], resolver),
                textColor = parseColor(styleMap["color"] ?: styleMap["textColor"], resolver),
                fontSize = parseFloat(styleMap["fontSize"], resolver, 14f),
                fontWeight = parseString(styleMap["fontWeight"], resolver, "normal"),
                textAlignment = parseString(styleMap["textAlign"], resolver, "left"),
                maxLines = parseInt(styleMap["maxLines"], resolver, Int.MAX_VALUE),
                spacing = parseInt(styleMap["spacing"], resolver),
                alignment = parseString(styleMap["alignment"], resolver, "start")
            )
        }
        
        private fun parseSize(value: Any?): Int? {
            return when (value) {
                is Number -> value.toInt()
                is String -> {
                    when (value) {
                        "match_parent", "fill_parent" -> -1
                        "wrap_content" -> -2
                        else -> value.toIntOrNull()
                    }
                }
                else -> null
            }
        }
        
        private fun parsePadding(styleMap: Map<String, Any>, specific: String, general: String, resolver: ThemeResolver): Int {
            return parseInt(styleMap[specific] ?: styleMap[general], resolver)
        }
        
        private fun parseMargin(styleMap: Map<String, Any>, specific: String, general: String, resolver: ThemeResolver): Int {
            return parseInt(styleMap[specific] ?: styleMap[general], resolver)
        }
        
        private fun parseInt(value: Any?, resolver: ThemeResolver, default: Int = 0): Int {
            return when (value) {
                is Number -> value.toInt()
                is String -> resolver.resolveInt(value) ?: value.toIntOrNull() ?: default
                else -> default
            }
        }
        
        private fun parseFloat(value: Any?, resolver: ThemeResolver, default: Float = 0f): Float {
            return when (value) {
                is Number -> value.toFloat()
                is String -> resolver.resolveFloat(value) ?: value.toFloatOrNull() ?: default
                else -> default
            }
        }
        
        private fun parseString(value: Any?, resolver: ThemeResolver, default: String = ""): String {
            return when (value) {
                is String -> resolver.resolveString(value) ?: value
                else -> default
            }
        }
        
        private fun parseColor(value: Any?, resolver: ThemeResolver): Int {
            return when (value) {
                is Number -> value.toInt()
                is String -> resolver.resolveColor(value) ?: parseHexColor(value)
                else -> 0x00000000
            }
        }
        
        private fun parseHexColor(hex: String): Int {
            return try {
                if (hex.startsWith("#")) {
                    android.graphics.Color.parseColor(hex)
                } else {
                    android.graphics.Color.parseColor("#$hex")
                }
            } catch (e: Exception) {
                0x00000000
            }
        }
    }
}