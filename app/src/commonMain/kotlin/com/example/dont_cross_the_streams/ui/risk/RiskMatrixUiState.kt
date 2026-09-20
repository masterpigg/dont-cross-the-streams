package com.example.dont_cross_the_streams.ui.risk

import com.example.dont_cross_the_streams.domain.model.RiskLevel
import com.example.dont_cross_the_streams.domain.model.RiskMatrixScore

data class RiskMatrixUiState(
    val regions: List<RiskMatrixScore> = emptyList(),
    val filteredRegions: List<RiskMatrixScore> = emptyList(),
    val selectedScore: RiskMatrixScore? = null,
    val searchQuery: String = "",
    val filterRiskLevel: RiskLevel? = null,
    val isLoading: Boolean = false
)
