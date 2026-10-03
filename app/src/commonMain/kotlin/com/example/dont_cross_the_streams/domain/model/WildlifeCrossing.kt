package com.example.dont_cross_the_streams.domain.model

enum class CrossingStructureType {
    OVERPASS,
    UNDERPASS,
    CULVERT,
    BRIDGE,
    FISH_PASSAGE
}

enum class CrossingKind {
    /** Built specifically for wildlife (OpenStreetMap man_made=wildlife_crossing). */
    DEDICATED,

    /** An existing road bridge or culvert over a waterway: a route animals often use under a road. */
    WATERWAY_STRUCTURE
}

/**
 * A place where animals can get under or over a barrier. Kept separate from [BarrierFeature] so the
 * map can contrast where animals are helped across with where they die.
 */
data class WildlifeCrossing(
    val id: String,
    val name: String,
    val structureType: CrossingStructureType,
    val location: GeoLocation,
    val targetSpecies: String,
    val source: String,
    val structureCount: Int = 1,
    val description: String? = null,
    val kind: CrossingKind = CrossingKind.DEDICATED,
    /** Road carried over the structure (NBI item 7 / OSM road name). */
    val carries: String? = null,
    /** Feature passing under it, e.g. the creek name (NBI item 6A). */
    val crosses: String? = null,
    /** Average daily traffic on the road above (NBI item 29). */
    val averageDailyTraffic: Int? = null,
    val yearBuilt: Int? = null,
    val recordUrl: String? = null
)
