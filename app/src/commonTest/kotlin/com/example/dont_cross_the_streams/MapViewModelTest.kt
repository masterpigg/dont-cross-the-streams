package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.LiveGeoDataRepository
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.Infrastructure
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.StudyArea
import com.example.dont_cross_the_streams.domain.model.StudyAreaStats
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.domain.repository.GeoDataRepository
import com.example.dont_cross_the_streams.ui.map.ConflictRegionPreset
import com.example.dont_cross_the_streams.ui.map.DEFAULT_MAP_CENTER
import com.example.dont_cross_the_streams.ui.map.LayerLoadState
import com.example.dont_cross_the_streams.ui.map.MapLayer
import com.example.dont_cross_the_streams.ui.map.MapViewModel
import com.example.dont_cross_the_streams.ui.map.monthOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    /** Wraps the fixture-backed live repository and records which areas each layer asked for. */
    private class RecordingRepository(var online: Boolean = true) : GeoDataRepository {
        private val live = LiveGeoDataRepository(ApiFixtures.http())
        val requests = mutableMapOf<String, MutableList<BoundingBox>>()
        var culvertsRequested = false

        private fun record(name: String, b: BoundingBox) {
            requests.getOrPut(name) { mutableListOf() }.add(b)
        }

        override suspend fun wildlifeObservations(bounds: BoundingBox): List<WildlifeOccurrence>? {
            record("wildlife", bounds); return if (online) live.wildlifeObservations(bounds) else null
        }

        override suspend fun collisionReports(bounds: BoundingBox): List<CollisionReport>? {
            record("collisions", bounds); return if (online) live.collisionReports(bounds) else null
        }

        override suspend fun infrastructure(bounds: BoundingBox, includeCulverts: Boolean): Infrastructure? {
            record("infrastructure", bounds); culvertsRequested = culvertsRequested || includeCulverts
            return if (online) live.infrastructure(bounds, includeCulverts) else null
        }

        override suspend fun waterwayStructures(bounds: BoundingBox): List<WildlifeCrossing>? {
            record("structures", bounds); return if (online) live.waterwayStructures(bounds) else null
        }

        override suspend fun censusTracts(bounds: BoundingBox): List<PopulationDensityZone>? {
            record("population", bounds); return if (online) live.censusTracts(bounds) else null
        }

        override suspend fun studyAreaStats(area: StudyArea): StudyAreaStats = live.studyAreaStats(area)
    }

    private lateinit var repository: RecordingRepository
    private lateinit var viewModel: MapViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = RecordingRepository()
        viewModel = MapViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsOnStLouisAndLoadsEveryLayerFromLiveSources() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(DEFAULT_MAP_CENTER, state.mapCenter)
        assertTrue(state.allWildlifeOccurrences.isNotEmpty())
        assertTrue(state.collisionReports.isNotEmpty())
        assertTrue(state.allBarriers.isNotEmpty())
        assertTrue(state.allPopulationZones.isNotEmpty())
        // Default zoom 10 is below the NBI layer's minimum zoom.
        assertEquals(LayerLoadState.ZOOM_IN, state.statusOf(MapLayer.STRUCTURES))
        assertEquals(LayerLoadState.LOADED, state.statusOf(MapLayer.WILDLIFE))
        assertFalse(repository.culvertsRequested)
    }

    @Test
    fun zoomingIn_loadsBridgesAndCulverts() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateMapCenterAndZoom(GeoLocation(38.70, -90.60), 12f)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(LayerLoadState.LOADED, state.statusOf(MapLayer.STRUCTURES))
        assertTrue(state.nbiStructures.isNotEmpty())
        assertTrue(repository.culvertsRequested)
        assertTrue(state.allWildlifeCrossings.size == state.nbiStructures.size + state.osmCrossings.size)
    }

    @Test
    fun zoomedOutTooFar_layersAskToZoomIn() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateMapCenterAndZoom(GeoLocation(38.5, -92.0), 6f)
        testDispatcher.scheduler.advanceUntilIdle()
        MapLayer.entries.forEach { layer ->
            assertEquals(LayerLoadState.ZOOM_IN, viewModel.uiState.value.statusOf(layer), "$layer")
        }
    }

    @Test
    fun rapidCameraMoves_areDebouncedIntoOneRequestPerLayer() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val before = repository.requests.getValue("collisions").size
        for (i in 1..20) viewModel.updateMapCenterAndZoom(GeoLocation(38.9 + i * 0.01, -90.3), 11f)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(before + 1, repository.requests.getValue("collisions").size)
        assertTrue(repository.requests.getValue("collisions").last().contains(GeoLocation(39.1, -90.3)))
    }

    @Test
    fun smallPanInsideLoadedArea_doesNotRefetch() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val before = repository.requests.getValue("wildlife").size
        viewModel.updateMapCenterAndZoom(GeoLocation(DEFAULT_MAP_CENTER.latitude, DEFAULT_MAP_CENTER.longitude + 0.0001), 10.2f)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(before, repository.requests.getValue("wildlife").size)
    }

    @Test
    fun unreachableSource_reportsUnavailableAndKeepsPreviousData() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val reports = viewModel.uiState.value.collisionReports
        repository.online = false
        viewModel.updateMapCenterAndZoom(GeoLocation(39.2, -90.9), 11f)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(LayerLoadState.UNAVAILABLE, state.statusOf(MapLayer.COLLISIONS))
        assertEquals(reports, state.collisionReports)
    }

    @Test
    fun hotspotsAreDerivedFromLoadedReports() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        // The two fixture reports are only 2 apart: not enough for a hotspot (minimum 3).
        assertTrue(viewModel.uiState.value.allCollisionHotspots.isEmpty())
        assertTrue(viewModel.uiState.value.collisionReports.size == 2)
    }

    @Test
    fun monthFilter_appliesToSightingsAndReports() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleMonth(4) // April: only the salamander sighting
        var state = viewModel.uiState.value
        assertEquals(setOf(4), state.selectedMonths)
        assertTrue(state.filteredWildlifeOccurrences.all { monthOf(it.observedOn) == 4 })
        assertTrue(state.filteredWildlifeOccurrences.isNotEmpty())
        assertTrue(state.filteredCollisionReports.isEmpty(), "fixture reports are Oct/Nov")

        viewModel.toggleMonth(10)
        state = viewModel.uiState.value
        assertEquals(1, state.filteredCollisionReports.size)

        viewModel.clearMonths()
        assertEquals(2, viewModel.uiState.value.filteredCollisionReports.size)
    }

    @Test
    fun toggles_hideLayersAndStopLoadingThem() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleWildlifeOverlay()
        viewModel.toggleCollisionReportsOverlay()
        viewModel.toggleBarriersOverlay()
        viewModel.togglePopulationDensityOverlay()
        viewModel.toggleCrossingsOverlay()
        val state = viewModel.uiState.value
        assertTrue(state.filteredWildlifeOccurrences.isEmpty())
        assertTrue(state.filteredCollisionReports.isEmpty())
        assertTrue(state.filteredBarriers.isEmpty())
        assertTrue(state.filteredPopulationZones.isEmpty())
        assertTrue(state.filteredWildlifeCrossings.isEmpty())

        val wildlifeRequests = repository.requests.getValue("wildlife").size
        viewModel.updateMapCenterAndZoom(GeoLocation(39.3, -91.0), 11f)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(wildlifeRequests, repository.requests.getValue("wildlife").size)
    }

    @Test
    fun barrierTypeFilter() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleBarrierType(BarrierType.HIGHWAY)
        assertFalse(viewModel.uiState.value.filteredBarriers.any { it.type == BarrierType.HIGHWAY })
        assertTrue(viewModel.uiState.value.filteredBarriers.isNotEmpty())
    }

    @Test
    fun presets_moveTheCameraToTheStudyRegion() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val preset = ConflictRegionPreset.ST_CHARLES_CITY
        viewModel.applyPreset(preset)
        val state = viewModel.uiState.value
        assertEquals(preset.center, state.mapCenter)
        assertEquals(preset.zoomLevel, state.zoomLevel)
        assertEquals(preset, state.activePreset)
        ConflictRegionPreset.entries.forEach { p ->
            assertTrue(p.center.latitude in 38.3..39.1 && p.center.longitude in -91.2..-90.0, "${p.name} is outside the St. Louis region")
        }
    }

    @Test
    fun resetFilters_restoresDefaults() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleTaxonGroup("Mammals")
        viewModel.toggleMonth(6)
        viewModel.toggleWildlifeOverlay()
        viewModel.resetFilters()
        val state = viewModel.uiState.value
        assertTrue("Mammals" in state.selectedTaxonGroups)
        assertTrue(state.selectedMonths.isEmpty())
        assertTrue(state.showWildlifeOccurrences)
        assertNull(state.activePreset)
    }

    @Test
    fun zoomButtons_stepWholeLevelsWithinLimits() = runTest {
        viewModel.updateMapCenterAndZoom(GeoLocation(38.0, -92.0), 17.5f)
        viewModel.zoomIn()
        assertEquals(18f, viewModel.uiState.value.zoomLevel)
        viewModel.updateMapCenterAndZoom(GeoLocation(38.0, -92.0), 2.5f)
        viewModel.zoomOut()
        assertEquals(2f, viewModel.uiState.value.zoomLevel)
    }

    @Test
    fun monthOf_handlesSourceDateFormats() {
        assertEquals(10, monthOf("2024-10-28"))
        assertEquals(7, monthOf("2019-07-11T00:00:00"))
        assertEquals(5, monthOf("2019-05-01/2019-05-03"))
        assertNull(monthOf("2019"))
        assertNull(monthOf(null))
    }
}
