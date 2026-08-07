package com.dlsu.unisync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dlsu.unisync.models.LegacyScheduleEntry

// Read-and-clear only: the schedule lives in Firestore now, and this table
// exists just long enough to hand its rows over. Insert is here for the tests
// that build a pre-upgrade database.
@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_entries ORDER BY id")
    suspend fun getAll(): List<LegacyScheduleEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LegacyScheduleEntry)

    @Query("DELETE FROM schedule_entries")
    suspend fun clear()
}
