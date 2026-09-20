package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.datasource.MockWildlifeDataSource
import com.example.dont_cross_the_streams.data.remote.GbifApiService
import com.example.dont_cross_the_streams.data.remote.INaturalistApiService
import com.example.dont_cross_the_streams.data.remote.NetworkClient
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.domain.repository.WildlifeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

class WildlifeRepositoryImpl(
    private val gbifApi: GbifApiService = NetworkClient.gbifApiService,
    private val iNatApi: INaturalistApiService = NetworkClient.iNaturalistApiService
) : WildlifeRepository {

    override fun getWildlifeOccurrences(
        location: GeoLocation?,
        radiusKm: Double?
    ): Flow<List<WildlifeOccurrence>> = flow {
        // 1. Instantly emit offline occurrences for immediate display
        val offlineList = MockWildlifeDataSource.occurrences
        val initialResult = if (location != null && radiusKm != null) {
            offlineList.filter { it.location.distanceToKm(location) <= radiusKm }
        } else {
            offlineList
        }
        emit(initialResult)

        // 2. Determine target query coordinates (Missouri bbox or given location)
        val targetLat = location?.latitude ?: 38.5767 // Central Missouri (Jefferson City)
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
            } catch (e: Exception) {
                // Seamless fallback to offline dataset already emitted
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
        return try {
            val response = gbifApi.searchOccurrences(
                scientificName = scientificName,
                latitude = latitude,
                longitude = longitude,
                limit = 30
            )
            response.results?.mapNotNull { item ->
                val lat = item.decimalLatitude
                val lon = item.decimalLongitude
                if (lat != null && lon != null) {
                    WildlifeOccurrence(
                        id = "gbif_${item.key ?: System.nanoTime()}",
                        species = item.scientificName ?: scientificName ?: "Unknown Species",
                        commonName = item.vernacularName,
                        location = GeoLocation(lat, lon),
                        taxonGroup = item.classGroup ?: "Fauna",
                        observationCount = item.individualCount ?: 1,
                        timestamp = System.currentTimeMillis(),
                        source = "GBIF API"
                    )
                } else null
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchLiveObservationsFromINaturalist(
        latitude: Double?,
        longitude: Double?,
        radiusKm: Int
    ): List<WildlifeOccurrence> {
        return try {
            val response = iNatApi.getObservations(
                lat = latitude,
                lng = longitude,
                radiusKm = radiusKm,
                perPage = 30
            )
            response.results?.mapNotNull { obs ->
                val locParts = obs.locationStr?.split(",")
                val lat = locParts?.getOrNull(0)?.trim()?.toDoubleOrNull()
                val lon = locParts?.getOrNull(1)?.trim()?.toDoubleOrNull()
                if (lat != null && lon != null) {
                    val imageUrl = obs.photos?.firstOrNull()?.url?.replace("square", "medium")
                    WildlifeOccurrence(
                        id = "inat_${obs.id ?: System.nanoTime()}",
                        species = obs.taxon?.name ?: obs.speciesGuess ?: "Unidentified Species",
                        commonName = obs.taxon?.preferredCommonName ?: obs.speciesGuess,
                        location = GeoLocation(lat, lon),
                        taxonGroup = obs.taxon?.iconicTaxonName ?: "Fauna",
                        observationCount = 1,
                        timestamp = System.currentTimeMillis(),
                        source = "iNaturalist API",
                        imageUrl = imageUrl
                    )
                } else null
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
