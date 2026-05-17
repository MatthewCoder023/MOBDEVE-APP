package com.dlsu.unisync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout

// Prototype login/register screen. Authentication is simulated with dummy navigation.
class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val titleText = findViewById<TextView>(R.id.authTitle)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val tabs = findViewById<TabLayout>(R.id.authTabs)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                titleText.text = if (tab.position == 0) "Welcome back, Lasallian" else "Create your UniSync account"
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        continueButton.setOnClickListener {
            Toast.makeText(this, "Demo account accepted", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
