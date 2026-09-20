package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.datasource.MockWildlifeDataSource
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.domain.repository.WildlifeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

class WildlifeRepositoryImpl : WildlifeRepository {

    override fun getWildlifeOccurrences(
        location: GeoLocation?,
        radiusKm: Double?
    ): Flow<List<WildlifeOccurrence>> = flow {
        val offlineList = MockWildlifeDataSource.occurrences
        val initialResult = if (location != null && radiusKm != null) {
            offlineList.filter { it.location.distanceToKm(location) <= radiusKm }
        } else {
            offlineList
        }
        emit(initialResult)

        val targetLat = location?.latitude ?: 38.5767
        val targetLon = location?.longitude ?: -92.1735
        val isMissouriOrExplicit = location == null ||
                (location.latitude in 35.9..40.6 && location.longitude in -95.8..-89.1)

        if (isMissouriOrExplicit) {
            try {
                val liveList = withTimeoutOrNull(4000L) {
                    val inatObs = fetchLiveObservationsFromINaturalist(targetLat, targetLon, radiusKm = 100)
                    val gbifObs = fetchLiveOccurrencesFromGbif(latitude = targetLat, longitude = targetLon)
                    (inatObs + gbifObs).distinctBy { it.id }
                } ?: emptyList()

                if (liveList.isNotEmpty()) {
                    val merged = (initialResult + liveList).distinctBy { "${it.species}_${it.location.latitude}_${it.location.longitude}" }
                    emit(merged)
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun getCollisionHotspots(
        location: GeoLocation?,
        radiusKm: Double?
    ): Flow<List<CollisionHotspot>> = flow {
        val all = MockWildlifeDataSource.collisionHotspots
        if (location != null && radiusKm != null) {
            val filtered = all.filter { it.location.distanceToKm(location) <= radiusKm }
            emit(filtered)
        } else {
            emit(all)
        }
    }

    override suspend fun fetchLiveOccurrencesFromGbif(
        scientificName: String?,
        latitude: Double?,
        longitude: Double?
    ): List<WildlifeOccurrence> {
        return fetchLiveOccurrencesFromGbifApi(scientificName, latitude, longitude)
    }

    override suspend fun fetchLiveObservationsFromINaturalist(
        latitude: Double?,
        longitude: Double?,
        radiusKm: Int
    ): List<WildlifeOccurrence> {
        return fetchLiveObservationsFromINaturalistApi(latitude, longitude, radiusKm)
    }
}
