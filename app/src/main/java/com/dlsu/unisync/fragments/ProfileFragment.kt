package com.dlsu.unisync.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.dlsu.unisync.AuthActivity
import com.dlsu.unisync.databinding.FragmentProfileBinding

// Draft profile/settings screen. Notification preferences persist in
// SharedPreferences; log-out returns to the auth screen.
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        binding.notificationSwitch.isChecked = prefs.getBoolean(KEY_REMINDERS, true)
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_REMINDERS, isChecked) }
        }

        binding.crowdSwitch.isChecked = prefs.getBoolean(KEY_CROWD_ALERTS, true)
        binding.crowdSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_CROWD_ALERTS, isChecked) }
        }

        binding.logoutButton.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val PREFS_NAME = "unisync_prefs"
        private const val KEY_REMINDERS = "pref_reminders"
        private const val KEY_CROWD_ALERTS = "pref_crowd_alerts"
    }
}
