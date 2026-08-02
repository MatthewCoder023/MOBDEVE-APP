package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import com.dlsu.unisync.models.CrowdReading
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.tasks.await

// Crowd levels are derived from real QR check-ins rather than fixture numbers.
// Counts are aggregated per room per hour in a shared `crowd` collection: only
// a running total is stored, never who checked in, so no one's movements are
// exposed to other accounts.
interface CrowdRepository {
    val readings: LiveData<List<CrowdReading>>

    suspend fun recordCheckIn(room: String)
}

class FirestoreCrowdRepository(private val collection: CollectionReference) : CrowdRepository {

    override val readings: LiveData<List<CrowdReading>> = CrowdLiveData(collection)

    // A transaction because several people can scan into the same room at once;
    // a plain read-then-write would lose increments.
    override suspend fun recordCheckIn(room: String) {
        val document = collection.document(slugOf(room))
        val hourKey = currentHourKey()
        collection.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            // A stale hourKey means the previous hour's tally; start over.
            val current = if (snapshot.getString(FIELD_HOUR_KEY) == hourKey) {
                snapshot.getLong(FIELD_COUNT) ?: 0L
            } else {
                0L
            }
            transaction.set(
                document,
                mapOf(
                    FIELD_ROOM to room,
                    FIELD_COUNT to current + 1,
                    FIELD_HOUR_KEY to hourKey,
                    FIELD_UPDATED_AT to System.currentTimeMillis()
                )
            )
        }.await()
    }

    companion object {
        const val COLLECTION = "crowd"
        const val FIELD_ROOM = "room"
        const val FIELD_COUNT = "count"
        const val FIELD_HOUR_KEY = "hourKey"
        const val FIELD_UPDATED_AT = "updatedAt"

        fun create(): FirestoreCrowdRepository =
            FirestoreCrowdRepository(FirebaseFirestore.getInstance().collection(COLLECTION))

        // UTC so every device agrees on which hour a check-in belongs to.
        fun currentHourKey(): String {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.format(java.util.Date())
        }

        // Firestore document ids cannot contain '/', so rooms are slugified.
        fun slugOf(room: String): String = room.trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "unknown" }
    }
}

// Only the current hour is shown; older documents are simply ignored rather
// than deleted, so a room that goes quiet drops off on its own.
private class CrowdLiveData(
    private val collection: CollectionReference
) : LiveData<List<CrowdReading>>() {
    private var registration: ListenerRegistration? = null

    override fun onActive() {
        registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val hourKey = FirestoreCrowdRepository.currentHourKey()
            value = snapshot.documents
                .filter { it.getString(FirestoreCrowdRepository.FIELD_HOUR_KEY) == hourKey }
                .mapNotNull { document ->
                    val room = document.getString(FirestoreCrowdRepository.FIELD_ROOM) ?: return@mapNotNull null
                    val count = document.getLong(FirestoreCrowdRepository.FIELD_COUNT)?.toInt() ?: 0
                    if (count <= 0) null else CrowdReading(document.id, room, count)
                }
                .sortedByDescending { it.count }
        }
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
    }
}
