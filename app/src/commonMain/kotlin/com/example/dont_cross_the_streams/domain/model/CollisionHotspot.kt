package com.example.dont_cross_the_streams.domain.model

enum class CollisionSeverity {
    CRITICAL,
    HIGH,
    MODERATE,
    LOW
}

/**
 * A cluster of real, point-level collision reports. Hotspots are never entered by hand: they are
 * derived by [com.example.dont_cross_the_streams.data.analysis.HotspotClustering] from the reports
 * currently loaded, so every incident counted here can be traced back to [reportIds].
 */
data class CollisionHotspot(
    val id: String,
    val location: GeoLocation,
    val incidentCount: Int,
    val primarySpeciesAffected: String,
    val severity: CollisionSeverity,
    val source: String,
    val highwayOrRouteName: String? = null,
    val description: String? = null,
    val reportIds: List<String> = emptyList(),
    val firstObserved: String? = null,
    val lastObserved: String? = null
)
