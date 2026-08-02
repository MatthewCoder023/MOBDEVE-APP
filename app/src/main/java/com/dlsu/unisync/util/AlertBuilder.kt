package com.dlsu.unisync.util

import com.dlsu.unisync.models.CampusAlert
import com.dlsu.unisync.models.CrowdReading
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.StatusLevel
import com.dlsu.unisync.models.TaskItem
import java.util.Calendar

// Turns the app's live data into the notification feed. Pure and free of
// Android types so the rules below can be unit tested directly.
object AlertBuilder {

    fun build(
        tasks: List<TaskItem>,
        schedule: List<ScheduleEntry>,
        crowd: List<CrowdReading>,
        today: Long = DueDates.todayUtcMidnight(),
        now: Calendar = Calendar.getInstance()
    ): List<CampusAlert> {
        val open = tasks.filter { !it.isDone }

        // Most urgent first: what is already late, then what is due today,
        // then context (next class, busy rooms).
        val overdue = open
            .filter { DueDates.isOverdue(it.dueAt, today) }
            .sortedBy { it.dueAt }
            .map { CampusAlert.OverdueTask(it.title, it.due) }

        val dueToday = open
            .filter { DueDates.isDueToday(it.dueAt, today) }
            .map { CampusAlert.TaskDueToday(it.title) }

        val nextClass = NextClassFinder.findNext(schedule, now)?.let {
            listOf(CampusAlert.NextClass(it.course, it.schedule, it.room))
        }.orEmpty()

        val busyRooms = crowd
            .filter { it.level == StatusLevel.HIGH }
            .sortedByDescending { it.count }
            .map { CampusAlert.BusyRoom(it.room, it.count) }

        return overdue + dueToday + nextClass + busyRooms
    }
}
