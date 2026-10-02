package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.BarrierRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.WildlifeRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.repository.CollisionReportRepository
import com.example.dont_cross_the_streams.ui.map.CollisionReportStatus
import com.example.dont_cross_the_streams.ui.map.ConflictRegionPreset
import com.example.dont_cross_the_streams.ui.map.MapViewModel
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
    private lateinit var viewModel: MapViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MapViewModel(
            wildlifeRepository = WildlifeRepositoryImpl(),
            barrierRepository = BarrierRepositoryImpl(),
            conflictMatrixRepository = ConflictMatrixRepositoryImpl()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadMapData_populatesState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.allWildlifeOccurrences.isNotEmpty())
        assertTrue(state.allCollisionHotspots.isNotEmpty())
        assertTrue(state.allBarriers.isNotEmpty())
        assertTrue(state.allPopulationZones.isNotEmpty())
    }

    @Test
    fun toggleOverlayLayers_togglesFlags() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showWildlifeOccurrences)
        viewModel.toggleWildlifeOverlay()
        assertFalse(viewModel.uiState.value.showWildlifeOccurrences)
        assertTrue(viewModel.uiState.value.filteredWildlifeOccurrences.isEmpty())

        assertTrue(viewModel.uiState.value.showBarriers)
        viewModel.toggleBarriersOverlay()
        assertFalse(viewModel.uiState.value.showBarriers)
        assertTrue(viewModel.uiState.value.filteredBarriers.isEmpty())
    }

    @Test
    fun toggleTaxonGroup_filtersOccurrences() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCount = viewModel.uiState.value.filteredWildlifeOccurrences.size
        viewModel.toggleTaxonGroup("Mammals")

        val stateAfterRemoval = viewModel.uiState.value
        assertFalse(stateAfterRemoval.selectedTaxonGroups.contains("Mammals"))
        assertTrue(stateAfterRemoval.filteredWildlifeOccurrences.size < initialCount)
    }

    @Test
    fun toggleBarrierType_filtersBarriers() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleBarrierType(BarrierType.HIGHWAY)
        val stateAfter = viewModel.uiState.value
        assertFalse(stateAfter.selectedBarrierTypes.contains(BarrierType.HIGHWAY))
        assertFalse(stateAfter.filteredBarriers.any { it.type == BarrierType.HIGHWAY })
    }

    @Test
    fun applyPreset_updatesCenterZoomAndFilters() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val preset = ConflictRegionPreset.YELLOWSTONE_CORRIDOR
        viewModel.applyPreset(preset)

        val state = viewModel.uiState.value
        assertEquals(preset.center, state.mapCenter)
        assertEquals(preset.zoomLevel, state.zoomLevel)
        assertEquals(preset, state.activePreset)
        assertEquals(preset.taxonGroups, state.selectedTaxonGroups)
        assertEquals(preset.barrierTypes, state.selectedBarrierTypes)
    }

    @Test
    fun resetFilters_restoresDefaults() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.applyPreset(ConflictRegionPreset.SNAKE_RIVER_DAMS)
        viewModel.toggleWildlifeOverlay()
        viewModel.resetFilters()

        val state = viewModel.uiState.value
        assertTrue(state.showWildlifeOccurrences)
        assertTrue(state.showCollisionHotspots)
        assertTrue(state.showBarriers)
        assertTrue(state.showPopulationDensity)
        assertNull(state.activePreset)
        assertEquals(5, state.selectedTaxonGroups.size)
        assertEquals(BarrierType.entries.size, state.selectedBarrierTypes.size)
    }

    @Test
    fun updateMapCenterAndZoom_updatesCoordinates() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val newCenter = GeoLocation(45.0, -110.0)
        viewModel.updateMapCenterAndZoom(newCenter, 10.0f)

        val state = viewModel.uiState.value
        assertEquals(newCenter, state.mapCenter)
        assertEquals(10.0f, state.zoomLevel)
    }

    private class FakeCollisionReportRepository(
        var result: List<CollisionReport>?
    ) : CollisionReportRepository {
        val requestedBounds = mutableListOf<BoundingBox>()

        override suspend fun getCollisionReports(bounds: BoundingBox): List<CollisionReport>? {
            requestedBounds += bounds
            return result
        }
    }

    private fun report(id: String, lat: Double, lon: Double) = CollisionReport(
        id = id,
        species = "Odocoileus virginianus",
        commonName = "White-tailed Deer",
        taxonGroup = "Mammals",
        location = GeoLocation(lat, lon),
        observedOn = "2026-09-01",
        source = "test"
    )

    private fun viewModelWith(repository: CollisionReportRepository) = MapViewModel(
        wildlifeRepository = WildlifeRepositoryImpl(),
        barrierRepository = BarrierRepositoryImpl(),
        conflictMatrixRepository = ConflictMatrixRepositoryImpl(),
        collisionReportRepository = repository
    )

    @Test
    fun loadMapData_includesWildlifeCrossings() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.allWildlifeCrossings.isNotEmpty())
        assertEquals(state.allWildlifeCrossings, state.filteredWildlifeCrossings)

        viewModel.toggleCrossingsOverlay()
        assertTrue(viewModel.uiState.value.filteredWildlifeCrossings.isEmpty())
    }

    @Test
    fun collisionReports_loadForTheVisibleArea() = runTest {
        val repository = FakeCollisionReportRepository(listOf(report("a", 38.9, -92.3), report("b", 38.7, -91.4)))
        val vm = viewModelWith(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(CollisionReportStatus.LOADED, state.collisionReportStatus)
        assertEquals(2, state.filteredCollisionReports.size)
        assertTrue(repository.requestedBounds.single().contains(state.mapCenter))
    }

    @Test
    fun collisionReports_refetchAfterMovingAndDebounceRapidMoves() = runTest {
        val repository = FakeCollisionReportRepository(emptyList())
        val vm = viewModelWith(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, repository.requestedBounds.size)

        // A burst of camera updates (a drag) results in a single request once things settle.
        for (i in 1..20) vm.updateMapCenterAndZoom(GeoLocation(45.0 + i * 0.01, -110.0), 9f)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, repository.requestedBounds.size)
        assertTrue(repository.requestedBounds.last().contains(GeoLocation(45.2, -110.0)))
    }

    @Test
    fun collisionReports_keepExistingDataWhenSourceIsUnavailable() = runTest {
        val repository = FakeCollisionReportRepository(listOf(report("a", 38.9, -92.3)))
        val vm = viewModelWith(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        repository.result = null
        vm.updateMapCenterAndZoom(GeoLocation(47.0, -121.0), 8f)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(CollisionReportStatus.UNAVAILABLE, state.collisionReportStatus)
        assertEquals(1, state.collisionReports.size)
    }

    @Test
    fun toggleCollisionReports_hidesLayer() = runTest {
        val vm = viewModelWith(FakeCollisionReportRepository(listOf(report("a", 38.9, -92.3))))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value.filteredCollisionReports.isNotEmpty())

        vm.toggleCollisionReportsOverlay()
        assertFalse(vm.uiState.value.showCollisionReports)
        assertTrue(vm.uiState.value.filteredCollisionReports.isEmpty())
    }

    @Test
    fun zoomButtons_stepWholeLevelsWithinLimits() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateMapCenterAndZoom(GeoLocation(38.0, -92.0), 17.5f)
        viewModel.zoomIn()
        assertEquals(18f, viewModel.uiState.value.zoomLevel)
        viewModel.updateMapCenterAndZoom(GeoLocation(38.0, -92.0), 2.5f)
        viewModel.zoomOut()
        assertEquals(2f, viewModel.uiState.value.zoomLevel)
    }
}
