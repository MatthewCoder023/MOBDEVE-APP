package com.dlsu.unisync.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.dlsu.unisync.R
import com.dlsu.unisync.models.CampusLocation
import com.dlsu.unisync.models.StatusLevel
import kotlin.math.roundToInt

// Draws the campus illustration and turns its building blocks into tap targets.
// Building bounds come from the drawable's 320x220 viewport, so the view scales
// them by its own width and hit-tests in that same space.
class CampusMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mapDrawable = AppCompatResources.getDrawable(context, R.drawable.img_campus_map)

    private val highlightFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.map_highlight)
        alpha = FILL_ALPHA
    }
    private val highlightStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.map_highlight)
    }
    private val activityFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // The blocks in the illustration range from dark green to pale mint, and a
    // busy one is amber or red, so no single text colour is readable on all of
    // them. White with a soft dark shadow is legible on every one.
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.manrope_bold)
    }

    var locations: List<CampusLocation> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    var selected: CampusLocation? = null
        set(value) {
            if (field == value) return
            field = value
            contentDescription = value?.name ?: context.getString(R.string.map_image_description)
            invalidate()
        }

    // Building name -> how busy it is right now. Only buildings worth marking
    // appear here; a quiet building is the normal case and is left alone.
    var busyLevels: Map<String, StatusLevel> = emptyMap()
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var onLocationSelected: ((CampusLocation) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
        contentDescription = context.getString(R.string.map_image_description)
    }

    // Lock to the illustration's aspect ratio so viewport units map linearly to
    // pixels; anything else would need letterbox handling in the hit test.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width * VIEWPORT_HEIGHT / VIEWPORT_WIDTH).roundToInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mapDrawable?.setBounds(0, 0, width, height)
        mapDrawable?.draw(canvas)

        val scale = width / VIEWPORT_WIDTH
        if (scale <= 0f) return
        val radius = CORNER_RADIUS_VIEWPORT * scale

        // Crowd first, selection on top: the white selection ring has to stay
        // readable over a building that is also marked busy.
        locations.forEach { location ->
            val level = busyLevels[location.name] ?: return@forEach
            activityFill.color = ContextCompat.getColor(context, colorFor(level))
            activityFill.alpha = ACTIVITY_ALPHA
            canvas.drawRoundRect(location.scaledBy(scale), radius, radius, activityFill)
        }

        selected?.let { location ->
            highlightStroke.strokeWidth = STROKE_WIDTH_VIEWPORT * scale
            val bounds = location.scaledBy(scale)
            canvas.drawRoundRect(bounds, radius, radius, highlightFill)
            canvas.drawRoundRect(bounds, radius, radius, highlightStroke)
        }

        // Labels last so they stay readable over the crowd tint and the
        // selection ring. Without them a building is an anonymous green block
        // until you tap it.
        labelPaint.setShadowLayer(SHADOW_RADIUS_VIEWPORT * scale, 0f, 0f, SHADOW_COLOR)
        locations.forEach { location ->
            val bounds = location.scaledBy(scale)
            labelPaint.textSize = textSizeFitting(location.shortName, bounds.width(), scale)
            // Centre on the block's middle rather than its baseline.
            val baseline = bounds.centerY() - (labelPaint.descent() + labelPaint.ascent()) / 2f
            canvas.drawText(location.shortName, bounds.centerX(), baseline, labelPaint)
        }
    }

    // Shrinks the label until it fits its building, so a long name on a narrow
    // block stays inside it instead of bleeding across the map.
    private fun textSizeFitting(text: String, blockWidth: Float, scale: Float): Float {
        val available = blockWidth - LABEL_PADDING_VIEWPORT * 2 * scale
        var size = LABEL_SIZE_VIEWPORT * scale
        val minimum = LABEL_MIN_SIZE_VIEWPORT * scale
        while (size > minimum) {
            labelPaint.textSize = size
            if (labelPaint.measureText(text) <= available) break
            size -= 1f
        }
        return size
    }

    private fun CampusLocation.scaledBy(scale: Float) =
        RectF(left * scale, top * scale, right * scale, bottom * scale)

    private fun colorFor(level: StatusLevel) = when (level) {
        StatusLevel.HIGH -> R.color.status_high
        StatusLevel.MEDIUM -> R.color.status_medium
        StatusLevel.LOW -> R.color.status_low
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) return super.onTouchEvent(event)
        val scale = width / VIEWPORT_WIDTH
        if (scale <= 0f) return super.onTouchEvent(event)

        val hit = locations.firstOrNull { it.contains(event.x / scale, event.y / scale) }
            ?: return super.onTouchEvent(event)
        selected = hit
        onLocationSelected?.invoke(hit)
        performClick()
        return true
    }

    // Overridden so the tap handling above still reports a click for
    // accessibility services and haptics.
    override fun performClick(): Boolean = super.performClick()

    private companion object {
        const val VIEWPORT_WIDTH = 320f
        const val VIEWPORT_HEIGHT = 220f
        const val CORNER_RADIUS_VIEWPORT = 6f
        const val STROKE_WIDTH_VIEWPORT = 3f
        const val FILL_ALPHA = 64

        // Nearly opaque on purpose. At lower alpha the amber and red composite
        // with the green block underneath and come out olive and brown, which
        // reads as a different illustration rather than a status.
        const val ACTIVITY_ALPHA = 235
        const val LABEL_SIZE_VIEWPORT = 10f
        const val LABEL_MIN_SIZE_VIEWPORT = 6f
        const val LABEL_PADDING_VIEWPORT = 4f
        const val SHADOW_RADIUS_VIEWPORT = 2f
        const val SHADOW_COLOR = 0x99000000.toInt()
    }
}
