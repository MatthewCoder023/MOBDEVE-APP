package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.databinding.ItemSimpleCardBinding
import com.dlsu.unisync.models.CampusLocation

// Campus buildings on the shared two-line card. Selection is mirrored with the
// map above, so tapping either surface highlights the same building — the list
// is also the accessible path to that selection.
class CampusLocationAdapter(
    private val onLocationClicked: (CampusLocation) -> Unit
) : ListAdapter<CampusLocation, CampusLocationAdapter.LocationViewHolder>(LocationDiffCallback) {

    var selected: CampusLocation? = null
        set(value) {
            if (field == value) return
            val previous = field
            field = value
            // Only the two affected rows need rebinding.
            listOf(previous, value).forEach { location ->
                val index = currentList.indexOf(location)
                if (index >= 0) notifyItemChanged(index)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemSimpleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding, onLocationClicked)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, isSelected = item == selected)
    }

    class LocationViewHolder(
        private val binding: ItemSimpleCardBinding,
        private val onLocationClicked: (CampusLocation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(location: CampusLocation, isSelected: Boolean) {
            val context = binding.root.context
            binding.itemTitle.text = location.name
            binding.itemSubtitle.text = location.description
            binding.itemIconContainer.isVisible = true
            binding.itemIcon.setImageResource(R.drawable.ic_nav_map)

            val card = binding.root
            card.strokeWidth = context.resources.getDimensionPixelSize(
                if (isSelected) R.dimen.card_stroke_selected else R.dimen.card_stroke
            )
            card.setStrokeColor(
                ContextCompat.getColorStateList(
                    context,
                    if (isSelected) R.color.brand_accent else R.color.soft_line
                )
            )
            card.isSelected = isSelected
            card.setOnClickListener { onLocationClicked(location) }
        }
    }

    private object LocationDiffCallback : DiffUtil.ItemCallback<CampusLocation>() {
        override fun areItemsTheSame(oldItem: CampusLocation, newItem: CampusLocation) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: CampusLocation, newItem: CampusLocation) = oldItem == newItem
    }
}
