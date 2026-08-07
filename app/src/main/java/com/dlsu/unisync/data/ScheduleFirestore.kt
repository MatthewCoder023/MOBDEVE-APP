package com.dlsu.unisync.data

import com.dlsu.unisync.models.ScheduleEntry
import com.google.firebase.firestore.DocumentSnapshot

// Explicit Firestore mapping rather than reflective POJO conversion, for the
// same reasons as tasks: stable field names and no no-arg-constructor
// requirement on the data class.
internal object ScheduleFields {
    const val COURSE = "course"
    const val SCHEDULE = "schedule"
    const val ROOM = "room"
    const val DAYS_MASK = "daysMask"
    const val START_MINUTES = "startMinutes"
    const val CREATED_AT = "createdAt"
}

internal fun ScheduleEntry.toFirestoreMap(): Map<String, Any?> = mapOf(
    ScheduleFields.COURSE to course,
    ScheduleFields.SCHEDULE to schedule,
    ScheduleFields.ROOM to room,
    ScheduleFields.DAYS_MASK to daysMask,
    ScheduleFields.START_MINUTES to startMinutes,
    ScheduleFields.CREATED_AT to createdAt
)

internal fun DocumentSnapshot.toScheduleEntry(): ScheduleEntry? {
    val course = getString(ScheduleFields.COURSE) ?: return null
    return ScheduleEntry(
        course = course,
        schedule = getString(ScheduleFields.SCHEDULE).orEmpty(),
        room = getString(ScheduleFields.ROOM).orEmpty(),
        daysMask = getLong(ScheduleFields.DAYS_MASK)?.toInt() ?: 0,
        startMinutes = getLong(ScheduleFields.START_MINUTES)?.toInt(),
        createdAt = getLong(ScheduleFields.CREATED_AT) ?: 0L,
        id = id
    )
}
