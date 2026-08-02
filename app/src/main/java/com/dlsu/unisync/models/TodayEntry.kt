package com.dlsu.unisync.models

// One line of the dashboard's "Today" list. Unlike the notification feed, this
// is the full agenda for the day: classes that already happened and tasks that
// are already ticked off stay on it, marked as done.
sealed interface TodayEntry {
    // startMinutes is minutes past midnight, or null when the entry's text has
    // no recognizable time.
    data class ClassSession(
        val course: String,
        val room: String,
        val startMinutes: Int?,
        val isOver: Boolean
    ) : TodayEntry

    data class TaskDue(val title: String, val isDone: Boolean) : TodayEntry
}
