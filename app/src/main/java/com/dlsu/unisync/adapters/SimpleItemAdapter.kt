package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.databinding.ItemSimpleCardBinding
import com.dlsu.unisync.models.SimpleItem
import com.dlsu.unisync.models.StatusLevel

// Generic card adapter for simple two-line prototype lists. An optional leading
// icon gives each screen's list its own visual identity, and items carrying a
// percentage render a color-coded occupancy bar.
class SimpleItemAdapter(
    private val items: List<SimpleItem>,
    @DrawableRes private val iconRes: Int? = null
) : RecyclerView.Adapter<SimpleItemAdapter.SimpleItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val binding = ItemSimpleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimpleItemViewHolder(binding, iconRes)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SimpleItemViewHolder(
        private val binding: ItemSimpleCardBinding,
        @DrawableRes private val iconRes: Int?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimpleItem) {
            binding.itemTitle.text = item.title
            binding.itemSubtitle.text = item.subtitle

            val icon = item.icon ?: iconRes
            binding.itemIconContainer.isVisible = icon != null
            icon?.let { binding.itemIcon.setImageResource(it) }

            // The level tints the icon whether or not there is a bar to draw, so
            // lists without a percentage can still colour-code urgency.
            val context = binding.root.context
            val (colorRes, containerRes) = when (item.level) {
                StatusLevel.LOW -> R.color.status_low to R.color.status_low_container
                StatusLevel.MEDIUM -> R.color.status_medium to R.color.status_medium_container
                StatusLevel.HIGH -> R.color.status_high to R.color.status_high_container
                null -> R.color.brand_accent to R.color.brand_container
            }
            binding.itemIcon.imageTintList = ContextCompat.getColorStateList(context, colorRes)
            binding.itemIconContainer.backgroundTintList =
                ContextCompat.getColorStateList(context, containerRes)

            val progress = item.progressPercent
            binding.itemProgress.isVisible = progress != null
            if (progress != null) {
                binding.itemProgress.setProgressCompat(progress.coerceIn(0, 100), false)
                binding.itemProgress.setIndicatorColor(ContextCompat.getColor(context, colorRes))
                binding.itemProgress.trackColor = ContextCompat.getColor(context, containerRes)
            }
        }
    }
}
