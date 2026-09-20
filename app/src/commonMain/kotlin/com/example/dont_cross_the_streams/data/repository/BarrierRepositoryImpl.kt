package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.datasource.MockBarrierDataSource
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.repository.BarrierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

class BarrierRepositoryImpl : BarrierRepository {

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
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun fetchLiveOverpassBarriers(bboxQuery: String): List<BarrierFeature> {
        return fetchLiveOverpassBarriersApi(bboxQuery)
    }
}
