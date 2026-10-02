package com.example.dont_cross_the_streams.domain.model

/**
 * One reported animal death at a specific point, as opposed to the corridor-level totals in
 * [CollisionHotspot]. Currently sourced from iNaturalist observations annotated "Alive or Dead:
 * Dead"; most are road mortality, but the annotation alone does not prove a vehicle strike.
 */
data class CollisionReport(
    val id: String,
    val species: String,
    val commonName: String?,
    val taxonGroup: String,
    val location: GeoLocation,
    val observedOn: String?,
    val source: String,
    val observationUrl: String? = null
)
