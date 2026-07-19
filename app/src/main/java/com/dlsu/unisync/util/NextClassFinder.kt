package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import java.util.Calendar

// Best-effort "next class" from free-text schedule strings like
// "Mon/Wed • 1:00 PM". Entries whose day can't be recognized are skipped;
// entries without a recognizable time count from the start of that day.
object NextClassFinder {
    private val DAY_TOKENS = mapOf(
        "mon" to Calendar.MONDAY,
        "tue" to Calendar.TUESDAY,
        "wed" to Calendar.WEDNESDAY,
        "thu" to Calendar.THURSDAY,
        "fri" to Calendar.FRIDAY,
        "sat" to Calendar.SATURDAY,
        "sun" to Calendar.SUNDAY
    )
    private val TIME_PATTERN = Regex("""(\d{1,2}):(\d{2})\s*([AaPp][Mm])?""")
    private val WORD_PATTERN = Regex("""[A-Za-z]+""")
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
        val days = WORD_PATTERN.findAll(entry.schedule)
            .mapNotNull { DAY_TOKENS[it.value.lowercase().take(3)] }
            .toList()
        if (days.isEmpty()) return null
        val classMinutes = parseTimeMinutes(entry.schedule) ?: 0
        return days.minOf { day ->
            val dayDiff = (day - nowDow + 7) % 7
            val delta = dayDiff * DAY_MINUTES + (classMinutes - nowMinutes)
            if (delta <= 0) delta + WEEK_MINUTES else delta
        }
    }

    private fun parseTimeMinutes(text: String): Int? {
        val match = TIME_PATTERN.find(text) ?: return null
        var hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()
        when (match.groupValues[3].uppercase()) {
            "PM" -> if (hour != 12) hour += 12
            "AM" -> if (hour == 12) hour = 0
        }
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }
}
