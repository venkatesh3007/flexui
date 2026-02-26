package dev.flexui.render.components

import android.content.Context
import android.view.View
import android.widget.Switch
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme

/**
 * Renders toggle components as Switch
 */
class ToggleRenderer : FlexComponentFactory {
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val switch = Switch(context)
        
        // Apply toggle-specific properties
        applyToggleProperties(switch, props, theme)
        
        return switch
    }
    
    private fun applyToggleProperties(switch: Switch, props: FlexProps, theme: FlexTheme) {
        // Set initial state
        val checked = props.getBoolean("checked", false)
        switch.isChecked = checked
        
        // Set enabled state
        val enabled = props.getBoolean("enabled", true)
        switch.isEnabled = enabled
        
        // Set text (for switches that show text)
        val text = props.getString("text")
        if (text != null) {
            switch.text = text
        }
        
        // Set text on/off labels
        val textOn = props.getString("textOn")
        val textOff = props.getString("textOff")
        if (textOn != null && textOff != null) {
            switch.textOn = textOn
            switch.textOff = textOff
        }
        
        // Set thumb and track colors if available
        theme.getColorInt("primary")?.let { primaryColor ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                switch.thumbTintList = android.content.res.ColorStateList.valueOf(primaryColor)
                switch.trackTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.argb(
                        128,
                        android.graphics.Color.red(primaryColor),
                        android.graphics.Color.green(primaryColor),
                        android.graphics.Color.blue(primaryColor)
                    )
                )
            }
        }
    }
}