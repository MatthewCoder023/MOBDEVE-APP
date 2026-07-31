package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.data.CampusRepository
import com.dlsu.unisync.databinding.FragmentCampusMapBinding

// Placeholder campus map: a stylised illustration plus the key buildings.
// Swap the illustration for a SupportMapFragment once a Maps SDK key exists.
class CampusMapFragment : Fragment() {
    private var _binding: FragmentCampusMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCampusMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mapLocationsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = SimpleItemAdapter(CampusRepository.keyLocations, R.drawable.ic_nav_map)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
