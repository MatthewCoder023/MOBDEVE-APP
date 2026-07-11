package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.data.CampusRepository
import com.dlsu.unisync.databinding.FragmentCrowdBinding

// Shows dummy occupancy data for common campus locations.
class CrowdFragment : Fragment() {
    private var _binding: FragmentCrowdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrowdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.crowdRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(CampusRepository.crowdLevels)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
