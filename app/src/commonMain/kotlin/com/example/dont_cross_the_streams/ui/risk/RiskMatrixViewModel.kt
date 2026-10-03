package com.example.dont_cross_the_streams.ui.risk

import com.example.dont_cross_the_streams.data.repository.LiveGeoDataRepository
import com.example.dont_cross_the_streams.domain.repository.GeoDataRepository
import com.example.dont_cross_the_streams.ui.common.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads measured statistics for each study area from the live APIs (no stored or estimated values). */
class RiskMatrixViewModel(
    private val repository: GeoDataRepository = LiveGeoDataRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiskMatrixUiState())
    val uiState: StateFlow<RiskMatrixUiState> = _uiState.asStateFlow()

    /** Loads on first view of the tab rather than at app start (the county queries are heavy). */
    fun loadIfNeeded() {
        val state = _uiState.value
        if (state.stats.isEmpty() && !state.isLoading) refresh()
    }

    fun refresh() {
        for (area in _uiState.value.areas) {
            _uiState.update { it.copy(loadingAreaIds = it.loadingAreaIds + area.id) }
            viewModelScope.launch {
                val stats = repository.studyAreaStats(area)
                _uiState.update {
                    it.copy(stats = it.stats + (area.id to stats), loadingAreaIds = it.loadingAreaIds - area.id)
                }
            }
        }
    }

    fun selectRegion(areaId: String) {
        if (_uiState.value.areas.any { it.id == areaId }) {
            _uiState.update { it.copy(selectedAreaId = areaId) }
        }
    }
}
