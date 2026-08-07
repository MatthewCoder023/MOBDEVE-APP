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

        val location = selected ?: return
        highlightStroke.strokeWidth = STROKE_WIDTH_VIEWPORT * scale
        val bounds = location.scaledBy(scale)
        canvas.drawRoundRect(bounds, radius, radius, highlightFill)
        canvas.drawRoundRect(bounds, radius, radius, highlightStroke)
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
    }
}
