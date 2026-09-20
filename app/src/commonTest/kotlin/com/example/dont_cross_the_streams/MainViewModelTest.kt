package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.ui.main.MainTab
import com.example.dont_cross_the_streams.ui.main.MainViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

class MainViewModelTest {

    private val viewModel = MainViewModel()

    @Test
    fun defaultTab_isMap() {
        assertEquals(MainTab.MAP, viewModel.uiState.value.currentTab)
    }

    @Test
    fun selectTab_updatesCurrentTab() {
        viewModel.selectTab(MainTab.TRANSPARENCY_HUB)
        assertEquals(MainTab.TRANSPARENCY_HUB, viewModel.uiState.value.currentTab)
    }

    @Test
    fun navigateToRiskMatrixForRegion_switchesTabAndSetsRegion() {
        viewModel.navigateToRiskMatrixForRegion("rms_002")
        val state = viewModel.uiState.value

        assertEquals(MainTab.RISK_MATRIX, state.currentTab)
        assertEquals("rms_002", state.selectedRegionIdForMatrix)
    }
}
