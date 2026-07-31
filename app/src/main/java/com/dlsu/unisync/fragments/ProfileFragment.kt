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
import com.dlsu.unisync.util.UserProfile
import com.google.firebase.auth.FirebaseAuth

// Profile/settings screen. Identity comes from the signed-in Firebase user;
// notification preferences persist in SharedPreferences.
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.profileName.text = UserProfile.displayName()
        binding.profileDetails.text = UserProfile.email()
        binding.avatarInitials.text = UserProfile.initials()

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
            FirebaseAuth.getInstance().signOut()
            // Clear the back stack so back can't return into the signed-in app.
            val intent = Intent(requireContext(), AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
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
