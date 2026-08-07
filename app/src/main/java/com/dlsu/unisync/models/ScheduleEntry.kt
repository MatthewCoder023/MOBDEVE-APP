package com.dlsu.unisync.models

// A user-editable class, stored per account in Firestore
// (users/{uid}/schedule/{id}) alongside tasks, so a schedule follows the person
// rather than the handset. Firestore's offline cache keeps it readable without
// a connection, so there is no local mirror to reconcile.
//
// `daysMask` and `startMinutes` are the real schedule: the days are a bit mask
// over Calendar's day constants and the time is minutes past midnight. Both are
// written by the picker, so a saved class can always be placed on a calendar.
//
// `schedule` is the human-readable rendering of those two fields, stored because
// every list and card displays it. Entries created before schedules were
// structured have a mask of 0 and only this text; ScheduleDays falls back to
// parsing it, and the schedule screen flags the ones that cannot be read.
data class ScheduleEntry(
    val course: String,
    val schedule: String,
    val room: String,
    val daysMask: Int = 0,
    val startMinutes: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val id: String = ""
)

// Oldest first, which is the order the list has always been read in. Kept in one
// place so the Firestore repository and the test fake cannot drift apart.
val SCHEDULE_ORDER: Comparator<ScheduleEntry> = compareBy<ScheduleEntry> { it.createdAt }
    .thenBy { it.id }
