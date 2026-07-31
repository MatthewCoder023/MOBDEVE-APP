package com.dlsu.unisync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dlsu.unisync.databinding.ActivityMainBinding
import com.dlsu.unisync.util.Prefs
import com.dlsu.unisync.work.TaskReminderScheduler

// Hosts the navigation graph; NavigationUI keeps the bottom bar, back stack,
// and per-tab state in sync.
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets(applyBottom = false)

        // Re-arm the daily reminder job on launch; WorkManager keeps a single
        // scheduled instance, so this is cheap and survives reboots/updates.
        if (Prefs.remindersEnabled(this)) {
            TaskReminderScheduler.schedule(this)
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNavigation.setupWithNavController(navController)
        // Reselecting the current tab pops back to the tab root, e.g. returning
        // from the dashboard shortcut screens (Crowd, QR, Alerts).
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, false)
        }
    }
}
