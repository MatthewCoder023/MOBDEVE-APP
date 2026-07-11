package com.dlsu.unisync.data

import com.dlsu.unisync.models.TaskItem

// In-memory single source of truth for tasks. Swap the internals for Room or
// Firebase later; screens only reach this through TasksViewModel.
object TaskRepository {
    private val tasks = mutableListOf<TaskItem>()

    init {
        reset()
    }

    fun getTasks(): List<TaskItem> = tasks.toList()

    fun addTask(task: TaskItem) {
        tasks.add(0, task)
    }

    fun insertTask(position: Int, task: TaskItem) {
        tasks.add(position.coerceIn(0, tasks.size), task)
    }

    fun removeTask(id: Long) {
        tasks.removeAll { it.id == id }
    }

    fun setDone(id: Long, done: Boolean) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index >= 0) tasks[index] = tasks[index].copy(isDone = done)
    }

    // Restores the seed data; also used by unit tests to isolate cases.
    fun reset() {
        tasks.clear()
        tasks.addAll(
            listOf(
                TaskItem("Finalize MOBDEVE wireframes", "Due tonight at 11:59 PM"),
                TaskItem("Read HCI chapter 6", "Due tomorrow"),
                TaskItem("Group meeting notes", "Due Friday", isDone = true)
            )
        )
    }
}
