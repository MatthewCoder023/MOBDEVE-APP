package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import java.util.Calendar

// The soonest upcoming class. Entries carry their days and start time as data;
// rows saved before schedules were structured fall back to parsing their text,
// and ones whose day still can't be recognized are skipped.
object NextClassFinder {
    private const val DAY_MINUTES = 24 * 60
    private const val WEEK_MINUTES = 7 * DAY_MINUTES

    fun findNext(entries: List<ScheduleEntry>, now: Calendar = Calendar.getInstance()): ScheduleEntry? {
        val nowDow = now.get(Calendar.DAY_OF_WEEK)
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return entries
            .mapNotNull { entry -> minutesUntil(entry, nowDow, nowMinutes)?.let { entry to it } }
            .minByOrNull { it.second }
            ?.first
    }

    private fun minutesUntil(entry: ScheduleEntry, nowDow: Int, nowMinutes: Int): Int? {
        val days = entry.meetingDays()
        if (days.isEmpty()) return null
        val classMinutes = entry.meetingStartMinutes() ?: 0
        return days.minOf { day ->
            val dayDiff = (day - nowDow + 7) % 7
            val delta = dayDiff * DAY_MINUTES + (classMinutes - nowMinutes)
            if (delta <= 0) delta + WEEK_MINUTES else delta
        }
    }
}
