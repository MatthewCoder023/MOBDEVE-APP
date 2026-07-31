package com.dlsu.unisync.data

import com.dlsu.unisync.models.SimpleItem
import com.dlsu.unisync.models.StatusLevel

// In-memory dummy content for the read-only screens. Fixture text intentionally
// stays in code rather than strings.xml because a real backend will replace it.
object CampusRepository {
    val dashboardUpdates = listOf(
        SimpleItem("CCPROG3 quiz", "Due today at 5:00 PM"),
        SimpleItem("Library reservation", "Henry Sy discussion room at 3:30 PM"),
        SimpleItem("Campus advisory", "North gate lines are currently light")
    )

    val crowdLevels = listOf(
        SimpleItem("Henry Sy Library", "Moderate • 62% capacity", 62, StatusLevel.MEDIUM),
        SimpleItem("Agno Food Court", "High • 84% capacity", 84, StatusLevel.HIGH),
        SimpleItem("Gokongwei Lobby", "Light • 28% capacity", 28, StatusLevel.LOW),
        SimpleItem("Velasco Hall", "Moderate • 51% capacity", 51, StatusLevel.MEDIUM)
    )

    val notifications = listOf(
        SimpleItem("Deadline reminder", "MOBDEVE prototype draft is due today."),
        SimpleItem("Room update", "CCAPDEV moved to Velasco 202 for this week."),
        SimpleItem("Crowd alert", "Agno is busy. Consider checking nearby options.")
    )
}
