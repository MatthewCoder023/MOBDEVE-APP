package com.dlsu.unisync.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dlsu.unisync.R
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.util.Prefs
import java.util.Calendar
import java.util.TimeZone

// Daily check for tasks that are due today or already overdue. Runs off the
// main thread via WorkManager and posts a single summary notification.
class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!Prefs.remindersEnabled(applicationContext)) return Result.success()

        val dao = UniSyncDatabase.getInstance(applicationContext).taskDao()
        val dueTasks = dao.getDueBy(endOfTodayUtc())
        if (dueTasks.isEmpty()) return Result.success()

        notifyDueTasks(dueTasks)
        return Result.success()
    }

    private fun notifyDueTasks(tasks: List<TaskItem>) {
        val manager = NotificationManagerCompat.from(applicationContext)
        // The user can revoke notifications at any time; skip quietly if so.
        if (!manager.areNotificationsEnabled()) return
        createChannel()

        val title = applicationContext.getString(R.string.reminder_title)
        val text = if (tasks.size == 1) {
            applicationContext.getString(R.string.reminder_single, tasks.first().title)
        } else {
            applicationContext.resources.getQuantityString(
                R.plurals.reminder_multiple,
                tasks.size,
                tasks.size
            )
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alerts)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.InboxStyle().also { style ->
                    tasks.take(MAX_LINES).forEach { style.addLine("${it.title} — ${it.due}") }
                }
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (securityException: SecurityException) {
            // POST_NOTIFICATIONS revoked between the check and the post.
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = applicationContext.getString(R.string.reminder_channel_description)
        }
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    // dueAt holds UTC-midnight values, so "due today" means dueAt <= today's
    // UTC midnight. Anything earlier is overdue and also worth surfacing.
    private fun endOfTodayUtc(): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val CHANNEL_ID = "task_reminders"
        const val NOTIFICATION_ID = 1001
        const val MAX_LINES = 5
    }
}
