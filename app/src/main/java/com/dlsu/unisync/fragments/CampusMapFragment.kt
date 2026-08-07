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
            // No setHasFixedSize here: this list is wrap_content inside a
            // ScrollView, so its height does depend on its contents. Claiming
            // otherwise suppressed the re-layout, left the list measured short,
            // and meant the last cards were never created -- which is why
            // scrolling to them found no view to scroll to.
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
        revealInList(location)
    }

    private fun highlight(location: CampusLocation) {
        binding.campusMap.selected = location
        locationAdapter.selected = location
    }

    // Tapping a building on the map outlines its card, which is useless when the
    // card is below the fold -- six buildings do not fit on screen with the map
    // above them. Scrolls the least amount that brings it into view, so as much
    // of the map as possible stays visible.
    //
    // Only for taps: doing this for the automatic next-class selection would
    // scroll the map off screen the instant the screen opened, hiding the very
    // thing the caption is talking about.
    private fun revealInList(location: CampusLocation) {
        val index = CampusMapData.keyLocations.indexOf(location)
        if (index < 0) return
        val recycler = binding.mapLocationsRecycler
        recycler.post {
            val card = recycler.layoutManager?.findViewByPosition(index) ?: return@post
            val scroll = _binding?.mapScroll ?: return@post

            // The page scrolls, not the list: the RecyclerView is wrap_content
            // with nested scrolling off, and in that state it answers
            // requestRectangleOnScreen itself and never passes it to the
            // ScrollView, so asking the card to reveal itself does nothing.
            val margin = resources.getDimensionPixelSize(R.dimen.space_lg)
            val cardTop = recycler.top + card.top
            val cardBottom = cardTop + card.height
            val visibleTop = scroll.scrollY
            val visibleBottom = visibleTop + scroll.height

            when {
                cardBottom > visibleBottom ->
                    scroll.smoothScrollTo(0, cardBottom - scroll.height + margin)

                cardTop < visibleTop ->
                    scroll.smoothScrollTo(0, cardTop - margin)
            }
        }
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
