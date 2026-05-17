package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.TaskAdapter
import com.dlsu.unisync.models.TaskItem

// Prototype task tracker with local-only dummy task creation.
class TasksFragment : Fragment() {
    private val tasks = mutableListOf(
        TaskItem("Finalize MOBDEVE wireframes", "Due tonight at 11:59 PM"),
        TaskItem("Read HCI chapter 6", "Due tomorrow"),
        TaskItem("Group meeting notes", "Due Friday", true)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val taskAdapter = TaskAdapter(tasks)
        view.findViewById<RecyclerView>(R.id.taskRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        view.findViewById<Button>(R.id.addDummyTaskButton).setOnClickListener {
            taskAdapter.addTask(TaskItem("New sample task", "Added just now"))
        }
    }
}
