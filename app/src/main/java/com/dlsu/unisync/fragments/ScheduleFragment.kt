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
import com.dlsu.unisync.adapters.ScheduleAdapter
import com.dlsu.unisync.databinding.DialogScheduleEntryBinding
import com.dlsu.unisync.databinding.FragmentScheduleBinding
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.viewmodels.ScheduleViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

// Room-backed, user-editable class schedule: add via dialog, tap to edit,
// swipe to remove with undo.
class ScheduleFragment : Fragment() {
    private val scheduleViewModel: ScheduleViewModel by activityViewModels { ScheduleViewModel.Factory }
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scheduleAdapter = ScheduleAdapter { entry -> showEntryDialog(entry) }
        binding.scheduleRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = scheduleAdapter
        }

        scheduleViewModel.entries.observe(viewLifecycleOwner) { entries ->
            scheduleAdapter.submitList(entries)
            binding.emptyState.isVisible = entries.isEmpty()
        }

        binding.addClassButton.setOnClickListener { showEntryDialog(null) }

        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val entry = scheduleAdapter.currentList[position]
                scheduleViewModel.removeEntry(entry)
                Snackbar.make(binding.root, R.string.schedule_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_undo) { scheduleViewModel.restoreEntry(entry) }
                    .show()
            }
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.scheduleRecycler)
    }

    // One dialog for both flows: existing == null creates, otherwise edits.
    private fun showEntryDialog(existing: ScheduleEntry?) {
        val dialogBinding = DialogScheduleEntryBinding.inflate(layoutInflater)
        dialogBinding.courseInput.setText(existing?.course.orEmpty())
        dialogBinding.dayTimeInput.setText(existing?.schedule.orEmpty())
        dialogBinding.roomInput.setText(existing?.room.orEmpty())
        dialogBinding.courseInput.doOnTextChanged { _, _, _, _ -> dialogBinding.courseLayout.error = null }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.schedule_dialog_new else R.string.schedule_dialog_edit)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val course = dialogBinding.courseInput.text?.toString()?.trim().orEmpty()
                val dayTime = dialogBinding.dayTimeInput.text?.toString()?.trim().orEmpty()
                val room = dialogBinding.roomInput.text?.toString()?.trim().orEmpty()
                if (course.isEmpty()) {
                    dialogBinding.courseLayout.error = getString(R.string.error_course_required)
                } else {
                    if (existing == null) {
                        scheduleViewModel.addEntry(course, dayTime, room)
                    } else {
                        scheduleViewModel.updateEntry(existing, course, dayTime, room)
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
