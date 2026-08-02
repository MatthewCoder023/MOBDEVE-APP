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
import com.dlsu.unisync.data.toTaskItem
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.util.DueDates
import com.dlsu.unisync.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Daily check for tasks that are due today or already overdue. Reads through
// Firestore's offline cache, so it still works without a connection.
class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!Prefs.remindersEnabled(applicationContext)) return Result.success()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()

        val dueTasks = try {
            FirebaseFirestore.getInstance()
                .collection("users").document(userId).collection("tasks")
                .get().await()
                .documents.mapNotNull { it.toTaskItem() }
                .filter { !it.isDone && it.dueAt != null && it.dueAt <= DueDates.todayUtcMidnight() }
                .sortedBy { it.dueAt }
        } catch (error: Exception) {
            // Transient failure (no cache yet, permissions, offline first run):
            // let WorkManager try again rather than dropping the day's reminder.
            return Result.retry()
        }
        if (dueTasks.isEmpty()) return Result.success()

        notifyDueTasks(dueTasks)
        return Result.success()
    }

    private fun notifyDueTasks(tasks: List<TaskItem>) {
        val manager = NotificationManagerCompat.from(applicationContext)
        // The user can revoke notifications at any time; skip quietly if so.
        if (!manager.areNotificationsEnabled()) return
        createChannel()

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
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
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
        applicationContext.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "task_reminders"
        const val NOTIFICATION_ID = 1001
        const val MAX_LINES = 5
    }
}
