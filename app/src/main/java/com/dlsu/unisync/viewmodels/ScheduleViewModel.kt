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
import com.dlsu.unisync.util.ScheduleDays
import com.dlsu.unisync.util.ScheduleFormatter
import kotlinx.coroutines.launch

// Screen-state seam for the editable class schedule.
class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {
    val entries: LiveData<List<ScheduleEntry>> = repository.entries

    // days is a list of Calendar day constants; startMinutes is minutes past
    // midnight, or null when the class has no set time. The display text is
    // derived from both so it can never disagree with them.
    fun addEntry(course: String, days: List<Int>, startMinutes: Int?, room: String) {
        viewModelScope.launch { repository.add(entryOf(course, days, startMinutes, room)) }
    }

    fun updateEntry(entry: ScheduleEntry, course: String, days: List<Int>, startMinutes: Int?, room: String) {
        viewModelScope.launch {
            repository.update(entryOf(course, days, startMinutes, room).copy(id = entry.id))
        }
    }

    private fun entryOf(course: String, days: List<Int>, startMinutes: Int?, room: String) = ScheduleEntry(
        course = course,
        schedule = ScheduleFormatter.display(days, startMinutes),
        room = room,
        daysMask = ScheduleDays.maskOf(days),
        startMinutes = startMinutes
    )

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
