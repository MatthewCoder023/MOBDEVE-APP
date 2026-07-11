package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import com.dlsu.unisync.models.TaskItem

// Data-layer seam for tasks. Production uses Room; unit tests substitute an
// in-memory fake. A future cloud backend slots in as another implementation.
interface TaskRepository {
    val tasks: LiveData<List<TaskItem>>

    suspend fun add(title: String, due: String)

    suspend fun setDone(id: Long, done: Boolean)

    suspend fun remove(task: TaskItem)

    suspend fun restore(task: TaskItem)
}

class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {
    override val tasks: LiveData<List<TaskItem>> = taskDao.getTasks()

    override suspend fun add(title: String, due: String) = taskDao.insert(TaskItem(title = title, due = due))

    override suspend fun setDone(id: Long, done: Boolean) = taskDao.setDone(id, done)

    override suspend fun remove(task: TaskItem) = taskDao.delete(task)

    // Re-inserting with the original id and createdAt puts the task back in
    // its old position, since the list is ordered by createdAt.
    override suspend fun restore(task: TaskItem) = taskDao.insert(task)
}
