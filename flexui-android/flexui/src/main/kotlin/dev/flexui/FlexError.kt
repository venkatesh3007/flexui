package dev.flexui

/**
 * Represents an error that occurred during FlexUI operations.
 */
data class FlexError(
    val message: String,
    val code: ErrorCode,
    val cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Error codes for different types of FlexUI errors
     */
    enum class ErrorCode {
        /** JSON parsing failed */
        PARSE_ERROR,
        
        /** Network request failed */
        NETWORK_ERROR,
        
        /** Configuration validation failed */
        VALIDATION_ERROR,
        
        /** Component rendering failed */
        RENDER_ERROR,
        
        /** Unknown component type */
        UNKNOWN_COMPONENT,
        
        /** Action handling failed */
        ACTION_ERROR,
        
        /** Theme resolution failed */
        THEME_ERROR,
        
        /** Cache operation failed */
        CACHE_ERROR,
        
        /** Unknown error */
        UNKNOWN_ERROR
    }
    
    companion object {
        /**
         * Create a parse error
         */
        @JvmStatic
        fun parseError(message: String, cause: Throwable? = null): FlexError {
            return FlexError(message, ErrorCode.PARSE_ERROR, cause)
        }
        
        /**
         * Create a network error
         */
        @JvmStatic
        fun networkError(message: String, cause: Throwable? = null): FlexError {
            return FlexError(message, ErrorCode.NETWORK_ERROR, cause)
        }
        
        /**
         * Create a validation error
         */
        @JvmStatic
        fun validationError(message: String, cause: Throwable? = null): FlexError {
            return FlexError(message, ErrorCode.VALIDATION_ERROR, cause)
        }
        
        /**
         * Create a render error
         */
        @JvmStatic
        fun renderError(message: String, cause: Throwable? = null): FlexError {
            return FlexError(message, ErrorCode.RENDER_ERROR, cause)
        }
        
        /**
         * Create an unknown component error
         */
        @JvmStatic
        fun unknownComponent(type: String): FlexError {
            return FlexError("Unknown component type: $type", ErrorCode.UNKNOWN_COMPONENT)
        }
        
        /**
         * Create an action error
         */
        @JvmStatic
        fun actionError(message: String, cause: Throwable? = null): FlexError {
            return FlexError(message, ErrorCode.ACTION_ERROR, cause)
        }
    }
}