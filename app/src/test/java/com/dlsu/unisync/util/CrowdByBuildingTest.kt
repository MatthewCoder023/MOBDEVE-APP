package com.dlsu.unisync.util

import com.dlsu.unisync.data.CampusMapData
import com.dlsu.unisync.models.CrowdReading
import com.dlsu.unisync.models.StatusLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrowdByBuildingTest {

    private val locations = CampusMapData.keyLocations

    private fun reading(room: String, count: Int) = CrowdReading(id = room, room = room, count = count)

    @Test
    fun `rooms in the same building are added together`() {
        val activity = CrowdByBuilding.aggregate(
            listOf(reading("Andrew 1404", 5), reading("Andrew canteen", 7)),
            locations
        )

        assertEquals(1, activity.size)
        assertEquals("Andrew Building", activity.single().building.name)
        assertEquals(12, activity.single().checkIns)
    }

    @Test
    fun `busiest building comes first`() {
        val activity = CrowdByBuilding.aggregate(
            listOf(
                reading("Velasco 201", 3),
                reading("Henry Sy Library", 30),
                reading("Gokongwei 305", 12)
            ),
            locations
        )

        assertEquals(
            listOf("Henry Sy Sr. Hall", "Gokongwei Hall", "Velasco Hall"),
            activity.map { it.building.name }
        )
    }

    @Test
    fun `a room that is not on the map counts towards nothing`() {
        val activity = CrowdByBuilding.aggregate(listOf(reading("Online", 40)), locations)

        assertTrue(activity.isEmpty())
    }

    @Test
    fun `no readings means no activity`() {
        assertTrue(CrowdByBuilding.aggregate(emptyList(), locations).isEmpty())
    }

    // A building's total is judged like a room's, so the same number means the
    // same thing wherever it is shown.
    @Test
    fun `combined rooms can push a building past the busy threshold`() {
        val activity = CrowdByBuilding.aggregate(
            listOf(
                reading("Andrew 1404", CrowdReading.BUSY_THRESHOLD - 1),
                reading("Andrew canteen", 1)
            ),
            locations
        )

        assertEquals(StatusLevel.HIGH, activity.single().level)
    }

    @Test
    fun `only buildings above quiet are marked on the map`() {
        val activity = CrowdByBuilding.aggregate(
            listOf(
                reading("Velasco 201", 1),
                reading("Gokongwei 305", CrowdReading.MODERATE_THRESHOLD),
                reading("Henry Sy Library", CrowdReading.BUSY_THRESHOLD)
            ),
            locations
        )

        val marked = CrowdByBuilding.busyLevels(activity)

        assertFalse("a quiet building should not be coloured", marked.containsKey("Velasco Hall"))
        assertEquals(StatusLevel.MEDIUM, marked["Gokongwei Hall"])
        assertEquals(StatusLevel.HIGH, marked["Henry Sy Sr. Hall"])
    }
}
