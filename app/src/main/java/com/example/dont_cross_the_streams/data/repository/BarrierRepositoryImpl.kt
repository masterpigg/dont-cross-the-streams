package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.datasource.MockBarrierDataSource
import com.example.dont_cross_the_streams.data.remote.NetworkClient
import com.example.dont_cross_the_streams.data.remote.OverpassApiService
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel
import com.example.dont_cross_the_streams.domain.repository.BarrierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

class BarrierRepositoryImpl(
    private val overpassApi: OverpassApiService = NetworkClient.overpassApiService
) : BarrierRepository {

    override fun getBarrierFeatures(
        location: GeoLocation?,
        radiusKm: Double?,
        typeFilter: BarrierType?
    ): Flow<List<BarrierFeature>> = flow {
        var offlineList = MockBarrierDataSource.barrierFeatures
        if (typeFilter != null) {
            offlineList = offlineList.filter { it.type == typeFilter }
        }
        if (location != null && radiusKm != null) {
            offlineList = offlineList.filter { it.location.distanceToKm(location) <= radiusKm }
        }
        emit(offlineList)

        // Asynchronously query live Overpass barriers for Missouri or location
        val isMissouriOrExplicit = location == null ||
                (location.latitude in 35.9..40.6 && location.longitude in -95.8..-89.1)

        if (isMissouriOrExplicit) {
            try {
                val query = "[out:json][timeout:10];(way[\"highway\"=\"motorway\"](38.2,-92.8,39.2,-91.8););out body geom;"
                val liveOverpass = withTimeoutOrNull(3500L) {
                    fetchLiveOverpassBarriers(query)
                } ?: emptyList()

                if (liveOverpass.isNotEmpty()) {
                    var combined = (offlineList + liveOverpass).distinctBy { it.id }
                    if (typeFilter != null) {
                        combined = combined.filter { it.type == typeFilter }
                    }
                    emit(combined)
                }
            } catch (e: Exception) {
                // Seamless fallback to offline list already emitted
            }
        }
    }

    override suspend fun fetchLiveOverpassBarriers(bboxQuery: String): List<BarrierFeature> {
        return try {
            val defaultQuery = if (bboxQuery.isBlank()) {
                "[out:json][timeout:25];(way[\"highway\"=\"motorway\"](39.5,-106.0,39.7,-105.5););out body geom;"
            } else bboxQuery

            val response = overpassApi.queryOverpass(defaultQuery)
            response.elements?.mapNotNull { elem ->
                val geom = elem.geometry?.map { GeoLocation(it.lat, it.lon) } ?: emptyList()
                val center = if (geom.isNotEmpty()) {
                    GeoLocation(
                        latitude = geom.map { it.latitude }.average(),
                        longitude = geom.map { it.longitude }.average()
                    )
                } else if (elem.lat != null && elem.lon != null) {
                    GeoLocation(elem.lat, elem.lon)
                } else null

                if (center != null) {
                    val highwayType = elem.tags?.get("highway")
                    val railwayType = elem.tags?.get("railway")
                    val waterwayType = elem.tags?.get("waterway")
                    val barrierType = elem.tags?.get("barrier")

                    val type = when {
                        highwayType != null -> BarrierType.HIGHWAY
                        railwayType != null -> BarrierType.RAILWAY
                        waterwayType == "dam" -> BarrierType.DAM
                        barrierType != null -> BarrierType.FENCE
                        else -> BarrierType.HIGHWAY
                    }

                    BarrierFeature(
                        id = "osm_${elem.id ?: System.nanoTime()}",
                        type = type,
                        name = elem.tags?.get("name") ?: elem.tags?.get("ref") ?: "OSM Linear Barrier",
                        location = center,
                        geometryPath = geom,
                        impactLevel = ImpactLevel.HIGH,
                        source = "OpenStreetMap Overpass API"
                    )
                } else null
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
