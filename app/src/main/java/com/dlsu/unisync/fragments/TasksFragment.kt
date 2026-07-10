package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.TaskAdapter
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.viewmodels.TasksViewModel

// Prototype task tracker. Task state lives in an activity-scoped ViewModel so it
// survives tab switches and rotation.
class TasksFragment : Fragment() {
    private val tasksViewModel: TasksViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val taskAdapter = TaskAdapter(tasksViewModel.tasks)
        val recycler = view.findViewById<RecyclerView>(R.id.taskRecycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        view.findViewById<Button>(R.id.addDummyTaskButton).setOnClickListener {
            taskAdapter.addTask(TaskItem("New sample task", "Added just now"))
            recycler.scrollToPosition(0)
        }
    }
}
