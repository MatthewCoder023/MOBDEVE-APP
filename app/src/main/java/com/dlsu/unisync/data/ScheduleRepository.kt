package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import com.dlsu.unisync.models.SCHEDULE_ORDER
import com.dlsu.unisync.models.ScheduleEntry
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

// Data-layer seam for the class schedule. Production talks to Firestore; tests
// substitute an in-memory fake.
interface ScheduleRepository {
    val entries: LiveData<List<ScheduleEntry>>

    suspend fun add(entry: ScheduleEntry)

    suspend fun update(entry: ScheduleEntry)

    suspend fun remove(entry: ScheduleEntry)

    suspend fun restore(entry: ScheduleEntry)
}

// Classes live under users/{uid}/schedule, the same shape tasks use, so an
// account sees its own schedule on any device it signs in to. Firestore's
// offline cache serves reads without a connection and replays writes when one
// returns.
class FirestoreScheduleRepository(private val collection: CollectionReference) : ScheduleRepository {

    override val entries: LiveData<List<ScheduleEntry>> = ScheduleLiveData(collection)

    override suspend fun add(entry: ScheduleEntry) {
        collection.add(entry.toFirestoreMap()).await()
    }

    override suspend fun update(entry: ScheduleEntry) {
        collection.document(entry.id).set(entry.toFirestoreMap()).await()
    }

    override suspend fun remove(entry: ScheduleEntry) {
        collection.document(entry.id).delete().await()
    }

    // Re-writing the original document id and createdAt puts an undone delete
    // back in its previous position.
    override suspend fun restore(entry: ScheduleEntry) {
        collection.document(entry.id).set(entry.toFirestoreMap()).await()
    }

    companion object {
        private const val USERS = "users"
        private const val SCHEDULE = "schedule"

        fun forUser(userId: String): FirestoreScheduleRepository = FirestoreScheduleRepository(
            FirebaseFirestore.getInstance().collection(USERS).document(userId).collection(SCHEDULE)
        )
    }
}

// Streams the collection while something is observing, and detaches when not.
// Sorting is client-side so no composite Firestore index is required.
private class ScheduleLiveData(
    private val collection: CollectionReference
) : LiveData<List<ScheduleEntry>>() {
    private var registration: ListenerRegistration? = null

    override fun onActive() {
        registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            value = snapshot.documents.mapNotNull { it.toScheduleEntry() }.sortedWith(SCHEDULE_ORDER)
        }
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
    }
}
