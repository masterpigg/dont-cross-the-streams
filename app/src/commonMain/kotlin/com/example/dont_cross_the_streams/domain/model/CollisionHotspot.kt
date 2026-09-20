package com.example.dont_cross_the_streams.domain.model

enum class CollisionSeverity {
    CRITICAL,
    HIGH,
    MODERATE,
    LOW
}

data class CollisionHotspot(
    val id: String,
    val location: GeoLocation,
    val incidentCount: Int,
    val primarySpeciesAffected: String,
    val severity: CollisionSeverity,
    val source: String,
    val highwayOrRouteName: String? = null,
    val description: String? = null
)
