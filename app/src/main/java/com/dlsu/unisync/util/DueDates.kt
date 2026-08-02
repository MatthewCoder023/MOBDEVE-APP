package com.dlsu.unisync.util

import java.util.Calendar
import java.util.TimeZone

// MaterialDatePicker hands back UTC-midnight timestamps, so every comparison
// against a due date has to happen in UTC too. Centralised here because the
// task list, the reminder worker, and the alert builder all need the same
// answer to "is this due today?".
object DueDates {
    fun todayUtcMidnight(): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun isOverdue(dueAt: Long?, today: Long = todayUtcMidnight()): Boolean =
        dueAt != null && dueAt < today

    fun isDueToday(dueAt: Long?, today: Long = todayUtcMidnight()): Boolean =
        dueAt != null && dueAt == today
}
