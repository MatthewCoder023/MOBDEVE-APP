package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.models.TodayEntry
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayBuilderTest {

    // 2026-01-07 is a Wednesday; due dates are UTC midnight like the picker emits.
    private val today = 1_767_744_000_000L
    private val yesterday = today - DAY
    private val tomorrow = today + DAY

    private fun wednesdayAt(hour: Int): Calendar = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.JANUARY, 7, hour, 0)
    }

    private fun build(
        tasks: List<TaskItem> = emptyList(),
        schedule: List<ScheduleEntry> = emptyList(),
        hour: Int = 9
    ) = TodayBuilder.build(tasks, schedule, today, wednesdayAt(hour))

    private val mobdeve = ScheduleEntry("MOBDEVE", "Mon/Wed • 1:00 PM", "Gokongwei 305", id = "1")
    private val stMath = ScheduleEntry("ST-MATH", "Wed • 8:00 AM", "Andrew 1404", id = "2")
    private val friday = ScheduleEntry("GEWORLD", "Friday • 10:00 AM", "Online", id = "3")

    @Test
    fun `an empty day has nothing on it`() {
        assertTrue(build().isEmpty())
    }

    @Test
    fun `only todays classes appear, earliest first`() {
        val entries = build(schedule = listOf(mobdeve, friday, stMath))

        assertEquals(
            listOf("ST-MATH", "MOBDEVE"),
            entries.filterIsInstance<TodayEntry.ClassSession>().map { it.course }
        )
    }

    @Test
    fun `a class whose start time has passed is marked finished`() {
        val entries = build(schedule = listOf(stMath, mobdeve), hour = 10)
        val sessions = entries.filterIsInstance<TodayEntry.ClassSession>()

        assertTrue(sessions[0].isOver)
        assertFalse(sessions[1].isOver)
    }

    @Test
    fun `a class with no readable time sorts last and stays upcoming`() {
        val untimed = ScheduleEntry("ELECTIVE", "Wednesday", "Online", id = "4")

        val sessions = build(schedule = listOf(untimed, mobdeve), hour = 23)
            .filterIsInstance<TodayEntry.ClassSession>()

        assertEquals("MOBDEVE", sessions[0].course)
        assertEquals(TodayEntry.ClassSession("ELECTIVE", "Online", null, isOver = false), sessions[1])
    }

    @Test
    fun `only tasks due today appear`() {
        val entries = build(
            tasks = listOf(
                TaskItem("Today", "Due Jan 7", dueAt = today),
                TaskItem("Late", "Due Jan 6", dueAt = yesterday),
                TaskItem("Later", "Due Jan 8", dueAt = tomorrow),
                TaskItem("Someday", "No due date", dueAt = null)
            )
        )

        assertEquals(listOf(TodayEntry.TaskDue("Today", isDone = false)), entries)
    }

    // The dashboard is an agenda, not an alert feed: finished work stays visible
    // so the day reads as a checklist.
    @Test
    fun `completed tasks stay on the list, after the open ones`() {
        val entries = build(
            tasks = listOf(
                TaskItem("Done", "Due Jan 7", isDone = true, dueAt = today),
                TaskItem("Open", "Due Jan 7", dueAt = today)
            )
        )

        assertEquals(
            listOf(TodayEntry.TaskDue("Open", isDone = false), TodayEntry.TaskDue("Done", isDone = true)),
            entries
        )
    }

    @Test
    fun `classes come before tasks`() {
        val entries = build(
            tasks = listOf(TaskItem("Today", "Due Jan 7", dueAt = today)),
            schedule = listOf(mobdeve)
        )

        assertTrue(entries[0] is TodayEntry.ClassSession)
        assertTrue(entries[1] is TodayEntry.TaskDue)
    }

    private companion object {
        const val DAY = 24L * 60 * 60 * 1000
    }
}
