package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.databinding.ItemSimpleCardBinding
import com.dlsu.unisync.models.SimpleItem

// Generic card adapter for simple two-line prototype lists.
class SimpleItemAdapter(
    private val items: List<SimpleItem>
) : RecyclerView.Adapter<SimpleItemAdapter.SimpleItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val binding = ItemSimpleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimpleItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SimpleItemViewHolder(private val binding: ItemSimpleCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimpleItem) {
            binding.itemTitle.text = item.title
            binding.itemSubtitle.text = item.subtitle
        }
    }
}
