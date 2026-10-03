package com.example.dont_cross_the_streams.domain.repository

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.Infrastructure
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.StudyArea
import com.example.dont_cross_the_streams.domain.model.StudyAreaStats
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

/**
 * Every map layer comes from a live public API for the requested area. Each call returns null when
 * its source is unreachable, so the UI can say "unavailable" instead of showing stand-in data.
 */
interface GeoDataRepository {
    suspend fun wildlifeObservations(bounds: BoundingBox): List<WildlifeOccurrence>?
    suspend fun collisionReports(bounds: BoundingBox): List<CollisionReport>?
    suspend fun infrastructure(bounds: BoundingBox, includeCulverts: Boolean): Infrastructure?
    suspend fun waterwayStructures(bounds: BoundingBox): List<WildlifeCrossing>?
    suspend fun censusTracts(bounds: BoundingBox): List<PopulationDensityZone>?
    suspend fun studyAreaStats(area: StudyArea): StudyAreaStats
}
