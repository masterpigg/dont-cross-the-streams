package com.example.dont_cross_the_streams.ui.transparency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dont_cross_the_streams.data.repository.DatasetTransparencyRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.AuthType
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo
import com.example.dont_cross_the_streams.domain.repository.DatasetTransparencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransparencyHubViewModel(
    private val repository: DatasetTransparencyRepository = DatasetTransparencyRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransparencyHubUiState(isLoading = true))
    val uiState: StateFlow<TransparencyHubUiState> = _uiState.asStateFlow()

    init {
        loadDataSources()
    }

    fun loadDataSources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllDataSources().collect { sources ->
                val initialSelected = sources.firstOrNull()
                _uiState.update { currentState ->
                    val filtered = applyFilterAndSearch(
                        sources,
                        currentState.searchQuery,
                        currentState.selectedCategory,
                        currentState.selectedAuthType
                    )
                    currentState.copy(
                        dataSources = sources,
                        filteredDataSources = filtered,
                        selectedDataSource = currentState.selectedDataSource ?: initialSelected,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectDataSource(id: String?) {
        val found = _uiState.value.dataSources.find { it.id.equals(id, ignoreCase = true) }
        _uiState.update { it.copy(selectedDataSource = found) }
    }

    fun selectDataSourceObj(dataSource: DataSourceInfo?) {
        _uiState.update { it.copy(selectedDataSource = dataSource) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSearch(
                currentState.dataSources,
                query,
                currentState.selectedCategory,
                currentState.selectedAuthType
            )
            currentState.copy(
                searchQuery = query,
                filteredDataSources = filtered
            )
        }
    }

    fun setCategoryFilter(category: DataSourceCategory?) {
        _uiState.update { currentState ->
            val newCategory = if (currentState.selectedCategory == category) null else category
            val filtered = applyFilterAndSearch(
                currentState.dataSources,
                currentState.searchQuery,
                newCategory,
                currentState.selectedAuthType
            )
            currentState.copy(
                selectedCategory = newCategory,
                filteredDataSources = filtered
            )
        }
    }

    fun setAuthTypeFilter(authType: AuthType?) {
        _uiState.update { currentState ->
            val newAuth = if (currentState.selectedAuthType == authType) null else authType
            val filtered = applyFilterAndSearch(
                currentState.dataSources,
                currentState.searchQuery,
                currentState.selectedCategory,
                newAuth
            )
            currentState.copy(
                selectedAuthType = newAuth,
                filteredDataSources = filtered
            )
        }
    }

    private fun applyFilterAndSearch(
        sources: List<DataSourceInfo>,
        query: String,
        category: DataSourceCategory?,
        authType: AuthType?
    ): List<DataSourceInfo> {
        return sources.filter { source ->
            val matchesQuery = query.isBlank() ||
                    source.name.contains(query, ignoreCase = true) ||
                    source.description.contains(query, ignoreCase = true) ||
                    source.keyUtility.contains(query, ignoreCase = true) ||
                    source.baseURL.contains(query, ignoreCase = true) ||
                    source.keyEndpoints.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = category == null || source.category == category
            val matchesAuth = authType == null || source.authType == authType

            matchesQuery && matchesCategory && matchesAuth
        }
    }
}
