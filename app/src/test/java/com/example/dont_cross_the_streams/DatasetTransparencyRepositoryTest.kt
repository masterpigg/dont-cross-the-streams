package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.repository.DatasetTransparencyRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DatasetTransparencyRepositoryTest {

    private lateinit var repository: DatasetTransparencyRepositoryImpl

    @Before
    fun setUp() {
        repository = DatasetTransparencyRepositoryImpl()
    }

    @Test
    fun getAllDataSources_returnsSixteenDataSources() = runTest {
        val dataSources = repository.getAllDataSources().first()
        assertEquals(16, dataSources.size)
    }

    @Test
    fun getDataSourceById_returnsCorrectDataSource() = runTest {
        val gbif = repository.getDataSourceById("gbif").first()
        assertNotNull(gbif)
        assertEquals("GBIF (Global Biodiversity Information Facility)", gbif?.name)
    }

    @Test
    fun getDataSourcesByCategory_returnsMatchingDataSources() = runTest {
        val observations = repository.getDataSourcesByCategory(DataSourceCategory.WILDLIFE_OBSERVATION).first()
        assertTrue(observations.isNotEmpty())
        assertTrue(observations.all { it.category == DataSourceCategory.WILDLIFE_OBSERVATION })
    }

    @Test
    fun searchDataSources_returnsFilteredDataSources() = runTest {
        val searchResults = repository.searchDataSources("Census").first()
        assertTrue(searchResults.isNotEmpty())
        assertTrue(searchResults.any { it.id == "us_census" })
    }
}
