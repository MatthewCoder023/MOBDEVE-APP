package com.dlsu.unisync.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

// Enqueues (or cancels) the daily task-reminder check.
object TaskReminderScheduler {
    private const val WORK_NAME = "task-reminders"
    private const val REMINDER_HOUR = 8

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextReminder(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            // UPDATE keeps a single scheduled job while picking up changes.
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    // Next local 08:00; if that has passed today, tomorrow's.
    private fun millisUntilNextReminder(): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (next.timeInMillis <= now.timeInMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis - now.timeInMillis
    }
}
