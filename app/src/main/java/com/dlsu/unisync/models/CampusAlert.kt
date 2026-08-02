package com.dlsu.unisync.models

// An item in the notification centre, derived from the app's own live data
// rather than a feed someone has to publish. Structured rather than
// pre-formatted so the wording stays in string resources and the builder stays
// free of Android dependencies.
sealed interface CampusAlert {
    data class OverdueTask(val title: String, val due: String) : CampusAlert

    data class TaskDueToday(val title: String) : CampusAlert

    data class NextClass(val course: String, val schedule: String, val room: String) : CampusAlert

    data class BusyRoom(val room: String, val count: Int) : CampusAlert
}
