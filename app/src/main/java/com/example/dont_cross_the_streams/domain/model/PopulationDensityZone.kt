package com.example.dont_cross_the_streams.domain.model

import java.io.Serializable

enum class UrbanLevel {
    WILDERNESS,
    RURAL,
    SUBURBAN,
    URBAN,
    METROPOLITAN
}

data class PopulationDensityZone(
    val id: String,
    val regionName: String,
    val boundingBox: BoundingBox,
    val centerLocation: GeoLocation,
    val densityScore: Double, // People per sq km
    val urbanLevel: UrbanLevel,
    val source: String = "US Census / NASA Footprint"
) : Serializable
