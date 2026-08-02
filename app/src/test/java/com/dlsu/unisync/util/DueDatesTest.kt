package com.dlsu.unisync.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DueDatesTest {

    private val today = 1_767_744_000_000L

    @Test
    fun `a date before today is overdue`() {
        assertTrue(DueDates.isOverdue(today - DAY, today))
    }

    @Test
    fun `today is not overdue`() {
        assertFalse(DueDates.isOverdue(today, today))
    }

    @Test
    fun `a future date is not overdue`() {
        assertFalse(DueDates.isOverdue(today + DAY, today))
    }

    @Test
    fun `an undated task is neither overdue nor due today`() {
        assertFalse(DueDates.isOverdue(null, today))
        assertFalse(DueDates.isDueToday(null, today))
    }

    @Test
    fun `due today matches only todays midnight`() {
        assertTrue(DueDates.isDueToday(today, today))
        assertFalse(DueDates.isDueToday(today - DAY, today))
        assertFalse(DueDates.isDueToday(today + DAY, today))
    }

    // The picker emits UTC midnight, so the boundary must be computed in UTC or
    // "due today" would flip a day early or late depending on the time zone.
    @Test
    fun `todays midnight is a whole number of days`() {
        assertTrue(DueDates.todayUtcMidnight() % DAY == 0L)
    }

    private companion object {
        const val DAY = 24L * 60 * 60 * 1000
    }
}
