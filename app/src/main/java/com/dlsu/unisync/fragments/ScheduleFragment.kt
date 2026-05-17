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

// Draft schedule manager using static class data.
class ScheduleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val schedule = listOf(
            SimpleItem("MOBDEVE", "Mon/Wed • 1:00 PM • Gokongwei 305"),
            SimpleItem("CCAPDEV", "Tue/Thu • 9:15 AM • Velasco 201"),
            SimpleItem("ST-MATH", "Friday • 10:00 AM • Andrew 1404"),
            SimpleItem("GEWORLD", "Online • Saturday • 8:00 AM")
        )

        view.findViewById<RecyclerView>(R.id.scheduleRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(schedule)
        }
    }
}
