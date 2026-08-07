package com.dlsu.unisync.util

import com.dlsu.unisync.models.CampusAlert
import com.dlsu.unisync.models.CrowdReading
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.TaskItem
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertBuilderTest {

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
        crowd: List<CrowdReading> = emptyList(),
        hour: Int = 9
    ) = AlertBuilder.build(tasks, schedule, crowd, today, wednesdayAt(hour))

    @Test
    fun `nothing to report yields no alerts`() {
        assertTrue(build().isEmpty())
    }

    @Test
    fun `overdue tasks are reported`() {
        val alerts = build(tasks = listOf(TaskItem("Wireframes", "Due Jan 6", dueAt = yesterday)))

        assertEquals(1, alerts.size)
        assertEquals(CampusAlert.OverdueTask("Wireframes", "Due Jan 6"), alerts.first())
    }

    @Test
    fun `tasks due today are reported separately from overdue ones`() {
        val alerts = build(
            tasks = listOf(
                TaskItem("Today", "Due Jan 7", dueAt = today),
                TaskItem("Late", "Due Jan 6", dueAt = yesterday)
            )
        )

        // Overdue sorts ahead of due-today.
        assertEquals(CampusAlert.OverdueTask("Late", "Due Jan 6"), alerts[0])
        assertEquals(CampusAlert.TaskDueToday("Today"), alerts[1])
    }

    @Test
    fun `future and undated tasks are not reported`() {
        val alerts = build(
            tasks = listOf(
                TaskItem("Later", "Due Jan 8", dueAt = tomorrow),
                TaskItem("Someday", "No due date", dueAt = null)
            )
        )

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `completed tasks never raise an alert`() {
        val alerts = build(
            tasks = listOf(TaskItem("Done late", "Due Jan 6", isDone = true, dueAt = yesterday))
        )

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `the next class is surfaced`() {
        val alerts = build(
            schedule = listOf(ScheduleEntry("MOBDEVE", "Mon/Wed • 1:00 PM", "Gokongwei 305", id = "1"))
        )

        assertEquals(CampusAlert.NextClass("MOBDEVE", "Mon/Wed • 1:00 PM", "Gokongwei 305"), alerts.single())
    }

    @Test
    fun `only busy rooms are surfaced, busiest first`() {
        val alerts = build(
            crowd = listOf(
                CrowdReading("quiet", "Velasco 201", count = 2),
                CrowdReading("busy", "Agno Food Court", count = 25),
                CrowdReading("busier", "Henry Sy Library", count = 40)
            )
        )

        assertEquals(
            listOf(
                CampusAlert.BusyRoom("Henry Sy Library", 40),
                CampusAlert.BusyRoom("Agno Food Court", 25)
            ),
            alerts
        )
    }

    @Test
    fun `deadlines outrank class and crowd context`() {
        val alerts = build(
            tasks = listOf(TaskItem("Late", "Due Jan 6", dueAt = yesterday)),
            schedule = listOf(ScheduleEntry("MOBDEVE", "Mon/Wed • 1:00 PM", "G305", id = "1")),
            crowd = listOf(CrowdReading("busy", "Agno", count = 30))
        )

        assertTrue(alerts[0] is CampusAlert.OverdueTask)
        assertTrue(alerts[1] is CampusAlert.NextClass)
        assertTrue(alerts[2] is CampusAlert.BusyRoom)
    }

    private companion object {
        const val DAY = 24L * 60 * 60 * 1000
    }
}
