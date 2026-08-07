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
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.util.hasReadableSchedule

// Renders schedule entries on the shared two-line card; row taps open the
// edit dialog in ScheduleFragment.
class ScheduleAdapter(
    private val onEntryClicked: (ScheduleEntry) -> Unit
) : ListAdapter<ScheduleEntry, ScheduleAdapter.EntryViewHolder>(EntryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemSimpleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding, onEntryClicked)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EntryViewHolder(
        private val binding: ItemSimpleCardBinding,
        private val onEntryClicked: (ScheduleEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: ScheduleEntry) {
            val context = binding.root.context
            binding.itemTitle.text = entry.course

            // An entry whose days cannot be read is invisible to the next-class
            // card, the Today list and reminders. Say so on the card instead of
            // letting the user assume it is set up.
            val readable = entry.hasReadableSchedule
            val details = listOf(entry.schedule, entry.room).filter { it.isNotBlank() }
            binding.itemSubtitle.text = if (readable) {
                details.joinToString(" • ")
            } else {
                (details + context.getString(R.string.schedule_unreadable)).joinToString(" • ")
            }

            binding.itemIconContainer.isVisible = true
            binding.itemIcon.setImageResource(R.drawable.ic_nav_schedule)
            val colorRes = if (readable) R.color.brand_accent else R.color.status_high
            val containerRes = if (readable) R.color.brand_container else R.color.status_high_container
            binding.itemIcon.imageTintList = ContextCompat.getColorStateList(context, colorRes)
            binding.itemIconContainer.backgroundTintList =
                ContextCompat.getColorStateList(context, containerRes)

            binding.root.setOnClickListener { onEntryClicked(entry) }
        }
    }

    private object EntryDiffCallback : DiffUtil.ItemCallback<ScheduleEntry>() {
        override fun areItemsTheSame(oldItem: ScheduleEntry, newItem: ScheduleEntry) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ScheduleEntry, newItem: ScheduleEntry) = oldItem == newItem
    }
}
