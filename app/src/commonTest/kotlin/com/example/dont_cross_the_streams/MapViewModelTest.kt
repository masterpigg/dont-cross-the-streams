package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.BarrierRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.WildlifeRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
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
}
