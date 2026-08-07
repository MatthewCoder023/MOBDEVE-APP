package com.dlsu.unisync.fragments

import android.os.Bundle
import android.text.format.DateFormat
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
import com.dlsu.unisync.util.ScheduleDays
import com.dlsu.unisync.util.ScheduleFormatter
import com.dlsu.unisync.util.meetingDays
import com.dlsu.unisync.util.meetingStartMinutes
import com.dlsu.unisync.util.shrinkOnScroll
import com.dlsu.unisync.viewmodels.ScheduleViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

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

        binding.addClassFab.setOnClickListener { showEntryDialog(null) }
        binding.addClassFab.shrinkOnScroll(binding.scheduleRecycler)

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
    // Days and time are picked rather than typed, so a saved class always has
    // enough structure to appear in the next-class card, Today, and reminders.
    private fun showEntryDialog(existing: ScheduleEntry?) {
        val dialogBinding = DialogScheduleEntryBinding.inflate(layoutInflater)
        val chips = mapOf(
            Calendar.MONDAY to dialogBinding.chipMon,
            Calendar.TUESDAY to dialogBinding.chipTue,
            Calendar.WEDNESDAY to dialogBinding.chipWed,
            Calendar.THURSDAY to dialogBinding.chipThu,
            Calendar.FRIDAY to dialogBinding.chipFri,
            Calendar.SATURDAY to dialogBinding.chipSat,
            Calendar.SUNDAY to dialogBinding.chipSun
        )

        // Editing an entry saved before this picker existed prefills whatever
        // could be parsed out of its text, so the fix is one tap away.
        val prefilledDays = existing?.meetingDays().orEmpty()
        chips.forEach { (day, chip) ->
            chip.isChecked = day in prefilledDays
            chip.setOnCheckedChangeListener { _, _ -> dialogBinding.daysError.isVisible = false }
        }

        var startMinutes: Int? = existing?.meetingStartMinutes()
        fun renderTime() {
            dialogBinding.startTimeInput.setText(startMinutes?.let(ScheduleFormatter::formatTime).orEmpty())
        }
        renderTime()

        dialogBinding.startTimeInput.setOnClickListener {
            val current = startMinutes ?: ScheduleFormatter.defaultStartMinutes()
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(
                    if (DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
                )
                .setHour(ScheduleFormatter.hourOf(current))
                .setMinute(ScheduleFormatter.minuteOf(current))
                .setTitleText(R.string.hint_start_time)
                .build()
            picker.addOnPositiveButtonClickListener {
                startMinutes = ScheduleFormatter.minutesOf(picker.hour, picker.minute)
                renderTime()
            }
            picker.show(childFragmentManager, TIME_PICKER_TAG)
        }

        dialogBinding.courseInput.setText(existing?.course.orEmpty())
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
                val room = dialogBinding.roomInput.text?.toString()?.trim().orEmpty()
                val days = chips.filterValues { it.isChecked }.keys.toList()

                dialogBinding.courseLayout.error =
                    if (course.isEmpty()) getString(R.string.error_course_required) else null
                dialogBinding.daysError.isVisible = days.isEmpty()
                if (course.isEmpty() || days.isEmpty()) return@setOnClickListener

                // Sorted so the saved text always reads Mon-first, whatever
                // order the chips were tapped in.
                val ordered = ScheduleDays.ORDER.filter { it in days }
                if (existing == null) {
                    scheduleViewModel.addEntry(course, ordered, startMinutes, room)
                } else {
                    scheduleViewModel.updateEntry(existing, course, ordered, startMinutes, room)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private companion object {
        const val TIME_PICKER_TAG = "schedule_time_picker"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
