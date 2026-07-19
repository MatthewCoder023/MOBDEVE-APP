package com.dlsu.unisync.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// A user-editable class schedule entry. schedule holds the day/time as text
// (e.g. "Mon/Wed • 1:00 PM"); structure it further when a calendar view needs it.
@Entity(tableName = "schedule_entries")
data class ScheduleEntry(
    val course: String,
    val schedule: String,
    val room: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
