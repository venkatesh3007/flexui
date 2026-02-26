package dev.flexui.render.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import dev.flexui.FlexComponentFactory
import dev.flexui.schema.FlexProps
import dev.flexui.schema.FlexTheme
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * Renders image components as ImageView with URL loading support
 */
class ImageRenderer : FlexComponentFactory {
    
    private val executor = Executors.newFixedThreadPool(4)
    
    override fun create(context: Context, props: FlexProps, theme: FlexTheme): View {
        val imageView = ImageView(context)
        
        // Apply image-specific properties
        applyImageProperties(context, imageView, props, theme)
        
        return imageView
    }
    
    private fun applyImageProperties(
        context: Context, 
        imageView: ImageView, 
        props: FlexProps, 
        theme: FlexTheme
    ) {
        // Set scale type
        val scaleType = props.getString("scaleType", "centerCrop")
        imageView.scaleType = when (scaleType) {
            "centerCrop" -> ImageView.ScaleType.CENTER_CROP
            "centerInside" -> ImageView.ScaleType.CENTER_INSIDE
            "fitCenter" -> ImageView.ScaleType.FIT_CENTER
            "fitXY" -> ImageView.ScaleType.FIT_XY
            "fitStart" -> ImageView.ScaleType.FIT_START
            "fitEnd" -> ImageView.ScaleType.FIT_END
            "center" -> ImageView.ScaleType.CENTER
            "matrix" -> ImageView.ScaleType.MATRIX
            else -> ImageView.ScaleType.CENTER_CROP
        }
        
        // Set aspect ratio if specified
        props.getFloat("aspectRatio")?.let { aspectRatio ->
            // Custom aspect ratio requires overriding onMeasure
            val originalLayoutParams = imageView.layoutParams
            imageView.layoutParams = object : android.view.ViewGroup.LayoutParams(
                originalLayoutParams?.width ?: MATCH_PARENT,
                originalLayoutParams?.height ?: WRAP_CONTENT
            ) {}
            
            // Set a custom view that maintains aspect ratio
            // This is a simplified implementation
        }
        
        // Set tint if specified
        props.getString("tint")?.let { tintColor ->
            try {
                val color = android.graphics.Color.parseColor(
                    if (tintColor.startsWith("#")) tintColor else "#$tintColor"
                )
                imageView.setColorFilter(color)
            } catch (e: Exception) {
                // Invalid color format, ignore
            }
        }
        
        // Set placeholder if specified
        val placeholder = props.getString("placeholder")
        if (placeholder != null) {
            loadPlaceholder(context, imageView, placeholder)
        }
        
        // Load image source
        val src = props.getString("src")
        if (src != null) {
            loadImageSource(context, imageView, src, placeholder)
        }
        
        // Set content description for accessibility
        val alt = props.getString("alt") ?: props.getString("contentDescription")
        if (alt != null) {
            imageView.contentDescription = alt
        }
        
        // Set crop to padding
        val cropToPadding = props.getBoolean("cropToPadding", false)
        imageView.cropToPadding = cropToPadding
        
        // Set adjustViewBounds
        val adjustViewBounds = props.getBoolean("adjustViewBounds", false)
        imageView.adjustViewBounds = adjustViewBounds
        
        // Set maximum dimensions
        props.getInt("maxWidth")?.let { maxWidth ->
            imageView.maxWidth = maxWidth
        }
        
        props.getInt("maxHeight")?.let { maxHeight ->
            imageView.maxHeight = maxHeight
        }
    }
    
    private fun loadPlaceholder(context: Context, imageView: ImageView, placeholder: String) {
        // Try to load as drawable resource
        val resourceId = context.resources.getIdentifier(
            placeholder.removePrefix("@drawable/"), 
            "drawable", 
            context.packageName
        )
        
        if (resourceId != 0) {
            imageView.setImageResource(resourceId)
        } else {
            // Try to load as asset or create a default placeholder
            createDefaultPlaceholder(context, imageView)
        }
    }
    
    private fun loadImageSource(
        context: Context, 
        imageView: ImageView, 
        src: String,
        placeholder: String?
    ) {
        when {
            src.startsWith("http://") || src.startsWith("https://") -> {
                loadImageFromUrl(imageView, src)
            }
            src.startsWith("@drawable/") -> {
                loadImageFromDrawable(context, imageView, src)
            }
            src.startsWith("file://") -> {
                loadImageFromFile(imageView, src)
            }
            src.startsWith("data:") -> {
                loadImageFromDataUri(imageView, src)
            }
            else -> {
                // Try as drawable resource
                loadImageFromDrawable(context, imageView, "@drawable/$src")
            }
        }
    }
    
    private fun loadImageFromUrl(imageView: ImageView, url: String) {
        // Load image in background thread
        thread {
            try {
                val bitmap = downloadBitmap(url)
                if (bitmap != null) {
                    // Set bitmap on main thread
                    imageView.post {
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                // Handle error - could show error placeholder
                imageView.post {
                    createErrorPlaceholder(imageView)
                }
            }
        }
    }
    
    private fun loadImageFromDrawable(context: Context, imageView: ImageView, src: String) {
        val resourceName = src.removePrefix("@drawable/")
        val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
        
        if (resourceId != 0) {
            imageView.setImageResource(resourceId)
        } else {
            createErrorPlaceholder(imageView)
        }
    }
    
    private fun loadImageFromFile(imageView: ImageView, src: String) {
        thread {
            try {
                val filePath = src.removePrefix("file://")
                val bitmap = BitmapFactory.decodeFile(filePath)
                if (bitmap != null) {
                    imageView.post {
                        imageView.setImageBitmap(bitmap)
                    }
                } else {
                    imageView.post {
                        createErrorPlaceholder(imageView)
                    }
                }
            } catch (e: Exception) {
                imageView.post {
                    createErrorPlaceholder(imageView)
                }
            }
        }
    }
    
    private fun loadImageFromDataUri(imageView: ImageView, dataUri: String) {
        thread {
            try {
                // Parse data URI (simplified implementation)
                if (dataUri.startsWith("data:image/")) {
                    val base64Data = dataUri.substring(dataUri.indexOf(",") + 1)
                    val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    
                    if (bitmap != null) {
                        imageView.post {
                            imageView.setImageBitmap(bitmap)
                        }
                    } else {
                        imageView.post {
                            createErrorPlaceholder(imageView)
                        }
                    }
                }
            } catch (e: Exception) {
                imageView.post {
                    createErrorPlaceholder(imageView)
                }
            }
        }
    }
    
    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            
            val inputStream: InputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createDefaultPlaceholder(context: Context, imageView: ImageView) {
        // Create a simple gray placeholder
        val placeholder = android.graphics.drawable.ColorDrawable(0xFFE0E0E0.toInt())
        imageView.setImageDrawable(placeholder)
    }
    
    private fun createErrorPlaceholder(imageView: ImageView) {
        // Create a simple red placeholder for errors
        val errorPlaceholder = android.graphics.drawable.ColorDrawable(0xFFFFCDD2.toInt())
        imageView.setImageDrawable(errorPlaceholder)
    }
}