package com.dlsu.unisync.viewmodels

import androidx.lifecycle.ViewModel
import com.dlsu.unisync.models.TaskItem

// Activity-scoped holder for task state so added tasks and checkbox states
// survive tab switches and rotation. Swap the backing list for a repository
// (Room/Firebase) later without touching the fragment.
class TasksViewModel : ViewModel() {
    val tasks = mutableListOf(
        TaskItem("Finalize MOBDEVE wireframes", "Due tonight at 11:59 PM"),
        TaskItem("Read HCI chapter 6", "Due tomorrow"),
        TaskItem("Group meeting notes", "Due Friday", true)
    )
}
