package com.example.dont_cross_the_streams.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun selectTab(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun navigateToRiskMatrixForRegion(regionId: String) {
        _uiState.update {
            it.copy(
                currentTab = MainTab.RISK_MATRIX,
                selectedRegionIdForMatrix = regionId
            )
        }
    }

    fun navigateToTransparencyHub(dataSourceId: String? = null) {
        _uiState.update {
            it.copy(
                currentTab = MainTab.TRANSPARENCY_HUB,
                selectedDataSourceIdForHub = dataSourceId
            )
        }
    }
}
