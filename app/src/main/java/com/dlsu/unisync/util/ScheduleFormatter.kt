package com.dlsu.unisync.util

import java.util.Calendar
import java.util.Locale

// Builds the display text stored on a schedule entry, e.g. "Mon/Wed • 1:00 PM".
//
// Locale.US is not an oversight: ScheduleParser has to be able to read this text
// back for rows written before schedules were structured, and a localized AM/PM
// would not survive that round trip. ScheduleFormatterTest asserts the round trip
// holds for every day/time combination the picker can produce.
object ScheduleFormatter {
    fun display(days: List<Int>, startMinutes: Int?): String {
        val dayText = days.joinToString("/") { ScheduleDays.label(it) }
        val timeText = startMinutes?.let(::formatTime)
        return listOfNotNull(dayText.takeIf { it.isNotEmpty() }, timeText).joinToString(" • ")
    }

    fun formatTime(minutesPastMidnight: Int): String {
        val hour24 = minutesPastMidnight / 60
        val minute = minutesPastMidnight % 60
        val hour12 = when (hour24 % 12) {
            0 -> 12
            else -> hour24 % 12
        }
        val marker = if (hour24 < 12) "AM" else "PM"
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, marker)
    }

    // Minutes past midnight for a picker result.
    fun minutesOf(hourOfDay: Int, minute: Int): Int = hourOfDay * 60 + minute

    fun hourOf(minutesPastMidnight: Int): Int = minutesPastMidnight / 60

    fun minuteOf(minutesPastMidnight: Int): Int = minutesPastMidnight % 60

    // Sensible default when adding a class with no time chosen yet.
    fun defaultStartMinutes(now: Calendar = Calendar.getInstance()): Int =
        (now.get(Calendar.HOUR_OF_DAY) * 60)
}
