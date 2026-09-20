package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.datasource.MockDatasetTransparencyDataSource
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo
import com.example.dont_cross_the_streams.domain.repository.DatasetTransparencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DatasetTransparencyRepositoryImpl : DatasetTransparencyRepository {

    override fun getAllDataSources(): Flow<List<DataSourceInfo>> = flow {
        emit(MockDatasetTransparencyDataSource.dataSources)
    }

    override fun getDataSourceById(id: String): Flow<DataSourceInfo?> = flow {
        val found = MockDatasetTransparencyDataSource.dataSources.find { it.id.equals(id, ignoreCase = true) }
        emit(found)
    }

    override fun getDataSourcesByCategory(category: DataSourceCategory): Flow<List<DataSourceInfo>> = flow {
        val filtered = MockDatasetTransparencyDataSource.dataSources.filter { it.category == category }
        emit(filtered)
    }

    override fun searchDataSources(query: String): Flow<List<DataSourceInfo>> = flow {
        if (query.isBlank()) {
            emit(MockDatasetTransparencyDataSource.dataSources)
        } else {
            val filtered = MockDatasetTransparencyDataSource.dataSources.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.keyUtility.contains(query, ignoreCase = true)
            }
            emit(filtered)
        }
    }
}
