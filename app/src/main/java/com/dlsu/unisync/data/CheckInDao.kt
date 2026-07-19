package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dlsu.unisync.models.CheckIn

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY timestamp DESC, id DESC LIMIT 10")
    fun getRecent(): LiveData<List<CheckIn>>

    @Insert
    suspend fun insert(checkIn: CheckIn)
}
