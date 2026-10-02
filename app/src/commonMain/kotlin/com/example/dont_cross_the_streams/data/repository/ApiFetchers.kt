package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

expect suspend fun fetchLiveOverpassBarriersApi(bboxQuery: String): List<BarrierFeature>

expect suspend fun fetchLiveOccurrencesFromGbifApi(
    scientificName: String?,
    latitude: Double?,
    longitude: Double?
): List<WildlifeOccurrence>

expect suspend fun fetchLiveObservationsFromINaturalistApi(
    latitude: Double?,
    longitude: Double?,
    radiusKm: Int
): List<WildlifeOccurrence>

/**
 * Point-level dead-animal reports inside [bounds]. Returns null when the platform has no network
 * client or the request fails, so "unavailable" is distinguishable from "none reported here".
 */
expect suspend fun fetchLiveRoadkillReportsApi(bounds: BoundingBox, maxResults: Int): List<CollisionReport>?
