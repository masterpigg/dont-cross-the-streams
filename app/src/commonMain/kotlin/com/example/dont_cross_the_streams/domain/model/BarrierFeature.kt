package com.example.dont_cross_the_streams.domain.model

enum class BarrierType {
    HIGHWAY,
    RAILWAY,
    DAM,
    FENCE,
    CANAL,
    URBAN_WALL
}

enum class ImpactLevel {
    SEVERE,
    HIGH,
    MODERATE,
    LOW
}

data class BarrierFeature(
    val id: String,
    val type: BarrierType,
    val name: String,
    val location: GeoLocation,
    val geometryPath: List<GeoLocation> = emptyList(),
    val impactLevel: ImpactLevel,
    val source: String,
    val heightMeters: Double? = null,
    val lengthKm: Double? = null,
    val description: String? = null
)
