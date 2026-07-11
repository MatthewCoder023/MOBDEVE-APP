package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.R
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.data.CampusRepository
import com.dlsu.unisync.databinding.FragmentDashboardBinding

// Home dashboard with shortcuts and dummy daily updates.
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.openCrowdButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_crowd)
        }
        binding.openQrButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_qr)
        }
        binding.openNotificationsButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_notifications)
        }

        binding.dashboardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(CampusRepository.dashboardUpdates)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
