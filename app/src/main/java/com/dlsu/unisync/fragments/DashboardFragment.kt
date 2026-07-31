package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.data.CampusRepository
import com.dlsu.unisync.databinding.FragmentDashboardBinding
import com.dlsu.unisync.util.NextClassFinder
import com.dlsu.unisync.util.UserProfile
import com.dlsu.unisync.viewmodels.ScheduleViewModel
import java.util.Calendar

// Home dashboard: time-of-day greeting, the next class computed from the
// user's schedule, shortcuts, and dummy daily updates.
class DashboardFragment : Fragment() {
    private val scheduleViewModel: ScheduleViewModel by activityViewModels { ScheduleViewModel.Factory }
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
            val next = NextClassFinder.findNext(entries) ?: entries.firstOrNull()
            binding.nextClassValue.text = next?.let { "${it.course} • ${it.schedule} • ${it.room}" }
                ?: getString(R.string.dashboard_no_classes)
        }

        binding.dashboardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(CampusRepository.dashboardUpdates, R.drawable.ic_today)
        }
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
