package com.dlsu.unisync.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.dlsu.unisync.AuthActivity
import com.dlsu.unisync.R
import com.dlsu.unisync.databinding.FragmentProfileBinding
import com.dlsu.unisync.util.Prefs
import com.dlsu.unisync.util.UserProfile
import com.dlsu.unisync.work.TaskReminderScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

// Profile/settings screen. Identity comes from the signed-in Firebase user;
// notification preferences persist in SharedPreferences and drive the daily
// task-reminder job.
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableReminders()
            } else {
                // Without the permission a reminder could never be shown, so
                // roll the switch back rather than leave a promise unkept.
                setRemindersChecked(false)
                Prefs.of(requireContext()).edit { putBoolean(Prefs.KEY_REMINDERS, false) }
                Snackbar.make(binding.root, R.string.reminder_permission_denied, Snackbar.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.profileName.text = UserProfile.displayName()
        binding.profileDetails.text = UserProfile.email()
        binding.avatarInitials.text = UserProfile.initials()

        val prefs = Prefs.of(requireContext())
        setRemindersChecked(prefs.getBoolean(Prefs.KEY_REMINDERS, true))

        binding.crowdSwitch.isChecked = prefs.getBoolean(Prefs.KEY_CROWD_ALERTS, true)
        binding.crowdSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(Prefs.KEY_CROWD_ALERTS, isChecked) }
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            TaskReminderScheduler.cancel(requireContext())
            // Clear the back stack so back can't return into the signed-in app.
            val intent = Intent(requireContext(), AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun onRemindersToggled(enabled: Boolean) {
        Prefs.of(requireContext()).edit { putBoolean(Prefs.KEY_REMINDERS, enabled) }
        if (!enabled) {
            TaskReminderScheduler.cancel(requireContext())
            return
        }
        if (needsNotificationPermission()) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            enableReminders()
        }
    }

    private fun enableReminders() {
        Prefs.of(requireContext()).edit { putBoolean(Prefs.KEY_REMINDERS, true) }
        setRemindersChecked(true)
        TaskReminderScheduler.schedule(requireContext())
    }

    private fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

    // Detaches the listener while setting the value so programmatic updates
    // (restoring state, rolling back a denied permission) don't re-enter.
    private fun setRemindersChecked(checked: Boolean) {
        binding.notificationSwitch.setOnCheckedChangeListener(null)
        binding.notificationSwitch.isChecked = checked
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            onRemindersToggled(isChecked)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
