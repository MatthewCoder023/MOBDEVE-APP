package com.dlsu.unisync

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.dlsu.unisync.data.TaskSeeder
import com.dlsu.unisync.databinding.ActivityAuthBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

// Launcher + login/register screen backed by Firebase Authentication. The system
// splash (SplashScreen API) shows while this loads; an already signed-in user
// goes straight to the main app.
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val isRegisterTab get() = binding.authTabs.selectedTabPosition == 1
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Skip the form entirely when a session already exists.
        if (auth.currentUser != null) {
            openMainApp()
            return
        }

        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.authTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.authTitle.text = getString(
                    if (tab.position == 0) R.string.welcome_back else R.string.auth_title_register
                )
                // While a request is in flight the button is showing the spinner
                // instead of a label; setLoading restores the right one after.
                if (!isLoading) {
                    binding.continueButton.text = getString(
                        if (tab.position == 0) R.string.action_sign_in else R.string.action_create_account
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.emailInput.doOnTextChanged { _, _, _, _ -> binding.emailLayout.error = null }
        binding.passwordInput.doOnTextChanged { _, _, _, _ -> binding.passwordLayout.error = null }

        binding.continueButton.setOnClickListener { submit() }
    }

    private fun submit() {
        if (!validateInput()) return
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        setLoading(true)
        val request = if (isRegisterTab) {
            auth.createUserWithEmailAndPassword(email, password)
        } else {
            auth.signInWithEmailAndPassword(email, password)
        }
        val registering = isRegisterTab
        request.addOnCompleteListener(this) { task ->
            setLoading(false)
            if (task.isSuccessful) {
                if (registering) {
                    auth.currentUser?.uid?.let(TaskSeeder::seedFor)
                }
                openMainApp()
            } else {
                showAuthError(task.exception)
            }
        }
    }

    private fun validateInput(): Boolean {
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        binding.emailLayout.error = when {
            email.isEmpty() -> getString(R.string.error_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            REQUIRED_EMAIL_DOMAIN != null && !email.endsWith("@$REQUIRED_EMAIL_DOMAIN", ignoreCase = true) ->
                getString(R.string.error_email_domain, REQUIRED_EMAIL_DOMAIN)
            else -> null
        }
        binding.passwordLayout.error = when {
            password.isEmpty() -> getString(R.string.error_password_required)
            password.length < MIN_PASSWORD_LENGTH -> getString(R.string.error_password_short)
            else -> null
        }
        return binding.emailLayout.error == null && binding.passwordLayout.error == null
    }

    private fun showAuthError(error: Exception?) {
        val messageRes = when (error) {
            is FirebaseAuthWeakPasswordException -> R.string.error_auth_weak_password
            is FirebaseAuthUserCollisionException -> R.string.error_auth_email_in_use
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> R.string.error_auth_invalid_credentials
            is FirebaseNetworkException -> R.string.error_auth_network
            else -> R.string.error_auth_generic
        }
        binding.authError.text = getString(messageRes)
        binding.authError.isVisible = true
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.authProgress.isVisible = loading
        // The label would otherwise sit behind the spinner.
        binding.continueButton.text = if (loading) "" else buttonLabel()
        binding.continueButton.isEnabled = !loading
        binding.emailInput.isEnabled = !loading
        binding.passwordInput.isEnabled = !loading
        if (loading) binding.authError.isVisible = false
    }

    private fun buttonLabel(): String = getString(
        if (isRegisterTab) R.string.action_create_account else R.string.action_sign_in
    )

    private fun openMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6

        // Restricts sign-in to DLSU accounts. Set to null to allow any domain
        // (useful when demoing with a personal address).
        val REQUIRED_EMAIL_DOMAIN: String? = "dlsu.edu.ph"
    }
}
