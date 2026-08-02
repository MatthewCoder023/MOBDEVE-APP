package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.databinding.FragmentNotificationsBinding
import com.dlsu.unisync.models.CampusAlert
import com.dlsu.unisync.models.SimpleItem
import com.dlsu.unisync.models.StatusLevel
import com.dlsu.unisync.viewmodels.NotificationsViewModel

// Notification centre: a live view over the app's own data — overdue and
// due-today tasks, the next class, and rooms that are busy right now.
class NotificationsFragment : Fragment() {
    private val notificationsViewModel: NotificationsViewModel by activityViewModels {
        NotificationsViewModel.Factory
    }
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.notificationsRecycler.layoutManager = LinearLayoutManager(requireContext())

        notificationsViewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            binding.notificationsRecycler.adapter = SimpleItemAdapter(alerts.map(::toItem))
            binding.notificationsEmpty.isVisible = alerts.isEmpty()
        }
    }

    // Wording lives here so AlertBuilder can stay free of Android types.
    private fun toItem(alert: CampusAlert): SimpleItem = when (alert) {
        is CampusAlert.OverdueTask -> SimpleItem(
            title = alert.title,
            subtitle = getString(R.string.alert_overdue, alert.due),
            level = StatusLevel.HIGH,
            icon = R.drawable.ic_nav_tasks
        )

        is CampusAlert.TaskDueToday -> SimpleItem(
            title = alert.title,
            subtitle = getString(R.string.alert_due_today),
            level = StatusLevel.MEDIUM,
            icon = R.drawable.ic_nav_tasks
        )

        is CampusAlert.NextClass -> SimpleItem(
            title = getString(R.string.alert_next_class, alert.course),
            subtitle = listOf(alert.schedule, alert.room).filter { it.isNotBlank() }.joinToString(" • "),
            icon = R.drawable.ic_nav_schedule
        )

        is CampusAlert.BusyRoom -> SimpleItem(
            title = getString(R.string.alert_busy_room, alert.room),
            subtitle = resources.getQuantityString(R.plurals.crowd_check_ins, alert.count, alert.count),
            level = StatusLevel.HIGH,
            icon = R.drawable.ic_crowd
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
