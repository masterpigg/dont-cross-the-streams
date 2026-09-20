package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.RiskLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConflictMatrixRepositoryTest {

    private lateinit var repository: ConflictMatrixRepositoryImpl

    @Before
    fun setUp() {
        repository = ConflictMatrixRepositoryImpl()
    }

    @Test
    fun getAllRiskMatrixScores_returnsDefaultHighRiskCorridors() = runTest {
        val scores = repository.getAllRiskMatrixScores().first()
        assertTrue(scores.isNotEmpty())
        assertTrue(scores.any { it.riskLevel == RiskLevel.HIGH })
    }

    @Test
    fun calculateCompositeConflictScore_producesExpectedWeighting() {
        val score = repository.calculateCompositeConflictScore(
            animalDensity = 100.0,
            barrierDensity = 100.0,
            collisionCount = 200, // normalized to 100.0
            humanPressure = 100.0
        )
        assertEquals(100, score)

        val zeroScore = repository.calculateCompositeConflictScore(
            animalDensity = 0.0,
            barrierDensity = 0.0,
            collisionCount = 0,
            humanPressure = 0.0
        )
        assertEquals(0, zeroScore)
    }

    @Test
    fun getRiskMatrixForLocation_calculatesDynamicScoreForVailPass() = runTest {
        val vailPassLoc = GeoLocation(39.638, -105.897)
        val score = repository.getRiskMatrixForLocation(vailPassLoc, "Vail Pass").first()

        assertNotNull(score)
        assertEquals("Vail Pass", score.region)
        assertTrue(score.compositeConflictScore > 0)
    }
}
