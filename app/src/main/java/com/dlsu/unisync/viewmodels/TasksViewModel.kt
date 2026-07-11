package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dlsu.unisync.data.TaskRepository
import com.dlsu.unisync.models.TaskItem

// Screen-state seam between the tasks UI and the data layer. Activity-scoped so
// state survives tab switches and rotation; the repository owns the data.
class TasksViewModel : ViewModel() {
    private val _tasks = MutableLiveData(TaskRepository.getTasks())
    val tasks: LiveData<List<TaskItem>> = _tasks

    fun addTask(title: String, due: String) {
        TaskRepository.addTask(TaskItem(title = title, due = due))
        refresh()
    }

    fun setTaskDone(task: TaskItem, done: Boolean) {
        TaskRepository.setDone(task.id, done)
        refresh()
    }

    fun removeTask(task: TaskItem) {
        TaskRepository.removeTask(task.id)
        refresh()
    }

    fun restoreTask(position: Int, task: TaskItem) {
        TaskRepository.insertTask(position, task)
        refresh()
    }

    private fun refresh() {
        _tasks.value = TaskRepository.getTasks()
    }
}
