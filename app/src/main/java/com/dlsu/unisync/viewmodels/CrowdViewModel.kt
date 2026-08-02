package com.dlsu.unisync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dlsu.unisync.data.CrowdRepository
import com.dlsu.unisync.data.FirestoreCrowdRepository
import com.dlsu.unisync.models.CrowdReading

// Exposes live crowd activity for the current hour.
class CrowdViewModel(repository: CrowdRepository) : ViewModel() {
    val readings: LiveData<List<CrowdReading>> = repository.readings

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CrowdViewModel(FirestoreCrowdRepository.create()) }
        }
    }
}
