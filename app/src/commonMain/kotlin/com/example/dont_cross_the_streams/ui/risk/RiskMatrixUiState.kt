package com.example.dont_cross_the_streams.ui.risk

import com.example.dont_cross_the_streams.domain.model.StudyArea
import com.example.dont_cross_the_streams.domain.model.StudyAreaStats
import com.example.dont_cross_the_streams.domain.model.StudyAreas

/** State for the Study Areas tab: live, measured counts per jurisdiction. */
data class RiskMatrixUiState(
    val areas: List<StudyArea> = StudyAreas.all,
    val stats: Map<String, StudyAreaStats> = emptyMap(),
    val loadingAreaIds: Set<String> = emptySet(),
    val selectedAreaId: String? = null
) {
    val isLoading: Boolean get() = loadingAreaIds.isNotEmpty()
}
