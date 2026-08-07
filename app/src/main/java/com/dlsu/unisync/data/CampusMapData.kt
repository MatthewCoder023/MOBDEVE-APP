package com.dlsu.unisync.data

import com.dlsu.unisync.models.CampusLocation

// Geometry for the hand-drawn campus map. This is not fixture data waiting for a
// backend: the coordinates describe the illustration itself, so they belong with
// the drawable.
object CampusMapData {
    // Bounds match the building blocks drawn in img_campus_map.xml, expressed in
    // that drawable's 320x220 viewport. Redrawing the illustration means
    // updating these too, or the tap targets drift off their buildings.
    val keyLocations = listOf(
        CampusLocation(
            name = "Henry Sy Sr. Hall",
            shortName = "Henry Sy",
            description = "Library, study rooms, and the AV theatre",
            left = 16f,
            top = 26f,
            right = 102f,
            bottom = 114f
        ),
        CampusLocation(
            name = "Gokongwei Hall",
            shortName = "Gokongwei",
            description = "Engineering and computer studies labs",
            left = 134f,
            top = 26f,
            right = 214f,
            bottom = 92f
        ),
        CampusLocation(
            name = "Velasco Hall",
            shortName = "Velasco",
            description = "Lecture rooms and faculty offices",
            left = 242f,
            top = 26f,
            right = 304f,
            bottom = 114f
        ),
        CampusLocation(
            name = "Andrew Building",
            shortName = "Andrew",
            description = "Science labs and the Andrew canteen",
            left = 16f,
            top = 152f,
            right = 102f,
            bottom = 210f
        ),
        CampusLocation(
            name = "St. La Salle Hall",
            shortName = "St. La Salle",
            description = "Administration offices and the chapel",
            left = 242f,
            top = 152f,
            right = 304f,
            bottom = 210f
        ),
        CampusLocation(
            name = "Central lawn",
            shortName = "Lawn",
            description = "Open green space between the halls",
            left = 132f,
            top = 152f,
            right = 214f,
            bottom = 210f
        )
    )
}
