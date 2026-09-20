package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.WildlifeRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WildlifeRepositoryTest {

    private val repository = WildlifeRepositoryImpl()

    @Test
    fun getWildlifeOccurrences_returnsAllSampleOccurrences() = runTest {
        val occurrences = repository.getWildlifeOccurrences().first()
        assertTrue(occurrences.size >= 10)
    }

    @Test
    fun getCollisionHotspots_returnsHotspots() = runTest {
        val hotspots = repository.getCollisionHotspots().first()
        assertTrue(hotspots.isNotEmpty())
    }

    @Test
    fun getCollisionHotspots_filtersByLocation() = runTest {
        val vailPassLoc = GeoLocation(39.638, -105.897)
        val filtered = repository.getCollisionHotspots(vailPassLoc, radiusKm = 10.0).first()
        assertTrue(filtered.isNotEmpty())
        assertEquals("I-70 Vail Pass Corridor", filtered.first().highwayOrRouteName)
    }
}
