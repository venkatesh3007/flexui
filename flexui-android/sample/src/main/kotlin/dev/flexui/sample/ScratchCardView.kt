package dev.flexui.sample

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Interactive scratch card view — touch to scratch and reveal reward underneath.
 * Uses PorterDuff.Mode.CLEAR to erase the cover layer on touch.
 */
class ScratchCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Cover layer (what you scratch off)
    private var coverBitmap: Bitmap? = null
    private var coverCanvas: Canvas? = null

    // Paint for erasing (scratch effect)
    private val erasePaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 70f
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    // Paint for drawing cover
    private val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint for reward text
    private val rewardTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    // Scratch path
    private val scratchPath = Path()
    private var lastX = 0f
    private var lastY = 0f

    // State
    private var isRevealed = false
    private var scratchPercentage = 0f
    private val revealThreshold = 0.40f // Auto-reveal at 40%

    // Configurable properties
    var rewardText: String = "₹500"
        set(value) { field = value; invalidate() }

    var rewardLabel: String = "CASHBACK"
        set(value) { field = value; invalidate() }

    var topLabel: String = "YOU WON"
        set(value) { field = value; invalidate() }

    var coverColors: IntArray = intArrayOf(
        Color.parseColor("#C0C0C0"),
        Color.parseColor("#A8A8A8"),
        Color.parseColor("#909090")
    )
        set(value) { field = value; initCover(); invalidate() }

    var rewardTextColor: Int = Color.parseColor("#1A1A1A")
        set(value) { field = value; invalidate() }

    var rewardBgColor: Int = Color.parseColor("#FFFFFF")
        set(value) { field = value; invalidate() }

    var coverText: String = "SCRATCH HERE"
        set(value) { field = value; invalidate() }

    var onRevealed: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            initCover()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Fallback: if onSizeChanged didn't fire yet, try after layout
        post {
            if (coverBitmap == null && width > 0 && height > 0) {
                initCover()
                invalidate()
            }
        }
    }

    private fun initCover() {
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        coverBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        coverCanvas = Canvas(coverBitmap!!)

        // Draw gradient cover
        val gradient = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            coverColors,
            null,
            Shader.TileMode.CLAMP
        )
        coverPaint.shader = gradient
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        val cornerRadius = 24f
        coverCanvas?.drawRoundRect(rect, cornerRadius, cornerRadius, coverPaint)

        // Draw "SCRATCH HERE" text on cover
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f * resources.displayMetrics.density
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            letterSpacing = 0.15f
        }
        coverCanvas?.drawText(
            coverText,
            w / 2f,
            h / 2f + textPaint.textSize / 3f,
            textPaint
        )

        // Draw dotted border hint
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f * resources.displayMetrics.density
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        val inset = 12f * resources.displayMetrics.density
        coverCanvas?.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            cornerRadius - inset / 2,
            cornerRadius - inset / 2,
            borderPaint
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density

        // Draw reward background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bgPaint.color = rewardBgColor
        canvas.drawRoundRect(RectF(0f, 0f, w, h), 24f, 24f, bgPaint)

        // Draw subtle pattern on reward bg
        val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(8, 0, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val step = 20f * density
        var i = -h
        while (i < w + h) {
            canvas.drawLine(i, h, i + h, 0f, patternPaint)
            i += step
        }

        // Draw top label ("YOU WON")
        labelTextPaint.color = Color.parseColor("#757575")
        labelTextPaint.textSize = 13f * density
        labelTextPaint.letterSpacing = 0.2f
        canvas.drawText(topLabel, w / 2f, h * 0.28f, labelTextPaint)

        // Draw reward amount
        rewardTextPaint.color = rewardTextColor
        rewardTextPaint.textSize = 48f * density
        canvas.drawText(rewardText, w / 2f, h * 0.55f, rewardTextPaint)

        // Draw bottom label ("CASHBACK")
        labelTextPaint.color = Color.parseColor("#FF6B00")
        labelTextPaint.textSize = 16f * density
        labelTextPaint.letterSpacing = 0.3f
        canvas.drawText(rewardLabel, w / 2f, h * 0.72f, labelTextPaint)

        // Draw cover on top (gets erased by scratching)
        if (!isRevealed) {
            coverBitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRevealed) return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scratchPath.moveTo(x, y)
                lastX = x
                lastY = y
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                scratchPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                coverCanvas?.drawPath(scratchPath, erasePaint)
                lastX = x
                lastY = y
                invalidate()
                checkRevealThreshold()
                return true
            }
            MotionEvent.ACTION_UP -> {
                scratchPath.reset()
                parent?.requestDisallowInterceptTouchEvent(false)
                checkRevealThreshold()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun checkRevealThreshold() {
        val bitmap = coverBitmap ?: return
        // Sample pixels to estimate scratch percentage
        val totalPixels = 200 // sample size
        var clearedPixels = 0
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return

        for (i in 0 until totalPixels) {
            val px = (Math.random() * w).toInt().coerceIn(0, w - 1)
            val py = (Math.random() * h).toInt().coerceIn(0, h - 1)
            if (Color.alpha(bitmap.getPixel(px, py)) == 0) {
                clearedPixels++
            }
        }

        scratchPercentage = clearedPixels.toFloat() / totalPixels
        if (scratchPercentage >= revealThreshold && !isRevealed) {
            reveal()
        }
    }

    private fun reveal() {
        isRevealed = true
        coverBitmap?.eraseColor(Color.TRANSPARENT)
        invalidate()
        onRevealed?.invoke()
    }

    fun reset() {
        isRevealed = false
        scratchPercentage = 0f
        scratchPath.reset()
        initCover()
        invalidate()
    }
}
