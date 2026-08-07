package com.dlsu.unisync.util

import com.dlsu.unisync.models.CampusLocation

// Works out which building on the map a room belongs to.
//
// Rooms are typed by hand ("Gokongwei 305", "Andrew 1404", "Henry Sy Library"),
// so this matches on the leading words of a building's name rather than
// expecting an exact string. The first word has to match either way, which is
// what stops "Online" or "Home" from being placed on campus; when several
// buildings share it, the one matching the most leading words wins.
object CampusLocator {

    fun buildingFor(room: String, locations: List<CampusLocation>): CampusLocation? {
        val roomWords = words(room)
        if (roomWords.isEmpty()) return null
        return locations
            .mapNotNull { location ->
                val matched = leadingMatchCount(roomWords, words(location.name))
                if (matched == 0) null else location to matched
            }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun words(text: String): List<String> =
        text.split(' ', '\t').filter { it.isNotBlank() }.map { it.lowercase() }

    // How many words the two names share from the start. Zero unless the very
    // first word matches, so "Andrew 1404" only ever resolves to the Andrew
    // building and never to whatever happens to share a later word.
    private fun leadingMatchCount(roomWords: List<String>, locationWords: List<String>): Int {
        var matched = 0
        while (matched < roomWords.size && matched < locationWords.size &&
            roomWords[matched] == locationWords[matched]
        ) {
            matched++
        }
        return matched
    }
}
