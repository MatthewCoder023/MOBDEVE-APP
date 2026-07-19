package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import com.dlsu.unisync.models.CheckIn

// Data-layer seam for attendance check-ins.
interface CheckInRepository {
    val recentCheckIns: LiveData<List<CheckIn>>

    suspend fun add(checkIn: CheckIn)
}

class RoomCheckInRepository(private val checkInDao: CheckInDao) : CheckInRepository {
    override val recentCheckIns: LiveData<List<CheckIn>> = checkInDao.getRecent()

    override suspend fun add(checkIn: CheckIn) = checkInDao.insert(checkIn)
}
