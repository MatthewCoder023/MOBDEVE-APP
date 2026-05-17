package com.dlsu.unisync.models

// Draft task model for the prototype task tracker.
data class TaskItem(
    val title: String,
    val due: String,
    var isDone: Boolean = false
)
