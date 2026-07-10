package com.dlsu.unisync

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dlsu.unisync.fragments.CampusMapFragment
import com.dlsu.unisync.fragments.CrowdFragment
import com.dlsu.unisync.fragments.DashboardFragment
import com.dlsu.unisync.fragments.NotificationsFragment
import com.dlsu.unisync.fragments.ProfileFragment
import com.dlsu.unisync.fragments.QrFragment
import com.dlsu.unisync.fragments.ScheduleFragment
import com.dlsu.unisync.fragments.TasksFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

// Hosts the main prototype screens and keeps navigation simple for students to extend.
class MainActivity : AppCompatActivity(), DashboardFragment.DashboardNavigator {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.mainRoot).applySystemBarInsets(applyBottom = false)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            clearShortcutBackStack()
            when (item.itemId) {
                R.id.nav_home -> openFragment(DashboardFragment())
                R.id.nav_schedule -> openFragment(ScheduleFragment())
                R.id.nav_tasks -> openFragment(TasksFragment())
                R.id.nav_map -> openFragment(CampusMapFragment())
                R.id.nav_profile -> openFragment(ProfileFragment())
                else -> false
            }
        }
        // Reselecting the current tab returns from shortcut screens (Crowd, QR, Alerts).
        bottomNavigation.setOnItemReselectedListener {
            clearShortcutBackStack()
        }

        if (savedInstanceState == null) {
            openFragment(DashboardFragment())
        }
    }

    override fun openCrowdMonitoring() {
        openFragment(CrowdFragment(), addToBackStack = true)
    }

    override fun openQrCheckIn() {
        openFragment(QrFragment(), addToBackStack = true)
    }

    override fun openNotifications() {
        openFragment(NotificationsFragment(), addToBackStack = true)
    }

    private fun clearShortcutBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun openFragment(fragment: Fragment, addToBackStack: Boolean = false): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .apply { if (addToBackStack) addToBackStack(null) }
            .commit()
        return true
    }
}
