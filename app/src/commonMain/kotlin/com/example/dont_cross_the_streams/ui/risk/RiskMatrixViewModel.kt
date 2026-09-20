package com.example.dont_cross_the_streams.ui.risk

import com.example.dont_cross_the_streams.ui.common.ViewModel
import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.RiskLevel
import com.example.dont_cross_the_streams.domain.model.RiskMatrixScore
import com.example.dont_cross_the_streams.domain.repository.ConflictMatrixRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RiskMatrixViewModel(
    private val repository: ConflictMatrixRepository = ConflictMatrixRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiskMatrixUiState(isLoading = true))
    val uiState: StateFlow<RiskMatrixUiState> = _uiState.asStateFlow()

    init {
        loadRiskMatrixScores()
    }

    fun loadRiskMatrixScores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllRiskMatrixScores().collect { scores ->
                val initialSelected = scores.firstOrNull()
                _uiState.update { currentState ->
                    val filtered = applyFilterAndSearch(scores, currentState.searchQuery, currentState.filterRiskLevel)
                    currentState.copy(
                        regions = scores,
                        filteredRegions = filtered,
                        selectedScore = currentState.selectedScore ?: initialSelected,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectRegion(regionId: String) {
        val found = _uiState.value.regions.find { it.id == regionId }
        if (found != null) {
            _uiState.update { it.copy(selectedScore = found) }
        }
    }

    fun selectRegionByScore(score: RiskMatrixScore?) {
        _uiState.update { it.copy(selectedScore = score) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = applyFilterAndSearch(currentState.regions, query, currentState.filterRiskLevel)
            currentState.copy(
                searchQuery = query,
                filteredRegions = filtered
            )
        }
    }

    fun setFilterRiskLevel(level: RiskLevel?) {
        _uiState.update { currentState ->
            val newLevel = if (currentState.filterRiskLevel == level) null else level
            val filtered = applyFilterAndSearch(currentState.regions, currentState.searchQuery, newLevel)
            currentState.copy(
                filterRiskLevel = newLevel,
                filteredRegions = filtered
            )
        }
    }

    fun calculateRiskForCustomLocation(location: GeoLocation, name: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getRiskMatrixForLocation(location, name).collect { score ->
                _uiState.update { currentState ->
                    val updatedRegions = listOf(score) + currentState.regions.filter { it.id != score.id }
                    val filtered = applyFilterAndSearch(updatedRegions, currentState.searchQuery, currentState.filterRiskLevel)
                    currentState.copy(
                        regions = updatedRegions,
                        filteredRegions = filtered,
                        selectedScore = score,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun applyFilterAndSearch(
        scores: List<RiskMatrixScore>,
        query: String,
        riskLevel: RiskLevel?
    ): List<RiskMatrixScore> {
        return scores.filter { score ->
            val matchesQuery = query.isBlank() ||
                    score.region.contains(query, ignoreCase = true) ||
                    score.severityAnalysis.contains(query, ignoreCase = true) ||
                    score.severanceCauses.any { it.title.contains(query, ignoreCase = true) }
            val matchesLevel = riskLevel == null || score.riskLevel == riskLevel
            matchesQuery && matchesLevel
        }
    }
}
