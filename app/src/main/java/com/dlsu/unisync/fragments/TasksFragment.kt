package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.TaskAdapter
import com.dlsu.unisync.databinding.DialogTaskBinding
import com.dlsu.unisync.databinding.FragmentTasksBinding
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.viewmodels.TasksViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Room-backed task tracker. Tasks are created and edited through a dialog with
// a Material date picker; checking off, swipe-to-delete, and undo as before.
class TasksFragment : Fragment() {
    private val tasksViewModel: TasksViewModel by activityViewModels { TasksViewModel.Factory }
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val dueDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val taskAdapter = TaskAdapter(
            onTaskToggled = { task, isChecked -> tasksViewModel.setTaskDone(task, isChecked) },
            onTaskClicked = { task -> showTaskDialog(task) }
        )
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

        binding.addDummyTaskButton.setOnClickListener { showTaskDialog(null) }

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

    // One dialog for both flows: existing == null creates, otherwise edits.
    private fun showTaskDialog(existing: TaskItem?) {
        val dialogBinding = DialogTaskBinding.inflate(layoutInflater)
        var selectedDueAt: Long? = existing?.dueAt
        var dueText = existing?.due ?: getString(R.string.task_no_due)

        dialogBinding.titleInput.setText(existing?.title.orEmpty())
        dialogBinding.dueInput.setText(dueText)
        dialogBinding.titleInput.doOnTextChanged { _, _, _, _ -> dialogBinding.titleLayout.error = null }
        dialogBinding.dueInput.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.hint_task_due)
                .apply { selectedDueAt?.let { setSelection(it) } }
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                selectedDueAt = selection
                dueText = getString(R.string.task_due_on, dueDateFormat.format(Date(selection)))
                dialogBinding.dueInput.setText(dueText)
            }
            picker.show(childFragmentManager, "task_due_date")
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.task_dialog_new else R.string.task_dialog_edit)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        // Positive button is overridden after show() so invalid input keeps the
        // dialog open instead of silently dismissing.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = dialogBinding.titleInput.text?.toString()?.trim().orEmpty()
                if (title.isEmpty()) {
                    dialogBinding.titleLayout.error = getString(R.string.error_task_title_required)
                } else {
                    if (existing == null) {
                        tasksViewModel.addTask(title, dueText, selectedDueAt)
                    } else {
                        tasksViewModel.updateTask(existing, title, dueText, selectedDueAt)
                    }
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
