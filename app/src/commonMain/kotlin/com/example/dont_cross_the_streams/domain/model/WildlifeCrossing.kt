package com.example.dont_cross_the_streams.domain.model

enum class CrossingStructureType {
    OVERPASS,
    UNDERPASS,
    CULVERT,
    FISH_PASSAGE
}

/**
 * A built (or funded) structure that lets animals get across a barrier. Kept separate from
 * [BarrierFeature] so the map can contrast where animals are helped across with where they die.
 */
data class WildlifeCrossing(
    val id: String,
    val name: String,
    val structureType: CrossingStructureType,
    val location: GeoLocation,
    val targetSpecies: String,
    val source: String,
    val structureCount: Int = 1,
    val description: String? = null
)
