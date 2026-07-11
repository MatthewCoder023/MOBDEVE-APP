package com.dlsu.unisync.data

import com.dlsu.unisync.models.TaskItem

// In-memory single source of truth for tasks. Swap the internals for Room or
// Firebase later; screens only reach this through TasksViewModel.
object TaskRepository {
    val tasks = mutableListOf(
        TaskItem("Finalize MOBDEVE wireframes", "Due tonight at 11:59 PM"),
        TaskItem("Read HCI chapter 6", "Due tomorrow"),
        TaskItem("Group meeting notes", "Due Friday", true)
    )

    fun addTask(task: TaskItem) {
        tasks.add(0, task)
    }
}
