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
    // id breaks ties for tasks created in the same millisecond.
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC, id DESC")
    fun getTasks(): LiveData<List<TaskItem>>

    // REPLACE lets an undo re-insert a deleted task with its original id.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskItem)

    @Query("UPDATE tasks SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Delete
    suspend fun delete(task: TaskItem)
}
