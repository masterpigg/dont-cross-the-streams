package com.example.dont_cross_the_streams.ui.main

enum class MainTab {
    MAP,
    RISK_MATRIX,
    TRANSPARENCY_HUB
}

data class MainUiState(
    val currentTab: MainTab = MainTab.MAP,
    val selectedRegionIdForMatrix: String? = null,
    val selectedDataSourceIdForHub: String? = null
)
