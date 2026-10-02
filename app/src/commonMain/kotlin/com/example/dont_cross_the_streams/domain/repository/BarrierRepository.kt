package com.example.dont_cross_the_streams.domain.repository

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import kotlinx.coroutines.flow.Flow

interface BarrierRepository {
    fun getBarrierFeatures(
        location: GeoLocation? = null,
        radiusKm: Double? = null,
        typeFilter: BarrierType? = null
    ): Flow<List<BarrierFeature>>

    fun getWildlifeCrossings(): Flow<List<WildlifeCrossing>>

    suspend fun fetchLiveOverpassBarriers(bboxQuery: String): List<BarrierFeature>
}
