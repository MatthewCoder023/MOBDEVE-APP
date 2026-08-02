package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.FirestoreCrowdRepository
import com.dlsu.unisync.data.FirestoreTaskRepository
import com.dlsu.unisync.data.RoomScheduleRepository
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.CampusAlert
import com.dlsu.unisync.models.CrowdReading
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.TaskItem
import com.dlsu.unisync.util.AlertBuilder
import com.google.firebase.auth.FirebaseAuth

// Combines tasks, schedule, and crowd activity into one feed. Any of the three
// changing rebuilds the list, so the notification centre reflects the same data
// the other screens are showing.
class NotificationsViewModel(
    tasks: LiveData<List<TaskItem>>,
    schedule: LiveData<List<ScheduleEntry>>,
    crowd: LiveData<List<CrowdReading>>
) : ViewModel() {

    private var latestTasks: List<TaskItem> = emptyList()
    private var latestSchedule: List<ScheduleEntry> = emptyList()
    private var latestCrowd: List<CrowdReading> = emptyList()

    private val _alerts = MediatorLiveData<List<CampusAlert>>().apply {
        value = emptyList()
        addSource(tasks) {
            latestTasks = it.orEmpty()
            rebuild()
        }
        addSource(schedule) {
            latestSchedule = it.orEmpty()
            rebuild()
        }
        addSource(crowd) {
            latestCrowd = it.orEmpty()
            rebuild()
        }
    }

    val alerts: LiveData<List<CampusAlert>> = _alerts

    private fun rebuild() {
        _alerts.value = AlertBuilder.build(latestTasks, latestSchedule, latestCrowd)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val tasks = userId?.let { FirestoreTaskRepository.forUser(it).tasks }
                    ?: MediatorLiveData(emptyList())
                NotificationsViewModel(
                    tasks = tasks,
                    schedule = RoomScheduleRepository(UniSyncDatabase.getInstance(application).scheduleDao()).entries,
                    crowd = FirestoreCrowdRepository.create().readings
                )
            }
        }
    }
}
