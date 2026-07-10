package com.dlsu.unisync

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Lightweight splash screen. The delay is lifecycle-aware, so leaving the screen
// before it fires cancels the pending navigation instead of relaunching the app.
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        findViewById<View>(R.id.splashRoot).applySystemBarInsets()

        lifecycleScope.launch {
            delay(1200)
            startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
            finish()
        }
    }
}
