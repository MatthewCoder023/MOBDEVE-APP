package com.dlsu.unisync.data

import com.dlsu.unisync.models.CampusLocation
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

    // Bounds match the building blocks drawn in img_campus_map.xml, expressed in
    // that drawable's 320x220 viewport. Redrawing the illustration means
    // updating these too, or the tap targets drift off their buildings.
    val keyLocations = listOf(
        CampusLocation(
            name = "Henry Sy Sr. Hall",
            description = "Library, study rooms, and the AV theatre",
            left = 16f,
            top = 26f,
            right = 102f,
            bottom = 114f
        ),
        CampusLocation(
            name = "Gokongwei Hall",
            description = "Engineering and computer studies labs",
            left = 134f,
            top = 26f,
            right = 214f,
            bottom = 92f
        ),
        CampusLocation(
            name = "Velasco Hall",
            description = "Lecture rooms and faculty offices",
            left = 242f,
            top = 26f,
            right = 304f,
            bottom = 114f
        ),
        CampusLocation(
            name = "Andrew Building",
            description = "Science labs and the Andrew canteen",
            left = 16f,
            top = 152f,
            right = 102f,
            bottom = 210f
        ),
        CampusLocation(
            name = "St. La Salle Hall",
            description = "Administration offices and the chapel",
            left = 242f,
            top = 152f,
            right = 304f,
            bottom = 210f
        ),
        CampusLocation(
            name = "Central lawn",
            description = "Open green space between the halls",
            left = 132f,
            top = 152f,
            right = 214f,
            bottom = 210f
        )
    )

    val notifications = listOf(
        SimpleItem("Deadline reminder", "MOBDEVE prototype draft is due today."),
        SimpleItem("Room update", "CCAPDEV moved to Velasco 202 for this week."),
        SimpleItem("Crowd alert", "Agno is busy. Consider checking nearby options.")
    )
}
