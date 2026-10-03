package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.LiveGeoDataRepository
import com.example.dont_cross_the_streams.domain.model.StudyAreas
import com.example.dont_cross_the_streams.ui.risk.RiskMatrixViewModel
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
class StudyAreasViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun studyAreas_coverStLouisCityCountyAndStCharles() {
        assertEquals(listOf("29-510", "29-189", "29-183"), StudyAreas.all.map { "${it.stateFips}-${it.countyFips}" })
        StudyAreas.all.forEach { area ->
            assertTrue(area.bounds.contains(area.center))
            assertTrue(area.bounds.minLat < area.bounds.maxLat && area.bounds.minLon < area.bounds.maxLon)
        }
    }

    @Test
    fun loadsLiveStatsForEveryArea() = runTest {
        val vm = RiskMatrixViewModel(LiveGeoDataRepository(ApiFixtures.http()))
        assertTrue(vm.uiState.value.stats.isEmpty(), "nothing is requested until the tab is shown")
        vm.loadIfNeeded()
        assertTrue(vm.uiState.value.isLoading)
        dispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(StudyAreas.all.map { it.id }.toSet(), state.stats.keys)
        assertEquals(321, state.stats.getValue("stl_city").deadAnimalReports)
    }

    @Test
    fun offlineSources_leaveValuesEmptyInsteadOfInventingThem() = runTest {
        val vm = RiskMatrixViewModel(LiveGeoDataRepository { null })
        vm.loadIfNeeded()
        dispatcher.scheduler.advanceUntilIdle()
        val stats = vm.uiState.value.stats.getValue("st_charles_county")
        assertNull(stats.deadAnimalReports)
        assertNull(stats.majorRoadKm)
        assertNull(stats.population)
    }
}
