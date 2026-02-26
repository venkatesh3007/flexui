package dev.flexui

import android.view.View

/**
 * Callback interface for async rendering operations.
 */
interface FlexRenderCallback {
    /**
     * Called when rendering completes successfully.
     * 
     * @param view The rendered view
     */
    fun onSuccess(view: View)
    
    /**
     * Called when rendering fails.
     * 
     * @param error The error that occurred
     */
    fun onError(error: FlexError)
}