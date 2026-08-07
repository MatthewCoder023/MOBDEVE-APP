package com.dlsu.unisync.util

import com.dlsu.unisync.models.CampusLocation
import com.dlsu.unisync.models.CrowdReading
import com.dlsu.unisync.models.StatusLevel

// Check-in activity for one building, summed across every room in it.
data class BuildingActivity(val building: CampusLocation, val checkIns: Int) {
    // Judged by the same thresholds as a single room, so a building marked busy
    // means the same thing as a room marked busy.
    val level: StatusLevel get() = CrowdReading.levelFor(checkIns)
}

// Rolls per-room check-ins up to the buildings on the map.
//
// Rooms are matched to buildings by CampusLocator, the same rule the next-class
// highlight uses, so "Andrew 1404" and "Andrew canteen" both count towards the
// Andrew building and "Online" counts towards nothing.
object CrowdByBuilding {

    fun aggregate(
        readings: List<CrowdReading>,
        locations: List<CampusLocation>
    ): List<BuildingActivity> = readings
        .mapNotNull { reading ->
            CampusLocator.buildingFor(reading.room, locations)?.let { it to reading.count }
        }
        .groupBy({ it.first }, { it.second })
        .map { (building, counts) -> BuildingActivity(building, counts.sum()) }
        .sortedWith(compareByDescending<BuildingActivity> { it.checkIns }.thenBy { it.building.name })

    // Only buildings worth marking on the illustration. A quiet building is the
    // normal case and colouring it would make the map noisier without saying
    // anything.
    fun busyLevels(activity: List<BuildingActivity>): Map<String, StatusLevel> = activity
        .filter { it.level != StatusLevel.LOW }
        .associate { it.building.name to it.level }
}
