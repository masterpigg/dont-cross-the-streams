package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.RiskLevel
import com.example.dont_cross_the_streams.ui.risk.RiskMatrixViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RiskMatrixViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ConflictMatrixRepositoryImpl
    private lateinit var viewModel: RiskMatrixViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = ConflictMatrixRepositoryImpl()
        viewModel = RiskMatrixViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadRiskMatrixScores_populatesRegionsAndSelectedScore() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertTrue(state.regions.isNotEmpty())
        assertNotNull(state.selectedScore)
        assertEquals(state.regions.first().id, state.selectedScore?.id)
    }

    @Test
    fun updateSearchQuery_filtersRegionsByTitle() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateSearchQuery("Vail Pass")

        val state = viewModel.uiState.value
        assertTrue(state.filteredRegions.isNotEmpty())
        assertTrue(state.filteredRegions.all { it.region.contains("Vail Pass", ignoreCase = true) })
    }

    @Test
    fun setFilterRiskLevel_filtersHighRiskRegions() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setFilterRiskLevel(RiskLevel.HIGH)

        val state = viewModel.uiState.value
        assertEquals(RiskLevel.HIGH, state.filterRiskLevel)
        assertTrue(state.filteredRegions.all { it.riskLevel == RiskLevel.HIGH })
    }

    @Test
    fun selectRegion_updatesSelectedScore() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val targetScore = viewModel.uiState.value.regions.last()

        viewModel.selectRegion(targetScore.id)
        val state = viewModel.uiState.value

        assertEquals(targetScore.id, state.selectedScore?.id)
    }
}
