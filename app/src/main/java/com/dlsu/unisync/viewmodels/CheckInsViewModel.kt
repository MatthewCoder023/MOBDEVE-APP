package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.CheckInRepository
import com.dlsu.unisync.data.CrowdRepository
import com.dlsu.unisync.data.FirestoreCrowdRepository
import com.dlsu.unisync.data.RoomCheckInRepository
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.CheckIn
import kotlinx.coroutines.launch

// Holds the personal check-in history and records new check-ins. Each check-in
// is written twice on purpose: the detailed record stays on this device, while
// only an anonymous per-room counter goes to the shared crowd collection.
class CheckInsViewModel(
    private val repository: CheckInRepository,
    private val crowdRepository: CrowdRepository
) : ViewModel() {
    val checkIns: LiveData<List<CheckIn>> = repository.recentCheckIns

    fun addCheckIn(course: String, room: String) {
        viewModelScope.launch {
            repository.add(CheckIn(course = course, room = room))
            runCatching { crowdRepository.recordCheckIn(room) }
                .onFailure {
                    // The personal record already succeeded; a failed crowd
                    // update should not surface as a failed check-in.
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                CheckInsViewModel(
                    RoomCheckInRepository(UniSyncDatabase.getInstance(application).checkInDao()),
                    FirestoreCrowdRepository.create()
                )
            }
        }
    }
}
