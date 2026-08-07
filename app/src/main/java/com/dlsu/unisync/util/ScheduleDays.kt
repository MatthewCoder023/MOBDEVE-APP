package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import java.util.Calendar

// Day-of-week handling for class schedules. Days are stored as a bit mask over
// Calendar's day constants so a class can meet on several days in one row.
//
// The English three-letter labels are deliberate: they are what gets written
// into the entry's display text, and what ScheduleParser reads back out of
// entries typed before schedules were structured.
object ScheduleDays {
    // Monday first: how a class timetable is read, not Calendar's Sunday-first order.
    val ORDER = listOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    )

    private val LABELS = mapOf(
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat",
        Calendar.SUNDAY to "Sun"
    )

    fun maskOf(days: Collection<Int>): Int = days.fold(0) { mask, day -> mask or (1 shl day) }

    fun fromMask(mask: Int): List<Int> = ORDER.filter { mask and (1 shl it) != 0 }

    fun label(day: Int): String = LABELS.getValue(day)
}

// The days a class meets. Entries saved through the picker carry a mask; older
// rows fall back to parsing whatever text was typed into them.
fun ScheduleEntry.meetingDays(): List<Int> =
    if (daysMask != 0) ScheduleDays.fromMask(daysMask) else ScheduleParser.daysOf(schedule)

fun ScheduleEntry.meetingStartMinutes(): Int? = startMinutes ?: ScheduleParser.startMinutes(schedule)

// False for legacy rows whose text names no recognizable day. Those entries are
// invisible to the next-class card, the Today list and notifications, so the
// schedule screen flags them instead of letting them fail quietly.
val ScheduleEntry.hasReadableSchedule: Boolean get() = meetingDays().isNotEmpty()
