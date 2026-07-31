package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dlsu.unisync.models.TASK_ORDER
import com.dlsu.unisync.models.TaskItem

// In-memory TaskRepository that applies the same TASK_ORDER comparator as the
// Firestore implementation, so ViewModel tests exercise realistic ordering.
class FakeTaskRepository : TaskRepository {
    private val items = mutableListOf<TaskItem>()
    private val _tasks = MutableLiveData<List<TaskItem>>(emptyList())
    private var nextId = 1L

    override val tasks: LiveData<List<TaskItem>> = _tasks

    override suspend fun add(title: String, due: String, dueAt: Long?) {
        items.add(
            TaskItem(
                title = title,
                due = due,
                dueAt = dueAt,
                createdAt = nextId,
                // Zero-padded so lexicographic id ties break like numeric ones.
                id = nextId.toString().padStart(4, '0')
            )
        )
        nextId++
        publish()
    }

    override suspend fun update(task: TaskItem) {
        val index = items.indexOfFirst { it.id == task.id }
        if (index >= 0) items[index] = task
        publish()
    }

    override suspend fun setDone(id: String, done: Boolean) {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) items[index] = items[index].copy(isDone = done)
        publish()
    }

    override suspend fun remove(task: TaskItem) {
        items.removeAll { it.id == task.id }
        publish()
    }

    override suspend fun restore(task: TaskItem) {
        items.add(task)
        publish()
    }

    private fun publish() {
        _tasks.value = items.sortedWith(TASK_ORDER)
    }
}
