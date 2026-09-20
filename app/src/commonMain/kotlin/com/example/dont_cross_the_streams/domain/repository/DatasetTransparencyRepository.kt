package com.example.dont_cross_the_streams.domain.repository

import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo
import kotlinx.coroutines.flow.Flow

interface DatasetTransparencyRepository {
    fun getAllDataSources(): Flow<List<DataSourceInfo>>
    fun getDataSourceById(id: String): Flow<DataSourceInfo?>
    fun getDataSourcesByCategory(category: DataSourceCategory): Flow<List<DataSourceInfo>>
    fun searchDataSources(query: String): Flow<List<DataSourceInfo>>
}
