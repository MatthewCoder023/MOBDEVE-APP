package com.dlsu.unisync.models

// Small display model reused for dashboard notes, schedules, crowd data, and alerts.
// progressPercent/level are optional: when set, the card shows a colored
// occupancy bar instead of relying on the subtitle text alone.
data class SimpleItem(
    val title: String,
    val subtitle: String,
    val progressPercent: Int? = null,
    val level: StatusLevel? = null
)

enum class StatusLevel { LOW, MEDIUM, HIGH }
