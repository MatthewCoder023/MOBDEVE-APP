package com.dlsu.unisync

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> openFragment(DashboardFragment())
                R.id.nav_schedule -> openFragment(ScheduleFragment())
                R.id.nav_tasks -> openFragment(TasksFragment())
                R.id.nav_map -> openFragment(CampusMapFragment())
                R.id.nav_profile -> openFragment(ProfileFragment())
                else -> false
            }
        }

        if (savedInstanceState == null) {
            openFragment(DashboardFragment())
        }
    }

    override fun openCrowdMonitoring() {
        openFragment(CrowdFragment())
    }

    override fun openQrCheckIn() {
        openFragment(QrFragment())
    }

    override fun openNotifications() {
        openFragment(NotificationsFragment())
    }

    private fun openFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
}
