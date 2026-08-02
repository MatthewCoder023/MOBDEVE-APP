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
import com.dlsu.unisync.databinding.FragmentCrowdBinding
import com.dlsu.unisync.models.SimpleItem
import com.dlsu.unisync.viewmodels.CrowdViewModel

// Live crowd levels, derived from QR check-ins recorded by every account in the
// current hour. Rooms with no activity simply do not appear.
class CrowdFragment : Fragment() {
    private val crowdViewModel: CrowdViewModel by activityViewModels { CrowdViewModel.Factory }
    private var _binding: FragmentCrowdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCrowdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.crowdRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        crowdViewModel.readings.observe(viewLifecycleOwner) { readings ->
            binding.crowdRecycler.adapter = SimpleItemAdapter(
                readings.map { reading ->
                    SimpleItem(
                        title = reading.room,
                        subtitle = resources.getQuantityString(
                            R.plurals.crowd_check_ins,
                            reading.count,
                            reading.count
                        ),
                        progressPercent = reading.progressPercent,
                        level = reading.level
                    )
                },
                R.drawable.ic_crowd
            )
            binding.crowdEmpty.isVisible = readings.isEmpty()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
