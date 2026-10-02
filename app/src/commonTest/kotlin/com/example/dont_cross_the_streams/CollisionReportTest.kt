package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.datasource.MockBarrierDataSource
import com.example.dont_cross_the_streams.data.datasource.MockCrossingDataSource
import com.example.dont_cross_the_streams.data.repository.INATURALIST_ROADKILL_SOURCE
import com.example.dont_cross_the_streams.data.repository.buildINaturalistRoadkillUrl
import com.example.dont_cross_the_streams.data.repository.parseRoadkillRows
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.ui.map.nearestCrossingLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollisionReportTest {

    @Test
    fun roadkillUrl_asksForDeadVertebratesInsideTheBounds() {
        val url = buildINaturalistRoadkillUrl(BoundingBox(38.0, -91.0, 39.0, -90.0), perPage = 500)
        assertTrue(url.startsWith("https://api.inaturalist.org/v1/observations?"))
        assertTrue(url.contains("term_id=17&term_value_id=19"), "must filter on Alive or Dead = Dead")
        assertTrue(url.contains("swlat=38.0&swlng=-91.0&nelat=39.0&nelng=-90.0"))
        assertTrue(url.contains("per_page=200"), "per_page is capped at the API maximum")
    }

    @Test
    fun parseRoadkillRows_mapsFieldsAndSkipsBadRows() {
        val rows = listOf(
            "123\t38.95\t-92.33\tOdocoileus virginianus\tWhite-tailed Deer\tMammalia\t2026-09-01\thttps://www.inaturalist.org/observations/123",
            "bad row",
            "456\tnot-a-number\t-92.0\tX\tY\tAves\t\t",
            "789\t37.1\t-89.4\tAgkistrodon piscivorus\t\tReptilia\t\t"
        ).joinToString("\n")

        val reports = parseRoadkillRows(rows)
        assertEquals(2, reports.size)

        val deer = reports[0]
        assertEquals("inat_dead_123", deer.id)
        assertEquals("White-tailed Deer", deer.commonName)
        assertEquals("Mammals", deer.taxonGroup)
        assertEquals(GeoLocation(38.95, -92.33), deer.location)
        assertEquals("2026-09-01", deer.observedOn)
        assertEquals(INATURALIST_ROADKILL_SOURCE, deer.source)
        assertEquals("https://www.inaturalist.org/observations/123", deer.observationUrl)

        val snake = reports[1]
        assertNull(snake.commonName)
        assertNull(snake.observedOn)
        assertNull(snake.observationUrl)
        assertEquals("Reptiles", snake.taxonGroup)
    }

    @Test
    fun crossings_areNoLongerMisfiledAsBarriers() {
        val crossingNames = MockCrossingDataSource.crossings.map { it.name }.toSet()
        assertTrue(crossingNames.isNotEmpty())
        assertFalse(MockBarrierDataSource.barrierFeatures.any { it.name in crossingNames || it.id.startsWith("bar_wcpp") })
    }

    @Test
    fun nearestCrossingLine_namesTheClosestCrossing() {
        val nearLibertyCanyon = GeoLocation(34.14, -118.73)
        val line = nearestCrossingLine(nearLibertyCanyon, MockCrossingDataSource.crossings)
        assertNotNull(line)
        assertTrue(line.contains("Liberty Canyon"), line)
        assertNull(nearestCrossingLine(nearLibertyCanyon, emptyList()))
    }
}
