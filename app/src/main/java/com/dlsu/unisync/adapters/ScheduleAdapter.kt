package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.databinding.ItemSimpleCardBinding
import com.dlsu.unisync.models.ScheduleEntry

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
            binding.itemTitle.text = entry.course
            binding.itemSubtitle.text = "${entry.schedule} • ${entry.room}"
            binding.root.setOnClickListener { onEntryClicked(entry) }
        }
    }

    private object EntryDiffCallback : DiffUtil.ItemCallback<ScheduleEntry>() {
        override fun areItemsTheSame(oldItem: ScheduleEntry, newItem: ScheduleEntry) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ScheduleEntry, newItem: ScheduleEntry) = oldItem == newItem
    }
}
