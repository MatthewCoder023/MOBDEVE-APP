package com.dlsu.unisync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dlsu.unisync.R
import com.dlsu.unisync.databinding.FragmentQrBinding

// Simulated QR check-in screen for presentation flow.
class QrFragment : Fragment() {
    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.checkInButton.setOnClickListener {
            binding.checkInStatus.text = getString(R.string.qr_status_done)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
