package com.dlsu.unisync.data

import com.dlsu.unisync.models.TaskItem
import com.google.firebase.firestore.FirebaseFirestore

// Gives a brand-new account something to look at. Called once, right after
// registration, so clearing every task later does not resurrect these.
object TaskSeeder {
    fun seedFor(userId: String) {
        val now = System.currentTimeMillis()
        val collection = FirebaseFirestore.getInstance()
            .collection("users").document(userId).collection("tasks")
        val demo = listOf(
            TaskItem("Finalize MOBDEVE wireframes", "Due tonight at 11:59 PM", createdAt = now),
            TaskItem("Read HCI chapter 6", "Due tomorrow", createdAt = now - 1),
            TaskItem("Group meeting notes", "Due Friday", isDone = true, createdAt = now - 2)
        )
        // Fire-and-forget: Firestore queues these offline and replays them, and
        // the snapshot listener picks them up whenever they land.
        demo.forEach { collection.add(it.toFirestoreMap()) }
    }
}
