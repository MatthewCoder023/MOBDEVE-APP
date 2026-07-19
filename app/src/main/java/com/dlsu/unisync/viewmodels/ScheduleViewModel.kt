package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.RoomScheduleRepository
import com.dlsu.unisync.data.ScheduleRepository
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.ScheduleEntry
import kotlinx.coroutines.launch

// Screen-state seam for the editable class schedule.
class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {
    val entries: LiveData<List<ScheduleEntry>> = repository.entries

    fun addEntry(course: String, schedule: String, room: String) {
        viewModelScope.launch { repository.add(course, schedule, room) }
    }

    fun updateEntry(entry: ScheduleEntry, course: String, schedule: String, room: String) {
        viewModelScope.launch { repository.update(entry.copy(course = course, schedule = schedule, room = room)) }
    }

    fun removeEntry(entry: ScheduleEntry) {
        viewModelScope.launch { repository.remove(entry) }
    }

    fun restoreEntry(entry: ScheduleEntry) {
        viewModelScope.launch { repository.restore(entry) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                ScheduleViewModel(RoomScheduleRepository(UniSyncDatabase.getInstance(application).scheduleDao()))
            }
        }
    }
}
