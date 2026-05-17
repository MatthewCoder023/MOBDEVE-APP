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

// Notification center with dummy academic and campus alerts.
class NotificationsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val notifications = listOf(
            SimpleItem("Deadline reminder", "MOBDEVE prototype draft is due today."),
            SimpleItem("Room update", "CCAPDEV moved to Velasco 202 for this week."),
            SimpleItem("Crowd alert", "Agno is busy. Consider checking nearby options.")
        )

        view.findViewById<RecyclerView>(R.id.notificationsRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(notifications)
        }
    }
}
