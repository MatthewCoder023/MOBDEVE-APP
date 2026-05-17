package com.dlsu.unisync.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.models.SimpleItem

// Generic card adapter for simple two-line prototype lists.
class SimpleItemAdapter(
    private val items: List<SimpleItem>
) : RecyclerView.Adapter<SimpleItemAdapter.SimpleItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_card, parent, false)
        return SimpleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SimpleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.itemTitle)
        private val subtitleText: TextView = itemView.findViewById(R.id.itemSubtitle)

        fun bind(item: SimpleItem) {
            titleText.text = item.title
            subtitleText.text = item.subtitle
        }
    }
}
