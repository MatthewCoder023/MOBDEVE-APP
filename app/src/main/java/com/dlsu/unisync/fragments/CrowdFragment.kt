package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.models.SimpleItem

// Shows dummy occupancy data for common campus locations.
class CrowdFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_crowd, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val locations = listOf(
            SimpleItem("Henry Sy Library", "Moderate • 62% capacity"),
            SimpleItem("Agno Food Court", "High • 84% capacity"),
            SimpleItem("Gokongwei Lobby", "Light • 28% capacity"),
            SimpleItem("Velasco Hall", "Moderate • 51% capacity")
        )

        view.findViewById<RecyclerView>(R.id.crowdRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(locations)
        }
    }
}
