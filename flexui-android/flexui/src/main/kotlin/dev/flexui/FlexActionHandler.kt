package dev.flexui

import dev.flexui.schema.FlexAction

/**
 * Interface for handling FlexUI actions.
 * Implement this interface to handle user interactions and custom actions.
 */
fun interface FlexActionHandler {
    /**
     * Handle a FlexUI action.
     * 
     * @param action The action to handle
     */
    fun handle(action: FlexAction)
}