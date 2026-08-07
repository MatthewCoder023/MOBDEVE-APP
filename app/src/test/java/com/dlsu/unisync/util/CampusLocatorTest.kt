package com.dlsu.unisync.util

import com.dlsu.unisync.data.CampusMapData
import com.dlsu.unisync.models.CampusLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CampusLocatorTest {

    private val locations = CampusMapData.keyLocations

    private fun building(room: String) = CampusLocator.buildingFor(room, locations)?.name

    @Test
    fun `a room number resolves to its building`() {
        assertEquals("Gokongwei Hall", building("Gokongwei 305"))
        assertEquals("Andrew Building", building("Andrew 1404"))
        assertEquals("Velasco Hall", building("Velasco 201"))
    }

    @Test
    fun `a named space inside a building resolves to it`() {
        assertEquals("Henry Sy Sr. Hall", building("Henry Sy Library"))
    }

    @Test
    fun `the building's own name resolves to itself`() {
        locations.forEach { location ->
            assertEquals(location.name, building(location.name))
        }
    }

    @Test
    fun `matching ignores case and stray spacing`() {
        assertEquals("Gokongwei Hall", building("  gokongwei 305  "))
        assertEquals("Velasco Hall", building("VELASCO 201"))
    }

    // "Online" is a real room in the seedless app; placing it on the campus map
    // would be worse than showing nothing.
    @Test
    fun `a room that is not on campus resolves to nothing`() {
        assertNull(building("Online"))
        assertNull(building("Home"))
        assertNull(building(""))
        assertNull(building("   "))
    }

    // Only the leading words count, so a room never lands on a building that
    // merely shares a word further along its name.
    @Test
    fun `a shared later word does not create a match`() {
        val halls = listOf(
            CampusLocation("Gokongwei Hall", "", 0f, 0f, 1f, 1f),
            CampusLocation("Velasco Hall", "", 0f, 0f, 1f, 1f)
        )

        assertNull(CampusLocator.buildingFor("Hall 12", halls))
    }

    @Test
    fun `the building matching the most leading words wins`() {
        val overlapping = listOf(
            CampusLocation("Henry Sy Sr. Hall", "", 0f, 0f, 1f, 1f),
            CampusLocation("Henry Yuchengco Hall", "", 0f, 0f, 1f, 1f)
        )

        assertEquals(
            "Henry Yuchengco Hall",
            CampusLocator.buildingFor("Henry Yuchengco 210", overlapping)?.name
        )
    }
}
