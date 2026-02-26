package dev.flexui.render.components

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders input components as EditText
 */
class InputRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val editText = EditText(context)
        
        // Apply input-specific properties
        applyInputProperties(editText, props, theme)
        
        return editText
    }
    
    private fun applyInputProperties(editText: EditText, props: FlexProps, theme: FlexTheme) {
        // Set placeholder/hint
        val placeholder = props.getString("placeholder") ?: props.getString("hint")
        if (placeholder != null) {
            editText.hint = placeholder
        }
        
        // Set input type
        val inputType = props.getString("inputType", "text")
        editText.inputType = when (inputType) {
            "text" -> InputType.TYPE_CLASS_TEXT
            "email" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            "password" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            "number" -> InputType.TYPE_CLASS_NUMBER
            "phone" -> InputType.TYPE_CLASS_PHONE
            "multiline" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            else -> InputType.TYPE_CLASS_TEXT
        }
        
        // Set default value
        val defaultValue = props.getString("defaultValue") ?: props.getString("value")
        if (defaultValue != null) {
            editText.setText(defaultValue)
        }
        
        // Set max length
        props.getInt("maxLength")?.let { maxLength ->
            editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
        }
        
        // Set single line
        val singleLine = props.getBoolean("singleLine", true)
        editText.isSingleLine = singleLine
        
        // Set enabled state
        val enabled = props.getBoolean("enabled", true)
        editText.isEnabled = enabled
        
        // Set read-only
        val readOnly = props.getBoolean("readOnly", false)
        editText.isClickable = !readOnly
        editText.isFocusable = !readOnly
        editText.isFocusableInTouchMode = !readOnly
        
        // Set text color
        theme.getColorInt("text")?.let { textColor ->
            editText.setTextColor(textColor)
        }
        
        // Set hint color
        theme.getColorInt("textSecondary")?.let { hintColor ->
            editText.setHintTextColor(hintColor)
        }
    }
}