package com.dlsu.unisync.models

// Task model, stored per user in Firestore (users/{uid}/tasks/{id}). Firestore's
// offline cache keeps it working without a connection, so there is no separate
// local copy. `due` is the display string; `dueAt` is the structured timestamp
// used for ordering, the overdue badge, and reminders.
data class TaskItem(
    val title: String,
    val due: String,
    val isDone: Boolean = false,
    val dueAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val id: String = ""
)

// Shared ordering: open tasks first, then soonest due date (undated last),
// then newest. Kept in one place so the Firestore repository and the test
// fake cannot drift apart.
val TASK_ORDER: Comparator<TaskItem> = compareBy<TaskItem> { it.isDone }
    .thenBy { it.dueAt == null }
    .thenBy { it.dueAt ?: Long.MAX_VALUE }
    .thenByDescending { it.createdAt }
    .thenByDescending { it.id }
