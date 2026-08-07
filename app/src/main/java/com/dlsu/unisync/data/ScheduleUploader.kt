package com.dlsu.unisync.data

import com.dlsu.unisync.models.LegacyScheduleEntry
import com.dlsu.unisync.models.ScheduleEntry

// Moves any classes still sitting in the old device-local table into the
// signed-in account, once.
//
// Without this, upgrading would look exactly like the data loss the move to
// Firestore is meant to prevent: the schedule screen would come back empty and
// the classes would still be on the handset, just unreachable. Rows are only
// cleared locally after the upload succeeds, so a failure here leaves them to be
// retried on the next launch rather than dropping them.
object ScheduleUploader {

    suspend fun uploadLocalEntries(dao: ScheduleDao, repository: ScheduleRepository) {
        val local = dao.getAll()
        if (local.isEmpty()) return
        local.forEach { repository.add(it.toScheduleEntry()) }
        dao.clear()
    }
}

internal fun LegacyScheduleEntry.toScheduleEntry(): ScheduleEntry = ScheduleEntry(
    course = course,
    schedule = schedule,
    room = room,
    daysMask = daysMask,
    startMinutes = startMinutes,
    // Row order in the old table was its insertion order, so keep it by spacing
    // the timestamps by id rather than stamping them all with "now".
    createdAt = id
)
