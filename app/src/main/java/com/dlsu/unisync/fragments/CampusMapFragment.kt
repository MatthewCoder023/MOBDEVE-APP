package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.CampusLocationAdapter
import com.dlsu.unisync.data.CampusRepository
import com.dlsu.unisync.databinding.FragmentCampusMapBinding
import com.dlsu.unisync.models.CampusLocation

// Campus map placeholder: a stylised illustration whose buildings are tappable,
// kept in sync with the list below. Swap CampusMapView for a real map view once
// a Maps SDK key exists.
class CampusMapFragment : Fragment() {
    private var _binding: FragmentCampusMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationAdapter: CampusLocationAdapter

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
        locationAdapter.submitList(CampusRepository.keyLocations)

        binding.campusMap.locations = CampusRepository.keyLocations
        binding.campusMap.onLocationSelected = { location -> select(location) }

        savedInstanceState?.getString(STATE_SELECTED)?.let { name ->
            CampusRepository.keyLocations.firstOrNull { it.name == name }?.let(::select)
        }
    }

    // Single entry point for selection so the map, the list, and the caption
    // can never disagree about what is highlighted.
    private fun select(location: CampusLocation) {
        binding.campusMap.selected = location
        locationAdapter.selected = location
        binding.mapSelection.text = getString(R.string.map_selected, location.name, location.description)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.campusMap?.selected?.let { outState.putString(STATE_SELECTED, it.name) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val STATE_SELECTED = "selected_location"
    }
}
