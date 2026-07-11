package com.dlsu.unisync

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dlsu.unisync.databinding.ActivityMainBinding

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

        val navController = (supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        binding.bottomNavigation.setupWithNavController(navController)
        // Reselecting the current tab pops back to the tab root, e.g. returning
        // from the dashboard shortcut screens (Crowd, QR, Alerts).
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, false)
        }
    }
}
