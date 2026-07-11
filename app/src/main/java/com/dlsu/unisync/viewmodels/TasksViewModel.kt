package com.dlsu.unisync.viewmodels

import androidx.lifecycle.ViewModel
import com.dlsu.unisync.data.TaskRepository
import com.dlsu.unisync.models.TaskItem

// Screen-state seam between the tasks UI and the data layer. Activity-scoped so
// state survives tab switches and rotation; the repository decides where tasks
// actually live.
class TasksViewModel : ViewModel() {
    val tasks: MutableList<TaskItem> get() = TaskRepository.tasks

    fun addTask(task: TaskItem) = TaskRepository.addTask(task)
}
