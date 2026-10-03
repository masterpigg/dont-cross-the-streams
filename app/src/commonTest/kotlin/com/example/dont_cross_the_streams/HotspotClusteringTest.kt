package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.analysis.HotspotClustering
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HotspotClusteringTest {

    private fun report(id: String, lat: Double, lon: Double, name: String = "White-tailed Deer", date: String = "2024-10-01") =
        CollisionReport(id, "Odocoileus virginianus", name, "Mammals", GeoLocation(lat, lon), date, "test")

    // ~0.0045° latitude ≈ 500 m
    private val roadside = listOf(
        report("a", 38.7000, -90.5000, date = "2024-10-03"),
        report("b", 38.7045, -90.5000, date = "2023-11-12"),
        report("c", 38.7090, -90.5000, name = "Raccoon", date = "2025-01-20"),
        report("d", 38.7135, -90.5000)
    )

    @Test
    fun chainOfReportsWithinRadiusFormsOneHotspot() {
        val hotspots = HotspotClustering.cluster(roadside)
        assertEquals(1, hotspots.size)
        val h = hotspots.single()
        assertEquals(4, h.incidentCount)
        assertEquals(setOf("a", "b", "c", "d"), h.reportIds.toSet())
        assertEquals("2023-11-12", h.firstObserved)
        assertEquals("2025-01-20", h.lastObserved)
        assertTrue(h.primarySpeciesAffected.startsWith("White-tailed Deer (3)"), h.primarySpeciesAffected)
        assertEquals(CollisionSeverity.LOW, h.severity)
    }

    @Test
    fun isolatedReportsAreNotHotspots() {
        val scattered = listOf(report("x", 38.6, -90.4), report("y", 38.8, -90.6), report("z", 38.5, -90.9))
        assertTrue(HotspotClustering.cluster(scattered).isEmpty())
        assertTrue(HotspotClustering.cluster(roadside.take(2)).isEmpty(), "needs at least 3 reports")
    }

    @Test
    fun noiseAwayFromClusterIsExcluded() {
        val hotspots = HotspotClustering.cluster(roadside + report("far", 38.9, -90.9))
        assertEquals(4, hotspots.single().incidentCount)
    }

    @Test
    fun severityScalesWithReportCount() {
        val dense = (0 until 15).map { report("r$it", 38.70 + it * 0.0001, -90.5) }
        assertEquals(CollisionSeverity.CRITICAL, HotspotClustering.cluster(dense).single().severity)
    }

    @Test
    fun hotspotNamesTheNearestMajorRoadWithin300m() {
        val road = BarrierFeature(
            id = "osm_way_1", type = BarrierType.HIGHWAY, name = "I 64", location = GeoLocation(38.705, -90.5),
            geometryPath = listOf(GeoLocation(38.69, -90.501), GeoLocation(38.72, -90.501)),
            impactLevel = ImpactLevel.SEVERE, source = "test"
        )
        val farRoad = road.copy(id = "osm_way_2", name = "Far Road",
            geometryPath = listOf(GeoLocation(38.69, -90.55), GeoLocation(38.72, -90.55)))
        assertEquals("I 64", HotspotClustering.cluster(roadside, listOf(farRoad, road)).single().highwayOrRouteName)
        assertNull(HotspotClustering.cluster(roadside, listOf(farRoad)).single().highwayOrRouteName)
    }

    @Test
    fun distanceToPath_isPerpendicularDistance() {
        val path = listOf(GeoLocation(38.0, -90.0), GeoLocation(38.0, -89.0))
        val d = HotspotClustering.distanceToPathKm(GeoLocation(38.01, -89.5), path)
        assertTrue(d in 1.09..1.13, "distance $d")
    }
}
