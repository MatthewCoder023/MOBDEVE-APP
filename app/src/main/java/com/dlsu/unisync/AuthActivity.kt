package com.dlsu.unisync

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.doOnTextChanged
import com.dlsu.unisync.databinding.ActivityAuthBinding
import com.google.android.material.tabs.TabLayout

// Launcher + login/register screen; the system splash (SplashScreen API) shows
// while this loads. Input is validated for real, but the authentication itself
// is still simulated with dummy navigation.
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.authTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.authTitle.text = getString(
                    if (tab.position == 0) R.string.welcome_back else R.string.auth_title_register
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.emailInput.doOnTextChanged { _, _, _, _ -> binding.emailLayout.error = null }
        binding.passwordInput.doOnTextChanged { _, _, _, _ -> binding.passwordLayout.error = null }

        binding.continueButton.setOnClickListener {
            if (validateInput()) {
                Toast.makeText(this, R.string.auth_demo_accepted, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun validateInput(): Boolean {
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        binding.emailLayout.error = when {
            email.isEmpty() -> getString(R.string.error_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            else -> null
        }
        binding.passwordLayout.error = when {
            password.isEmpty() -> getString(R.string.error_password_required)
            password.length < 6 -> getString(R.string.error_password_short)
            else -> null
        }
        return binding.emailLayout.error == null && binding.passwordLayout.error == null
    }
}
