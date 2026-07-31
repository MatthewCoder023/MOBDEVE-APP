package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dlsu.unisync.models.TaskItem

@Dao
interface TaskDao {
    // Open tasks first, then soonest due date (undated last), then newest.
    // id breaks ties for tasks created in the same millisecond.
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY isDone ASC,
                 CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END ASC,
                 dueAt ASC,
                 createdAt DESC,
                 id DESC
        """
    )
    fun getTasks(): LiveData<List<TaskItem>>

    // Unfinished tasks already due (or due by the given instant), for reminders.
    @Query("SELECT * FROM tasks WHERE isDone = 0 AND dueAt IS NOT NULL AND dueAt <= :until ORDER BY dueAt ASC")
    suspend fun getDueBy(until: Long): List<TaskItem>

    // REPLACE lets an undo re-insert a deleted task with its original id.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskItem)

    @Query("UPDATE tasks SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Delete
    suspend fun delete(task: TaskItem)
}
