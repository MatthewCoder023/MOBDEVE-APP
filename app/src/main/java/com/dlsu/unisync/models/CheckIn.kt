package com.dlsu.unisync.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// A recorded QR (or simulated) attendance check-in.
@Entity(tableName = "check_ins")
data class CheckIn(
    val course: String,
    val room: String,
    val timestamp: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
