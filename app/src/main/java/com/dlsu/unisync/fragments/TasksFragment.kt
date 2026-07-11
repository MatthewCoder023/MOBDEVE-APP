package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.TaskAdapter
import com.dlsu.unisync.databinding.FragmentTasksBinding
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.viewmodels.TasksViewModel

// Prototype task tracker backed by an activity-scoped ViewModel.
class TasksFragment : Fragment() {
    private val tasksViewModel: TasksViewModel by activityViewModels()
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val taskAdapter = TaskAdapter(tasksViewModel.tasks)
        binding.taskRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        binding.addDummyTaskButton.setOnClickListener {
            tasksViewModel.addTask(
                TaskItem(getString(R.string.task_new_title), getString(R.string.task_new_due))
            )
            taskAdapter.notifyItemInserted(0)
            binding.taskRecycler.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
