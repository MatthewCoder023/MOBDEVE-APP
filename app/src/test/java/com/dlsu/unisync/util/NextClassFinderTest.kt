package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextClassFinderTest {

    // 2026-01-07 is a Wednesday.
    private fun wednesdayAt(hour: Int, minute: Int = 0): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.JANUARY, 7, hour, minute)
        }

    private val mobdeve = ScheduleEntry("MOBDEVE", "Mon/Wed • 1:00 PM", "Gokongwei 305", id = 1)
    private val stMath = ScheduleEntry("ST-MATH", "Friday • 10:00 AM", "Andrew 1404", id = 2)

    @Test
    fun `prefers a later class today over one later in the week`() {
        val next = NextClassFinder.findNext(listOf(mobdeve, stMath), wednesdayAt(9))

        assertEquals("MOBDEVE", next?.course)
    }

    @Test
    fun `a class already over today rolls to the next occurrence`() {
        val next = NextClassFinder.findNext(listOf(mobdeve, stMath), wednesdayAt(14))

        assertEquals("ST-MATH", next?.course)
    }

    @Test
    fun `day without a time counts from the start of that day`() {
        val saturdayOnly = ScheduleEntry("GEWORLD", "Saturday", "Online", id = 3)

        val next = NextClassFinder.findNext(listOf(saturdayOnly, stMath), wednesdayAt(9))

        assertEquals("ST-MATH", next?.course)
    }

    @Test
    fun `full day names are recognized`() {
        val next = NextClassFinder.findNext(listOf(stMath), wednesdayAt(9))

        assertEquals("ST-MATH", next?.course)
    }

    @Test
    fun `entries without a recognizable day are skipped`() {
        val vague = ScheduleEntry("ELECTIVE", "Asynchronous", "Online", id = 4)

        assertNull(NextClassFinder.findNext(listOf(vague), wednesdayAt(9)))
    }
}
