package com.dlsu.unisync.models

// Live check-in activity for one room, aggregated across accounts for the
// current hour. This is measured activity, not true occupancy: the app can only
// know who scanned in, so the UI says "checked in this hour" rather than
// implying a headcount.
data class CrowdReading(
    val id: String,
    val room: String,
    val count: Int
) {
    val level: StatusLevel get() = levelFor(count)

    // Scaled against the busy threshold so the bar fills as a room gets busier
    // and pins at full rather than overflowing.
    val progressPercent: Int
        get() = (count * 100 / BUSY_THRESHOLD).coerceIn(0, 100)

    companion object {
        const val MODERATE_THRESHOLD = 8
        const val BUSY_THRESHOLD = 20

        // Shared so a building's combined total is judged by the same thresholds
        // as a single room, instead of the two drifting apart.
        fun levelFor(count: Int): StatusLevel = when {
            count >= BUSY_THRESHOLD -> StatusLevel.HIGH
            count >= MODERATE_THRESHOLD -> StatusLevel.MEDIUM
            else -> StatusLevel.LOW
        }
    }
}
