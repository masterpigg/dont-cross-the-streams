package com.example.dont_cross_the_streams.domain.model

data class WildlifeOccurrence(
    val id: String,
    val species: String,
    val commonName: String?,
    val location: GeoLocation,
    val taxonGroup: String,
    val observationCount: Int,
    val timestamp: Long,
    val source: String,
    val imageUrl: String? = null,
    val conservationStatus: String? = null
)
