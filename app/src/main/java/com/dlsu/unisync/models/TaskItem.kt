package com.dlsu.unisync.models

import java.util.concurrent.atomic.AtomicLong

// Immutable task model. State changes create copies inside TaskRepository; the
// id gives DiffUtil a stable identity even when titles repeat.
data class TaskItem(
    val title: String,
    val due: String,
    val isDone: Boolean = false,
    val id: Long = nextId()
) {
    private companion object {
        val counter = AtomicLong()
        fun nextId() = counter.incrementAndGet()
    }
}
