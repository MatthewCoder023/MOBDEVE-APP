package com.dlsu.unisync.fragments

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.databinding.FragmentDashboardBinding
import com.dlsu.unisync.models.SimpleItem
import com.dlsu.unisync.models.StatusLevel
import com.dlsu.unisync.models.TodayEntry
import com.dlsu.unisync.util.NextClassFinder
import com.dlsu.unisync.util.UserProfile
import com.dlsu.unisync.viewmodels.DashboardViewModel
import com.dlsu.unisync.viewmodels.ScheduleViewModel
import java.util.Calendar

// Home dashboard: time-of-day greeting, the next class computed from the user's
// schedule, shortcuts, and today's agenda built from their classes and tasks.
class DashboardFragment : Fragment() {
    private val scheduleViewModel: ScheduleViewModel by activityViewModels { ScheduleViewModel.Factory }
    private val dashboardViewModel: DashboardViewModel by activityViewModels { DashboardViewModel.Factory }
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.greetingText.text = greeting()

        binding.openCrowdButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_crowd)
        }
        binding.openQrButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_qr)
        }
        binding.openNotificationsButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_notifications)
        }
        binding.openScheduleButton.setOnClickListener {
            // Same options NavigationUI uses for bottom-nav items, so this behaves
            // exactly like tapping the Schedule tab (no stacked duplicates).
            val navController = findNavController()
            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.startDestinationId, inclusive = false, saveState = true)
                .build()
            navController.navigate(R.id.nav_schedule, null, options)
        }

        scheduleViewModel.entries.observe(viewLifecycleOwner) { entries ->
            val next = NextClassFinder.findNext(entries)
            binding.nextClassValue.text = when {
                next != null -> "${next.course} • ${next.schedule} • ${next.room}"
                entries.isEmpty() -> getString(R.string.dashboard_no_classes)
                // Classes exist, but none of them can be placed on a day, so
                // there is no "next" one. This used to fall back to whichever
                // class happened to be first in the list and label it Next
                // class, which presented a class the app could not place in time
                // as though it were coming up.
                else -> getString(R.string.dashboard_no_upcoming_class)
            }
        }

        binding.dashboardRecycler.layoutManager = LinearLayoutManager(requireContext())
        dashboardViewModel.today.observe(viewLifecycleOwner) { entries ->
            binding.dashboardRecycler.adapter = SimpleItemAdapter(entries.map(::toItem))
            binding.dashboardEmpty.isVisible = entries.isEmpty()
        }
    }

    // Wording lives here so TodayBuilder can stay free of Android types.
    private fun toItem(entry: TodayEntry): SimpleItem = when (entry) {
        is TodayEntry.ClassSession -> SimpleItem(
            title = entry.course,
            subtitle = classSubtitle(entry),
            icon = R.drawable.ic_nav_schedule
        )

        is TodayEntry.TaskDue -> SimpleItem(
            title = entry.title,
            subtitle = getString(
                if (entry.isDone) R.string.today_task_done else R.string.today_task_due
            ),
            level = if (entry.isDone) StatusLevel.LOW else StatusLevel.MEDIUM,
            icon = R.drawable.ic_nav_tasks
        )
    }

    private fun classSubtitle(session: TodayEntry.ClassSession): String {
        val parts = buildList {
            if (session.isOver) add(getString(R.string.today_class_over))
            session.startMinutes?.let { add(formatTime(it)) }
            if (session.room.isNotBlank()) add(session.room)
        }
        return parts.joinToString(" • ")
    }

    // Uses the device's 12/24-hour setting rather than a hard-coded pattern.
    private fun formatTime(minutesPastMidnight: Int): String {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutesPastMidnight / 60)
            set(Calendar.MINUTE, minutesPastMidnight % 60)
        }
        return DateFormat.getTimeFormat(requireContext()).format(time.time)
    }

    private fun greeting(): String {
        val greetingRes = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> R.string.greeting_morning
            in 12..17 -> R.string.greeting_afternoon
            else -> R.string.greeting_evening
        }
        return getString(greetingRes, UserProfile.firstName())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
