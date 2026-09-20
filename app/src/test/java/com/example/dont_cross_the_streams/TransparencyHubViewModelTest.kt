package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.DatasetTransparencyRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.AuthType
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.ui.transparency.TransparencyHubViewModel
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
class TransparencyHubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: DatasetTransparencyRepositoryImpl
    private lateinit var viewModel: TransparencyHubViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = DatasetTransparencyRepositoryImpl()
        viewModel = TransparencyHubViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadDataSources_populatesAllSixteenSources() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(16, state.dataSources.size)
        assertNotNull(state.selectedDataSource)
    }

    @Test
    fun updateSearchQuery_filtersDataSourcesByText() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateSearchQuery("eBird")

        val state = viewModel.uiState.value
        assertTrue(state.filteredDataSources.isNotEmpty())
        assertTrue(state.filteredDataSources.any { it.id == "ebird" })
    }

    @Test
    fun setCategoryFilter_filtersByWildlifeObservation() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setCategoryFilter(DataSourceCategory.WILDLIFE_OBSERVATION)

        val state = viewModel.uiState.value
        assertEquals(DataSourceCategory.WILDLIFE_OBSERVATION, state.selectedCategory)
        assertTrue(state.filteredDataSources.all { it.category == DataSourceCategory.WILDLIFE_OBSERVATION })
    }

    @Test
    fun setAuthTypeFilter_filtersByPublicOpen() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setAuthTypeFilter(AuthType.NONE_PUBLIC)

        val state = viewModel.uiState.value
        assertEquals(AuthType.NONE_PUBLIC, state.selectedAuthType)
        assertTrue(state.filteredDataSources.all { it.authType == AuthType.NONE_PUBLIC })
    }
}
