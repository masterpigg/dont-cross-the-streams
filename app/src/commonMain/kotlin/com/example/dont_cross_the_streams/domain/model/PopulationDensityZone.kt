package com.example.dont_cross_the_streams.domain.model

enum class UrbanLevel {
    WILDERNESS,
    RURAL,
    SUBURBAN,
    URBAN,
    METROPOLITAN
}

/** One 2020 Census tract: real population and land area from the Census Bureau's TIGERweb service. */
data class PopulationDensityZone(
    val id: String,
    val regionName: String,
    val boundingBox: BoundingBox,
    val centerLocation: GeoLocation,
    val densityScore: Double, // People per sq km of land
    val urbanLevel: UrbanLevel,
    val source: String = "US Census Bureau 2020 (TIGERweb)",
    val population: Int = 0,
    val landAreaKm2: Double = 0.0,
    /** Outer and inner rings of the tract boundary (lat/lon). */
    val polygon: List<List<GeoLocation>> = emptyList()
)

fun urbanLevelForDensity(peoplePerKm2: Double): UrbanLevel = when {
    peoplePerKm2 < 10 -> UrbanLevel.WILDERNESS
    peoplePerKm2 < 100 -> UrbanLevel.RURAL
    peoplePerKm2 < 1000 -> UrbanLevel.SUBURBAN
    peoplePerKm2 < 3000 -> UrbanLevel.URBAN
    else -> UrbanLevel.METROPOLITAN
}
