package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dlsu.unisync.models.ScheduleEntry

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_entries ORDER BY id")
    fun getEntries(): LiveData<List<ScheduleEntry>>

    // REPLACE doubles as update (same id) and as undo re-insert.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ScheduleEntry)

    @Delete
    suspend fun delete(entry: ScheduleEntry)
}
