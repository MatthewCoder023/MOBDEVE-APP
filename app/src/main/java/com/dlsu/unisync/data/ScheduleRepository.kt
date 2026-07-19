package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import com.dlsu.unisync.models.ScheduleEntry

// Data-layer seam for the class schedule.
interface ScheduleRepository {
    val entries: LiveData<List<ScheduleEntry>>

    suspend fun add(course: String, schedule: String, room: String)

    suspend fun update(entry: ScheduleEntry)

    suspend fun remove(entry: ScheduleEntry)

    suspend fun restore(entry: ScheduleEntry)
}

class RoomScheduleRepository(private val scheduleDao: ScheduleDao) : ScheduleRepository {
    override val entries: LiveData<List<ScheduleEntry>> = scheduleDao.getEntries()

    override suspend fun add(course: String, schedule: String, room: String) =
        scheduleDao.insert(ScheduleEntry(course = course, schedule = schedule, room = room))

    // Insert uses REPLACE, so writing an existing id updates the row in place.
    override suspend fun update(entry: ScheduleEntry) = scheduleDao.insert(entry)

    override suspend fun remove(entry: ScheduleEntry) = scheduleDao.delete(entry)

    // Undo re-insert keeps the original id, so id-ordering restores the position.
    override suspend fun restore(entry: ScheduleEntry) = scheduleDao.insert(entry)
}
