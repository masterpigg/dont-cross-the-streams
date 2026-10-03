package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.remote.GbifClient
import com.example.dont_cross_the_streams.data.remote.INaturalistClient
import com.example.dont_cross_the_streams.data.remote.NbiClient
import com.example.dont_cross_the_streams.data.remote.OverpassClient
import com.example.dont_cross_the_streams.data.remote.TigerWebClient
import com.example.dont_cross_the_streams.data.remote.encodeUrlParam
import com.example.dont_cross_the_streams.data.repository.LiveGeoDataRepository
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CrossingKind
import com.example.dont_cross_the_streams.domain.model.CrossingStructureType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel
import com.example.dont_cross_the_streams.domain.model.StudyAreas
import com.example.dont_cross_the_streams.domain.model.UrbanLevel
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiClientsTest {
    private val bounds = BoundingBox(38.6, -90.7, 38.8, -90.4)
    private val offline: suspend (String) -> String? = { null }

    @Test
    fun inaturalist_deadReports_parseAndSkipRecordsWithoutCoordinates() = runTest {
        val reports = INaturalistClient(ApiFixtures.http()).deadAnimalReports(bounds, pages = 1)
        assertNotNull(reports)
        assertEquals(listOf("inat_dead_101", "inat_dead_102"), reports.map { it.id })
        val deer = reports.first()
        assertEquals(GeoLocation(38.7121, -90.5012), deer.location)
        assertEquals("White-tailed Deer", deer.commonName)
        assertEquals("Mammals", deer.taxonGroup)
        assertEquals("2024-10-28", deer.observedOn)
        assertEquals("https://www.inaturalist.org/observations/101", deer.observationUrl)
    }

    @Test
    fun inaturalist_urls_targetDeadAnnotationAndBounds() {
        val dead = INaturalistClient.deadReportsUrl(bounds, 200, 2)
        assertTrue(dead.contains("term_id=17&term_value_id=19"))
        assertTrue(dead.contains("swlat=38.6&swlng=-90.7&nelat=38.8&nelng=-90.4"))
        assertTrue(dead.contains("page=2"))
        val obs = INaturalistClient.observationsUrl(bounds, 0, 1)
        assertTrue(obs.contains("quality_grade=research"))
        assertTrue(obs.contains("per_page=0"))
        assertFalse(obs.contains("term_value_id"))
    }

    @Test
    fun inaturalist_counts_readTotalResults() = runTest {
        val client = INaturalistClient(ApiFixtures.http())
        assertEquals(321, client.countDeadAnimalReports(bounds))
        assertEquals(4567, client.countObservations(bounds))
        assertNull(INaturalistClient(offline).countObservations(bounds))
    }

    @Test
    fun gbif_parsesRecordsAndDropsInaturalistDuplicates() = runTest {
        val records = GbifClient(ApiFixtures.http()).occurrences(bounds)
        assertNotNull(records)
        assertEquals(1, records.size)
        val raccoon = records.single()
        assertEquals("Procyon lotor", raccoon.species)
        assertEquals("Mammals", raccoon.taxonGroup)
        assertEquals("2019-07-11T00:00:00", raccoon.observedOn)
        assertEquals("https://www.gbif.org/occurrence/4000000001", raccoon.recordUrl)
        assertTrue(raccoon.source.contains("MDC Survey"))
        assertTrue(GbifClient.searchUrl(bounds, 300).contains("decimalLatitude=38.6,38.8&decimalLongitude=-90.7,-90.4"))
    }

    @Test
    fun overpass_classifiesBarriersCrossingsAndCulverts() = runTest {
        val infra = OverpassClient(ApiFixtures.http()).infrastructure(bounds, includeCulverts = true)
        assertNotNull(infra)
        val byId = infra.barriers.associateBy { it.id }
        val i64 = byId.getValue("osm_way_11")
        assertEquals(BarrierType.HIGHWAY, i64.type)
        assertEquals(ImpactLevel.SEVERE, i64.impactLevel)
        assertEquals("I 64 · Avenue of the Saints", i64.name)
        assertEquals(3, i64.geometryPath.size)
        assertTrue(i64.lengthKm!! > 8.0 && i64.lengthKm!! < 10.0, "length ${i64.lengthKm}")
        assertEquals(BarrierType.RAILWAY, byId.getValue("osm_way_12").type)
        assertEquals(BarrierType.DAM, byId.getValue("osm_node_13").type)
        assertFalse(byId.containsKey("osm_way_16"), "service roads are not barriers")

        val dedicated = infra.crossings.single { it.kind == CrossingKind.DEDICATED }
        assertEquals("Test Overpass", dedicated.name)
        assertEquals(CrossingStructureType.OVERPASS, dedicated.structureType)
        assertEquals("https://www.openstreetmap.org/way/14", dedicated.recordUrl)
        val culvert = infra.crossings.single { it.kind == CrossingKind.WATERWAY_STRUCTURE }
        assertEquals(CrossingStructureType.CULVERT, culvert.structureType)
        assertEquals("Dardenne Creek", culvert.crosses)
    }

    @Test
    fun overpass_query_onlyAsksForCulvertsWhenRequested() {
        assertFalse(OverpassClient.infrastructureQuery(bounds, includeCulverts = false).contains("culvert"))
        val q = OverpassClient.infrastructureQuery(bounds, includeCulverts = true)
        assertTrue(q.contains("(38.6,-90.7,38.8,-90.4)"), "Overpass uses south,west,north,east")
        assertTrue(q.contains("man_made\"=\"wildlife_crossing"))
        assertTrue(q.contains("around.roads:20"))
    }

    @Test
    fun overpass_statsQueries() = runTest {
        val client = OverpassClient(ApiFixtures.http())
        assertEquals(812.3456, client.majorRoadKm(bounds)!!, 1e-6)
        assertEquals(1, client.countWildlifeCrossings(bounds))
    }

    @Test
    fun nbi_parsesWaterwayStructures() = runTest {
        val structures = NbiClient(ApiFixtures.http()).waterwayStructures(bounds)
        assertNotNull(structures)
        assertEquals(2, structures.size, "records without coordinates are skipped")
        val culvert = structures[0]
        assertEquals("nbi_A1234", culvert.id)
        assertEquals(CrossingStructureType.CULVERT, culvert.structureType)
        assertEquals(CrossingKind.WATERWAY_STRUCTURE, culvert.kind)
        assertEquals("I-64", culvert.carries)
        assertEquals("DARDENNE CREEK", culvert.crosses)
        assertEquals(64000, culvert.averageDailyTraffic)
        assertEquals(1972, culvert.yearBuilt)
        val bridge = structures[1]
        assertEquals(CrossingStructureType.BRIDGE, bridge.structureType)
        assertEquals(5400, bridge.averageDailyTraffic)
        assertNull(bridge.yearBuilt)
        assertEquals(412, NbiClient(ApiFixtures.http()).countWaterwayStructures(bounds))
    }

    @Test
    fun nbi_queryFiltersToWaterwayServiceCodes() {
        val url = NbiClient.queryUrl(bounds, countOnly = false, limit = 1000)
        assertTrue(url.contains(encodeUrlParam("SERVICE_UND_042B IN ('5','6','7','8','9')")))
        assertTrue(url.contains("geometry=-90.7,38.6,-90.4,38.8"))
        assertTrue(url.contains("inSR=4326"))
    }

    @Test
    fun nbi_arcgisErrorBodyIsUnavailable() = runTest {
        val http = ApiFixtures.http(mapOf("geo.dot.gov" to """{"error":{"code":400,"message":"Invalid query"}}"""))
        assertNull(NbiClient(http).waterwayStructures(bounds))
    }

    @Test
    fun tigerweb_parsesTractPolygonsAndDensity() = runTest {
        val tracts = TigerWebClient(ApiFixtures.http()).tracts(bounds)
        assertNotNull(tracts)
        assertEquals(2, tracts.size)
        val urban = tracts[0]
        assertEquals("tract_29189212100", urban.id)
        assertEquals(4200, urban.population)
        assertEquals(3.5, urban.landAreaKm2, 1e-9)
        assertEquals(1200.0, urban.densityScore, 1e-9)
        assertEquals(UrbanLevel.URBAN, urban.urbanLevel)
        assertEquals(GeoLocation(38.7, -90.5), urban.centerLocation)
        assertEquals(5, urban.polygon.single().size)
        assertEquals(UrbanLevel.WILDERNESS, tracts[1].urbanLevel)
    }

    @Test
    fun tigerweb_countyStatsUseFipsCodes() = runTest {
        assertTrue(TigerWebClient.countyStatsUrl("29", "183").contains(encodeUrlParam("STATE='29' AND COUNTY='183'")))
        val pop = TigerWebClient(ApiFixtures.http()).countyPopulation("29", "189")
        assertNotNull(pop)
        assertEquals(1004125, pop.population)
        assertTrue(abs(pop.densityPerKm2 - 763.77) < 0.1, "density ${pop.densityPerKm2}")
    }

    @Test
    fun repository_wildlife_mergesSourcesAndExcludesDeadAnimals() = runTest {
        val wildlife = LiveGeoDataRepository(ApiFixtures.http()).wildlifeObservations(bounds)
        assertNotNull(wildlife)
        // iNat 101 is also in the dead set, GBIF's iNaturalist copy is dropped.
        assertEquals(setOf("inat_201", "gbif_4000000001"), wildlife.map { it.id }.toSet())
    }

    @Test
    fun repository_everyLayerIsNullWhenOffline() = runTest {
        val repo = LiveGeoDataRepository(offline)
        assertNull(repo.wildlifeObservations(bounds))
        assertNull(repo.collisionReports(bounds))
        assertNull(repo.infrastructure(bounds, includeCulverts = true))
        assertNull(repo.waterwayStructures(bounds))
        assertNull(repo.censusTracts(bounds))
        val stats = repo.studyAreaStats(StudyAreas.ST_CHARLES_COUNTY)
        assertNull(stats.deadAnimalReports)
        assertNull(stats.population)
        assertNull(stats.deadReportsPer100RoadKm)
    }

    @Test
    fun repository_studyAreaStats_combineAllSources() = runTest {
        val stats = LiveGeoDataRepository(ApiFixtures.http()).studyAreaStats(StudyAreas.ST_LOUIS_COUNTY)
        assertEquals(321, stats.deadAnimalReports)
        assertEquals(4567, stats.wildlifeObservations)
        assertEquals(8910, stats.gbifOccurrences)
        assertEquals(412, stats.waterwayStructures)
        assertEquals(1, stats.dedicatedCrossings)
        assertEquals(1004125, stats.population?.population)
        assertEquals(321 / 812.3456 * 100, stats.deadReportsPer100RoadKm!!, 1e-9)
    }

    @Test
    fun encodeUrlParam_encodesReservedCharacters() {
        assertEquals("a%20b%26c%3D%27d%27", encodeUrlParam("a b&c='d'"))
        assertEquals("%5B%22x%22%5D", encodeUrlParam("[\"x\"]"))
    }
}
