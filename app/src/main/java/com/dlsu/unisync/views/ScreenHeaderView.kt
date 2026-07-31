package com.dlsu.unisync.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.dlsu.unisync.R
import com.dlsu.unisync.databinding.ViewScreenHeaderBinding

// One title/subtitle treatment for every screen, so spacing, type, and the
// TalkBack heading flag stay identical instead of being retyped per layout.
// The subtitle hides itself when no text is supplied.
class ScreenHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewScreenHeaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        context.withStyledAttributes(attrs, R.styleable.ScreenHeaderView) {
            title = getString(R.styleable.ScreenHeaderView_headerTitle)
            subtitle = getString(R.styleable.ScreenHeaderView_headerSubtitle)
        }
    }

    var title: CharSequence?
        get() = binding.headerTitle.text
        set(value) {
            binding.headerTitle.text = value
        }

    var subtitle: CharSequence?
        get() = binding.headerSubtitle.text
        set(value) {
            binding.headerSubtitle.text = value
            binding.headerSubtitle.isVisible = !value.isNullOrBlank()
        }
}
