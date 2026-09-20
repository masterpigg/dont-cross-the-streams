package com.example.dont_cross_the_streams.domain.repository

import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import kotlinx.coroutines.flow.Flow

interface WildlifeRepository {
    fun getWildlifeOccurrences(
        location: GeoLocation? = null,
        radiusKm: Double? = null
    ): Flow<List<WildlifeOccurrence>>

    fun getCollisionHotspots(
        location: GeoLocation? = null,
        radiusKm: Double? = null
    ): Flow<List<CollisionHotspot>>

    suspend fun fetchLiveOccurrencesFromGbif(
        scientificName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): List<WildlifeOccurrence>

    suspend fun fetchLiveObservationsFromINaturalist(
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Int = 50
    ): List<WildlifeOccurrence>
}
