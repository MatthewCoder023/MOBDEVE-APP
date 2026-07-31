package com.dlsu.unisync.data

import com.dlsu.unisync.models.TaskItem
import com.google.firebase.firestore.DocumentSnapshot

// Explicit Firestore mapping rather than reflective POJO conversion: it keeps
// field names stable (Firestore's bean rules would rename `isDone` to `done`)
// and avoids the no-arg-constructor requirements on data classes.
internal object TaskFields {
    const val TITLE = "title"
    const val DUE = "due"
    const val IS_DONE = "isDone"
    const val DUE_AT = "dueAt"
    const val CREATED_AT = "createdAt"
}

internal fun TaskItem.toFirestoreMap(): Map<String, Any?> = mapOf(
    TaskFields.TITLE to title,
    TaskFields.DUE to due,
    TaskFields.IS_DONE to isDone,
    TaskFields.DUE_AT to dueAt,
    TaskFields.CREATED_AT to createdAt
)

internal fun DocumentSnapshot.toTaskItem(): TaskItem? {
    val title = getString(TaskFields.TITLE) ?: return null
    return TaskItem(
        title = title,
        due = getString(TaskFields.DUE).orEmpty(),
        isDone = getBoolean(TaskFields.IS_DONE) ?: false,
        dueAt = getLong(TaskFields.DUE_AT),
        createdAt = getLong(TaskFields.CREATED_AT) ?: 0L,
        id = id
    )
}
