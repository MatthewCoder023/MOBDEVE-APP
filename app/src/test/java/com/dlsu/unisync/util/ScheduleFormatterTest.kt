package com.dlsu.unisync.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleFormatterTest {

    @Test
    fun `days are joined in the order given`() {
        val text = ScheduleFormatter.display(listOf(Calendar.MONDAY, Calendar.WEDNESDAY), 13 * 60)

        assertEquals("Mon/Wed • 1:00 PM", text)
    }

    @Test
    fun `a class with no time shows only its days`() {
        assertEquals("Sat", ScheduleFormatter.display(listOf(Calendar.SATURDAY), null))
    }

    @Test
    fun `noon and midnight read the way people write them`() {
        assertEquals("Mon • 12:00 PM", ScheduleFormatter.display(listOf(Calendar.MONDAY), 12 * 60))
        assertEquals("Mon • 12:00 AM", ScheduleFormatter.display(listOf(Calendar.MONDAY), 0))
    }

    @Test
    fun `minutes are zero padded`() {
        assertEquals("Tue • 9:05 AM", ScheduleFormatter.display(listOf(Calendar.TUESDAY), 9 * 60 + 5))
    }

    // The guard that matters: the display text is what older rows are parsed
    // out of, so anything the picker can produce has to survive the round trip.
    // If this breaks, classes silently stop appearing in reminders.
    @Test
    fun `every day and time the picker can produce parses back to itself`() {
        for (day in ScheduleDays.ORDER) {
            for (minutes in 0 until 24 * 60 step 5) {
                val text = ScheduleFormatter.display(listOf(day), minutes)

                assertEquals("days for '$text'", listOf(day), ScheduleParser.daysOf(text))
                assertEquals("time for '$text'", minutes, ScheduleParser.startMinutes(text))
            }
        }
    }

    @Test
    fun `multi-day text parses back to every day it names`() {
        val days = listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)

        val text = ScheduleFormatter.display(days, 7 * 60 + 30)

        assertEquals(days, ScheduleParser.daysOf(text))
        assertEquals(7 * 60 + 30, ScheduleParser.startMinutes(text))
    }

    @Test
    fun `minutes convert to and from picker fields`() {
        val minutes = ScheduleFormatter.minutesOf(hourOfDay = 14, minute = 45)

        assertEquals(14 * 60 + 45, minutes)
        assertEquals(14, ScheduleFormatter.hourOf(minutes))
        assertEquals(45, ScheduleFormatter.minuteOf(minutes))
    }

    @Test
    fun `the default start time is a whole hour within the day`() {
        val default = ScheduleFormatter.defaultStartMinutes()

        assertTrue(default in 0 until 24 * 60)
        assertEquals(0, default % 60)
    }
}
