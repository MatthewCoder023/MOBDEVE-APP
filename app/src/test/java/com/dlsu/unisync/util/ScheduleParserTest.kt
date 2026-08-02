package com.dlsu.unisync.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleParserTest {

    @Test
    fun `abbreviated and full day names are both recognized`() {
        assertEquals(listOf(Calendar.FRIDAY), ScheduleParser.daysOf("Friday • 10:00 AM"))
        assertEquals(listOf(Calendar.FRIDAY), ScheduleParser.daysOf("Fri • 10:00 AM"))
    }

    @Test
    fun `every day in the text is returned`() {
        assertEquals(
            listOf(Calendar.MONDAY, Calendar.WEDNESDAY),
            ScheduleParser.daysOf("Mon/Wed • 1:00 PM")
        )
    }

    @Test
    fun `text without a day yields nothing`() {
        assertTrue(ScheduleParser.daysOf("Asynchronous").isEmpty())
    }

    @Test
    fun `occursOn matches only the listed days`() {
        assertTrue(ScheduleParser.occursOn("Mon/Wed • 1:00 PM", Calendar.WEDNESDAY))
        assertFalse(ScheduleParser.occursOn("Mon/Wed • 1:00 PM", Calendar.TUESDAY))
    }

    @Test
    fun `afternoon times are converted to a 24-hour offset`() {
        assertEquals(13 * 60, ScheduleParser.startMinutes("Mon/Wed • 1:00 PM"))
    }

    @Test
    fun `noon and midnight are handled`() {
        assertEquals(12 * 60, ScheduleParser.startMinutes("Mon • 12:00 PM"))
        assertEquals(0, ScheduleParser.startMinutes("Mon • 12:00 AM"))
    }

    @Test
    fun `times already in 24-hour form are kept`() {
        assertEquals(13 * 60 + 30, ScheduleParser.startMinutes("Mon 13:30"))
    }

    @Test
    fun `a missing or impossible time yields null`() {
        assertNull(ScheduleParser.startMinutes("Saturday"))
        assertNull(ScheduleParser.startMinutes("Mon • 25:00"))
    }
}
