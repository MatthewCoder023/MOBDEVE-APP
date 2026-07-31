package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.FirestoreTaskRepository
import com.dlsu.unisync.data.TaskRepository
import com.dlsu.unisync.models.TaskItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Screen-state seam between the tasks UI and the data layer. Activity-scoped so
// state survives tab switches and rotation; the repository owns the data.
class TasksViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: LiveData<List<TaskItem>> = repository.tasks

    fun addTask(title: String, due: String, dueAt: Long?) {
        viewModelScope.launch { repository.add(title, due, dueAt) }
    }

    fun updateTask(task: TaskItem, title: String, due: String, dueAt: Long?) {
        viewModelScope.launch { repository.update(task.copy(title = title, due = due, dueAt = dueAt)) }
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
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                // Auth gates every route into the app, so a signed-out state here
                // means the session ended mid-flight; show nothing rather than crash.
                val repository = userId?.let { FirestoreTaskRepository.forUser(it) } ?: EmptyTaskRepository
                TasksViewModel(repository)
            }
        }
    }
}

private object EmptyTaskRepository : TaskRepository {
    override val tasks: LiveData<List<TaskItem>> = MutableLiveData(emptyList())

    override suspend fun add(title: String, due: String, dueAt: Long?) = Unit

    override suspend fun update(task: TaskItem) = Unit

    override suspend fun setDone(id: String, done: Boolean) = Unit

    override suspend fun remove(task: TaskItem) = Unit

    override suspend fun restore(task: TaskItem) = Unit
}
