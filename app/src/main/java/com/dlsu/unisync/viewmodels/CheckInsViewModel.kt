package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.CheckInRepository
import com.dlsu.unisync.data.RoomCheckInRepository
import com.dlsu.unisync.data.UniSyncDatabase
import com.dlsu.unisync.models.CheckIn
import kotlinx.coroutines.launch

// Holds the recent check-in history and records new check-ins.
class CheckInsViewModel(private val repository: CheckInRepository) : ViewModel() {
    val checkIns: LiveData<List<CheckIn>> = repository.recentCheckIns

    fun addCheckIn(course: String, room: String) {
        viewModelScope.launch { repository.add(CheckIn(course = course, room = room)) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                CheckInsViewModel(RoomCheckInRepository(UniSyncDatabase.getInstance(application).checkInDao()))
            }
        }
    }
}
