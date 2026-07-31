package com.dlsu.unisync.util

import android.content.Context
import android.content.SharedPreferences

// Shared preference keys, so the profile screen and the reminder worker agree
// on where the notification settings live.
object Prefs {
    private const val NAME = "unisync_prefs"
    const val KEY_REMINDERS = "pref_reminders"
    const val KEY_CROWD_ALERTS = "pref_crowd_alerts"

    fun of(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun remindersEnabled(context: Context): Boolean = of(context).getBoolean(KEY_REMINDERS, true)
}
