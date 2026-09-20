package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

actual suspend fun fetchLiveOverpassBarriersApi(bboxQuery: String): List<BarrierFeature> = emptyList()

actual suspend fun fetchLiveOccurrencesFromGbifApi(
    scientificName: String?,
    latitude: Double?,
    longitude: Double?
): List<WildlifeOccurrence> = emptyList()

actual suspend fun fetchLiveObservationsFromINaturalistApi(
    latitude: Double?,
    longitude: Double?,
    radiusKm: Int
): List<WildlifeOccurrence> = emptyList()
