package com.dlsu.unisync.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// A user-editable class schedule entry.
//
// `daysMask` and `startMinutes` are the real schedule: the days are a bit mask
// over Calendar's day constants and the time is minutes past midnight. Both are
// written by the picker, so a saved class can always be placed on a calendar.
//
// `schedule` is the human-readable rendering of those two fields, kept as a
// column because every list and card displays it. Rows created before schedules
// were structured have a mask of 0 and only this text; ScheduleDays falls back
// to parsing it, and the schedule screen flags the ones that cannot be read.
@Entity(tableName = "schedule_entries")
data class ScheduleEntry(
    val course: String,
    val schedule: String,
    val room: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val daysMask: Int = 0,
    val startMinutes: Int? = null
)
