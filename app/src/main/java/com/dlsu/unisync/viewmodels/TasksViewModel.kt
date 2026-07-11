package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.RoomTaskRepository
import com.dlsu.unisync.data.TaskRepository
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.TaskItem
import kotlinx.coroutines.launch

// Screen-state seam between the tasks UI and the data layer. Activity-scoped so
// state survives tab switches and rotation; the repository owns the data.
class TasksViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: LiveData<List<TaskItem>> = repository.tasks

    fun addTask(title: String, due: String) {
        viewModelScope.launch { repository.add(title, due) }
    }

    fun setTaskDone(task: TaskItem, done: Boolean) {
        viewModelScope.launch { repository.setDone(task.id, done) }
    }

    fun removeTask(task: TaskItem) {
        viewModelScope.launch { repository.remove(task) }
    }

    fun restoreTask(task: TaskItem) {
        viewModelScope.launch { repository.restore(task) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                TasksViewModel(RoomTaskRepository(UniSyncDatabase.getInstance(application).taskDao()))
            }
        }
    }
}
