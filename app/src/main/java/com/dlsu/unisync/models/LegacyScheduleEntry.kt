package com.dlsu.unisync.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// The schedule as it was stored before it moved to Firestore: a local Room
// table, one database per device.
//
// Nothing writes this table any more. It stays only so that classes someone
// entered on this device can be uploaded to their account the next time they
// sign in -- see ScheduleUploader, which clears the table once they are safely
// in Firestore. The columns must keep matching the v5 schema, or Room's
// validation fails on launch.
@Entity(tableName = "schedule_entries")
data class LegacyScheduleEntry(
    val course: String,
    val schedule: String,
    val room: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val daysMask: Int = 0,
    val startMinutes: Int? = null
)
