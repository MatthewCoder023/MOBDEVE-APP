package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleDaysTest {

    @Test
    fun `a mask round-trips back to its days`() {
        val days = listOf(Calendar.TUESDAY, Calendar.THURSDAY)

        assertEquals(days, ScheduleDays.fromMask(ScheduleDays.maskOf(days)))
    }

    @Test
    fun `days come back Monday first whatever order they went in`() {
        val mask = ScheduleDays.maskOf(listOf(Calendar.SUNDAY, Calendar.WEDNESDAY, Calendar.MONDAY))

        assertEquals(
            listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.SUNDAY),
            ScheduleDays.fromMask(mask)
        )
    }

    @Test
    fun `an empty mask has no days`() {
        assertEquals(0, ScheduleDays.maskOf(emptyList()))
        assertTrue(ScheduleDays.fromMask(0).isEmpty())
    }

    @Test
    fun `every day has a distinct bit`() {
        val masks = ScheduleDays.ORDER.map { ScheduleDays.maskOf(listOf(it)) }

        assertEquals(masks.size, masks.toSet().size)
    }

    @Test
    fun `a structured entry reads its days from the mask, not its text`() {
        // The text says Monday; the mask says Friday. The mask is the schedule.
        val entry = ScheduleEntry(
            course = "MOBDEVE",
            schedule = "Mon • 1:00 PM",
            room = "G305",
            daysMask = ScheduleDays.maskOf(listOf(Calendar.FRIDAY)),
            startMinutes = 9 * 60
        )

        assertEquals(listOf(Calendar.FRIDAY), entry.meetingDays())
        assertEquals(9 * 60, entry.meetingStartMinutes())
        assertTrue(entry.hasReadableSchedule)
    }

    @Test
    fun `an entry saved before the picker falls back to parsing its text`() {
        val legacy = ScheduleEntry("CCAPDEV", "Tue/Thu • 9:15 AM", "Velasco 201")

        assertEquals(listOf(Calendar.TUESDAY, Calendar.THURSDAY), legacy.meetingDays())
        assertEquals(9 * 60 + 15, legacy.meetingStartMinutes())
        assertTrue(legacy.hasReadableSchedule)
    }

    @Test
    fun `an entry whose text names no day is flagged as unreadable`() {
        val vague = ScheduleEntry("ELECTIVE", "Asynchronous", "Online")

        assertTrue(vague.meetingDays().isEmpty())
        assertFalse(vague.hasReadableSchedule)
    }
}
