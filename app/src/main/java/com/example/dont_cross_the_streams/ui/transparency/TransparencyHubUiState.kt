package com.example.dont_cross_the_streams.ui.transparency

import com.example.dont_cross_the_streams.domain.model.AuthType
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo

data class TransparencyHubUiState(
    val dataSources: List<DataSourceInfo> = emptyList(),
    val filteredDataSources: List<DataSourceInfo> = emptyList(),
    val selectedDataSource: DataSourceInfo? = null,
    val searchQuery: String = "",
    val selectedCategory: DataSourceCategory? = null,
    val selectedAuthType: AuthType? = null,
    val isLoading: Boolean = false
)
