package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.CampusLocationAdapter
import com.dlsu.unisync.data.CampusMapData
import com.dlsu.unisync.databinding.FragmentCampusMapBinding
import com.dlsu.unisync.models.CampusLocation
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.util.CampusLocator
import com.dlsu.unisync.util.NextClassFinder
import com.dlsu.unisync.util.ScheduleFormatter
import com.dlsu.unisync.util.meetingStartMinutes
import com.dlsu.unisync.viewmodels.ScheduleViewModel

// Campus map: a stylised illustration whose buildings are tappable, kept in sync
// with the list below.
//
// It opens on the building the next class is in, so the screen answers "where am
// I going" before anyone taps anything. Tapping takes over from there -- a user
// exploring the map should not have their selection yanked back by a schedule
// update.
class CampusMapFragment : Fragment() {
    private val scheduleViewModel: ScheduleViewModel by activityViewModels { ScheduleViewModel.Factory }
    private var _binding: FragmentCampusMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationAdapter: CampusLocationAdapter
    private var chosenByUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCampusMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationAdapter = CampusLocationAdapter { location -> select(location) }
        binding.mapLocationsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = locationAdapter
        }
        locationAdapter.submitList(CampusMapData.keyLocations)

        binding.campusMap.locations = CampusMapData.keyLocations
        binding.campusMap.onLocationSelected = { location -> select(location) }

        val restored = savedInstanceState?.getString(STATE_SELECTED)
        if (restored != null) {
            chosenByUser = savedInstanceState.getBoolean(STATE_CHOSEN_BY_USER, false)
            CampusMapData.keyLocations.firstOrNull { it.name == restored }?.let(::select)
        }

        scheduleViewModel.entries.observe(viewLifecycleOwner) { entries ->
            if (!chosenByUser) showNextClass(entries)
        }
    }

    private fun showNextClass(entries: List<ScheduleEntry>) {
        val next = NextClassFinder.findNext(entries) ?: return
        val building = CampusLocator.buildingFor(next.room, CampusMapData.keyLocations)
        val time = next.meetingStartMinutes()?.let(ScheduleFormatter::formatTime)

        if (building == null) {
            // A room the map does not know, e.g. "Online". Say so rather than
            // highlighting the wrong building or silently showing nothing.
            if (next.room.isNotBlank()) {
                binding.mapSelection.text = getString(R.string.map_next_class_off_map, next.course, next.room)
            }
            return
        }

        highlight(building)
        binding.mapSelection.text = if (time == null) {
            getString(R.string.map_next_class, next.course, building.name)
        } else {
            getString(R.string.map_next_class_at, next.course, time, building.name)
        }
    }

    // Single entry point for a tap, so the map, the list, and the caption can
    // never disagree about what is highlighted.
    private fun select(location: CampusLocation) {
        chosenByUser = true
        highlight(location)
        binding.mapSelection.text = getString(R.string.map_selected, location.name, location.description)
    }

    private fun highlight(location: CampusLocation) {
        binding.campusMap.selected = location
        locationAdapter.selected = location
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.campusMap?.selected?.let { outState.putString(STATE_SELECTED, it.name) }
        outState.putBoolean(STATE_CHOSEN_BY_USER, chosenByUser)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val STATE_SELECTED = "selected_location"
        const val STATE_CHOSEN_BY_USER = "selection_was_chosen_by_user"
    }
}
