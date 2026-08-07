package com.dlsu.unisync.models

// A building on the campus illustration. The bounds are expressed in the
// drawable's own 320x220 viewport (see res/drawable/img_campus_map.xml) so the
// map view can scale them to whatever size it is laid out at; keep them in sync
// if the illustration is redrawn.
data class CampusLocation(
    val name: String,
    val description: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    // What gets drawn on the building itself. Full names do not fit a block that
    // is 60-80 units wide, so each location carries the shortest form that still
    // identifies it. Defaults to the full name for anything that fits.
    val shortName: String = name
) {
    fun contains(x: Float, y: Float): Boolean = x >= left && x <= right && y >= top && y <= bottom
}
