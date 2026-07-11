package com.dlsu.unisync.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Immutable task model, persisted by Room. createdAt drives newest-first
// ordering, and the id gives DiffUtil a stable identity.
@Entity(tableName = "tasks")
data class TaskItem(
    val title: String,
    val due: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
