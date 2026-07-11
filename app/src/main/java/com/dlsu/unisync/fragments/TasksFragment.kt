package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.TaskAdapter
import com.dlsu.unisync.databinding.FragmentTasksBinding
import com.dlsu.unisync.viewmodels.TasksViewModel
import com.google.android.material.snackbar.Snackbar

// Room-backed task tracker. Supports checking off, adding, and swipe-to-delete
// with undo; the activity-scoped ViewModel keeps state across tab switches.
class TasksFragment : Fragment() {
    private val tasksViewModel: TasksViewModel by activityViewModels { TasksViewModel.Factory }
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val taskAdapter = TaskAdapter { task, isChecked ->
            tasksViewModel.setTaskDone(task, isChecked)
        }
        binding.taskRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = taskAdapter
        }

        // Keep the newest task visible when one is added at the top. Guarded with
        // the nullable binding because DiffUtil commits asynchronously.
        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) _binding?.taskRecycler?.scrollToPosition(0)
            }
        })

        tasksViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
            binding.emptyState.isVisible = tasks.isEmpty()
        }

        binding.addDummyTaskButton.setOnClickListener {
            tasksViewModel.addTask(getString(R.string.task_new_title), getString(R.string.task_new_due))
        }

        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val task = taskAdapter.currentList[position]
                tasksViewModel.removeTask(task)
                Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_undo) { tasksViewModel.restoreTask(task) }
                    .show()
            }
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.taskRecycler)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
