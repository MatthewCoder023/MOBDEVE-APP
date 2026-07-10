package com.dlsu.unisync

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// With targetSdk 35 the system enforces edge-to-edge, so each screen's root view
// must pad itself clear of the status and navigation bars. Skip the bottom inset
// when a BottomNavigationView is present — Material applies that inset itself.
fun View.applySystemBarInsets(applyBottom: Boolean = true) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val bottom = if (applyBottom) bars.bottom else view.paddingBottom
        view.setPadding(bars.left, bars.top, bars.right, bottom)
        insets
    }
}
