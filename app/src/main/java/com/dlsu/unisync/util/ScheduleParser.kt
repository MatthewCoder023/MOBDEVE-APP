package com.dlsu.unisync.util

import java.util.Calendar

// Reads the free-text schedule strings the user types ("Mon/Wed • 1:00 PM").
// The next-class lookup and the dashboard's today list both need the day and
// start time, so the rules live here once instead of being reimplemented.
object ScheduleParser {
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

    // Calendar day-of-week constants the entry repeats on. Empty when nothing in
    // the text looks like a day, which callers treat as "cannot place this one".
    fun daysOf(schedule: String): List<Int> = WORD_PATTERN.findAll(schedule)
        .mapNotNull { DAY_TOKENS[it.value.lowercase().take(3)] }
        .toList()

    fun occursOn(schedule: String, dayOfWeek: Int): Boolean = daysOf(schedule).contains(dayOfWeek)

    // Minutes past midnight of the first time in the text, or null when there is
    // no recognizable one.
    fun startMinutes(schedule: String): Int? {
        val match = TIME_PATTERN.find(schedule) ?: return null
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
