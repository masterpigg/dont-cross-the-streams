package com.example.dont_cross_the_streams.domain.repository

import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.RiskMatrixScore
import kotlinx.coroutines.flow.Flow

interface ConflictMatrixRepository {
    fun getAllRiskMatrixScores(): Flow<List<RiskMatrixScore>>
    fun getRiskMatrixForLocation(location: GeoLocation, regionName: String? = null): Flow<RiskMatrixScore>
    fun getPopulationDensityZones(): Flow<List<PopulationDensityZone>>
    fun calculateCompositeConflictScore(
        animalDensity: Double,
        barrierDensity: Double,
        collisionCount: Int,
        humanPressure: Double
    ): Int
}
