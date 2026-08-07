package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.FirestoreScheduleRepository
import com.dlsu.unisync.data.ScheduleRepository
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.util.ScheduleDays
import com.dlsu.unisync.util.ScheduleFormatter
import com.google.firebase.auth.FirebaseAuth
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

    // Keeps the original id and createdAt so an edit updates the row in place
    // rather than moving it to the end of the list.
    fun updateEntry(entry: ScheduleEntry, course: String, days: List<Int>, startMinutes: Int?, room: String) {
        viewModelScope.launch {
            repository.update(
                entryOf(course, days, startMinutes, room)
                    .copy(id = entry.id, createdAt = entry.createdAt)
            )
        }
    }

    fun removeEntry(entry: ScheduleEntry) {
        viewModelScope.launch { repository.remove(entry) }
    }

    fun restoreEntry(entry: ScheduleEntry) {
        viewModelScope.launch { repository.restore(entry) }
    }

    private fun entryOf(course: String, days: List<Int>, startMinutes: Int?, room: String) = ScheduleEntry(
        course = course,
        schedule = ScheduleFormatter.display(days, startMinutes),
        room = room,
        daysMask = ScheduleDays.maskOf(days),
        startMinutes = startMinutes
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ScheduleViewModel(scheduleRepositoryForSignedInUser())
            }
        }

        // Signed out there is no schedule to show; an empty source keeps the
        // screens rendering their empty states instead of failing.
        fun scheduleRepositoryForSignedInUser(): ScheduleRepository {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            return userId?.let(FirestoreScheduleRepository::forUser) ?: EmptyScheduleRepository
        }
    }
}

private object EmptyScheduleRepository : ScheduleRepository {
    override val entries: LiveData<List<ScheduleEntry>> = MutableLiveData(emptyList())

    override suspend fun add(entry: ScheduleEntry) = Unit

    override suspend fun update(entry: ScheduleEntry) = Unit

    override suspend fun remove(entry: ScheduleEntry) = Unit

    override suspend fun restore(entry: ScheduleEntry) = Unit
}
