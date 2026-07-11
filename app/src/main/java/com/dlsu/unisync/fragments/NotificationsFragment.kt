package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dlsu.unisync.adapters.SimpleItemAdapter
import com.dlsu.unisync.data.CampusRepository
import com.dlsu.unisync.databinding.FragmentNotificationsBinding

// Notification center with dummy academic and campus alerts.
class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.notificationsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SimpleItemAdapter(CampusRepository.notifications)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
