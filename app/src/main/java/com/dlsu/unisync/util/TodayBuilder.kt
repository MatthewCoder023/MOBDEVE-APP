package com.dlsu.unisync.util

import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.models.TodayEntry
import java.util.Calendar

// Builds the dashboard's agenda for today: the classes that meet today in
// chronological order, then the tasks due today. Pure so the ordering and
// filtering rules can be tested without Firestore, Room, or a device clock.
object TodayBuilder {
    fun build(
        tasks: List<TaskItem>,
        schedule: List<ScheduleEntry>,
        today: Long = DueDates.todayUtcMidnight(),
        now: Calendar = Calendar.getInstance()
    ): List<TodayEntry> {
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val classes = schedule
            .filter { ScheduleParser.occursOn(it.schedule, dayOfWeek) }
            .map { entry ->
                val start = ScheduleParser.startMinutes(entry.schedule)
                TodayEntry.ClassSession(
                    course = entry.course,
                    room = entry.room,
                    startMinutes = start,
                    // Without a time there is no way to tell, so it stays upcoming.
                    isOver = start != null && start < nowMinutes
                )
            }
            // Classes with no recognizable time sort to the end of the day.
            .sortedBy { it.startMinutes ?: Int.MAX_VALUE }

        val dueToday = tasks
            .filter { DueDates.isDueToday(it.dueAt, today) }
            .sortedBy { it.isDone }
            .map { TodayEntry.TaskDue(it.title, it.isDone) }

        return classes + dueToday
    }
}
