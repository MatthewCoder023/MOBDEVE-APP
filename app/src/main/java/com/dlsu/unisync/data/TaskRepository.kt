package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import com.dlsu.unisync.models.TASK_ORDER
import com.dlsu.unisync.models.TaskItem
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

// Data-layer seam for tasks. Production talks to Firestore; unit tests
// substitute an in-memory fake.
interface TaskRepository {
    val tasks: LiveData<List<TaskItem>>

    suspend fun add(title: String, due: String, dueAt: Long?)

    suspend fun update(task: TaskItem)

    suspend fun setDone(id: String, done: Boolean)

    suspend fun remove(task: TaskItem)

    suspend fun restore(task: TaskItem)
}

// Tasks live under users/{uid}/tasks, so each account only ever sees its own.
// Firestore's offline cache serves reads without a connection and replays
// writes when one returns, which is why there is no local mirror to reconcile.
class FirestoreTaskRepository(private val collection: CollectionReference) : TaskRepository {

    override val tasks: LiveData<List<TaskItem>> = TasksLiveData(collection)

    override suspend fun add(title: String, due: String, dueAt: Long?) {
        val task = TaskItem(title = title, due = due, dueAt = dueAt)
        collection.add(task.toFirestoreMap()).await()
    }

    override suspend fun update(task: TaskItem) {
        collection.document(task.id).set(task.toFirestoreMap()).await()
    }

    override suspend fun setDone(id: String, done: Boolean) {
        collection.document(id).update(TaskFields.IS_DONE, done).await()
    }

    override suspend fun remove(task: TaskItem) {
        collection.document(task.id).delete().await()
    }

    // Re-writing the original document id and createdAt puts an undone delete
    // back in its previous position.
    override suspend fun restore(task: TaskItem) {
        collection.document(task.id).set(task.toFirestoreMap()).await()
    }

    companion object {
        private const val USERS = "users"
        private const val TASKS = "tasks"

        fun forUser(userId: String): FirestoreTaskRepository = FirestoreTaskRepository(
            FirebaseFirestore.getInstance().collection(USERS).document(userId).collection(TASKS)
        )
    }
}

// Streams the collection while something is observing, and detaches when not.
// Sorting happens client-side so no composite Firestore index is required and
// the "undated last" rule stays expressible.
private class TasksLiveData(
    private val collection: CollectionReference
) : LiveData<List<TaskItem>>() {
    private var registration: ListenerRegistration? = null

    override fun onActive() {
        registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            value = snapshot.documents.mapNotNull { it.toTaskItem() }.sortedWith(TASK_ORDER)
        }
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
    }
}
