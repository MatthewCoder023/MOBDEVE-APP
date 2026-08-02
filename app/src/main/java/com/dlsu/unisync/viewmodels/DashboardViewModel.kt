package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.FirestoreTaskRepository
import com.dlsu.unisync.data.RoomScheduleRepository
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.models.TodayEntry
import com.dlsu.unisync.util.TodayBuilder
import com.google.firebase.auth.FirebaseAuth

// Feeds the dashboard's "Today" list from the same task and schedule sources the
// Tasks and Schedule screens use, so editing either one updates the home screen.
class DashboardViewModel(
    tasks: LiveData<List<TaskItem>>,
    schedule: LiveData<List<ScheduleEntry>>
) : ViewModel() {

    private var latestTasks: List<TaskItem> = emptyList()
    private var latestSchedule: List<ScheduleEntry> = emptyList()

    private val _today = MediatorLiveData<List<TodayEntry>>().apply {
        value = emptyList()
        addSource(tasks) {
            latestTasks = it.orEmpty()
            rebuild()
        }
        addSource(schedule) {
            latestSchedule = it.orEmpty()
            rebuild()
        }
    }

    val today: LiveData<List<TodayEntry>> = _today

    private fun rebuild() {
        _today.value = TodayBuilder.build(latestTasks, latestSchedule)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val tasks = userId?.let { FirestoreTaskRepository.forUser(it).tasks }
                    ?: MediatorLiveData(emptyList())
                DashboardViewModel(
                    tasks = tasks,
                    schedule = RoomScheduleRepository(
                        UniSyncDatabase.getInstance(application).scheduleDao()
                    ).entries
                )
            }
        }
    }
}
