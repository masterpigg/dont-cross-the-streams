package com.example.dont_cross_the_streams.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dont_cross_the_streams.ui.map.ConflictMapScreen
import com.example.dont_cross_the_streams.ui.risk.RiskMatrixScreen
import com.example.dont_cross_the_streams.ui.risk.RiskMatrixViewModel
import com.example.dont_cross_the_streams.ui.transparency.TransparencyHubScreen
import com.example.dont_cross_the_streams.ui.transparency.TransparencyHubViewModel

sealed interface NavRoute {
    data object Map : NavRoute
    data class RiskMatrix(val regionId: String? = null) : NavRoute
    data class TransparencyHub(val dataSourceId: String? = null) : NavRoute
}

data class NavigationTabItem(
    val tab: MainTab,
    val title: String,
    val icon: ImageVector,
    val route: NavRoute
)

@Composable
fun MainAdaptiveScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = remember { MainViewModel() },
    riskMatrixViewModel: RiskMatrixViewModel = remember { RiskMatrixViewModel() },
    transparencyHubViewModel: TransparencyHubViewModel = remember { TransparencyHubViewModel() }
) {
    val mainUiState by mainViewModel.uiState.collectAsState()

    MainAdaptiveScreenContent(
        currentTab = mainUiState.currentTab,
        selectedRegionIdForMatrix = mainUiState.selectedRegionIdForMatrix,
        selectedDataSourceIdForHub = mainUiState.selectedDataSourceIdForHub,
        onTabSelected = mainViewModel::selectTab,
        onNavigateToTransparencyHub = mainViewModel::navigateToTransparencyHub,
        riskMatrixViewModel = riskMatrixViewModel,
        transparencyHubViewModel = transparencyHubViewModel,
        modifier = modifier
    )
}

@Composable
fun MainAdaptiveScreenContent(
    currentTab: MainTab,
    selectedRegionIdForMatrix: String?,
    selectedDataSourceIdForHub: String? = null,
    onTabSelected: (MainTab) -> Unit,
    onNavigateToTransparencyHub: (String?) -> Unit = {},
    riskMatrixViewModel: RiskMatrixViewModel = remember { RiskMatrixViewModel() },
    transparencyHubViewModel: TransparencyHubViewModel = remember { TransparencyHubViewModel() },
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            NavigationTabItem(
                tab = MainTab.MAP,
                title = "Conflict Map",
                icon = Icons.Rounded.Map,
                route = NavRoute.Map
            ),
            NavigationTabItem(
                tab = MainTab.RISK_MATRIX,
                title = "Risk Matrix",
                icon = Icons.Rounded.Analytics,
                route = NavRoute.RiskMatrix()
            ),
            NavigationTabItem(
                tab = MainTab.TRANSPARENCY_HUB,
                title = "Data Sources",
                icon = Icons.Rounded.Storage,
                route = NavRoute.TransparencyHub()
            )
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                ) {
                    tabs.forEach { item ->
                        NavigationRailItem(
                            selected = currentTab == item.tab,
                            onClick = { onTabSelected(item.tab) },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    MainTabContent(
                        currentTab = currentTab,
                        selectedRegionIdForMatrix = selectedRegionIdForMatrix,
                        selectedDataSourceIdForHub = selectedDataSourceIdForHub,
                        onNavigateToTransparencyHub = onNavigateToTransparencyHub,
                        riskMatrixViewModel = riskMatrixViewModel,
                        transparencyHubViewModel = transparencyHubViewModel
                    )
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        tabs.forEach { item ->
                            NavigationBarItem(
                                selected = currentTab == item.tab,
                                onClick = { onTabSelected(item.tab) },
                                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                label = { Text(item.title, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MainTabContent(
                        currentTab = currentTab,
                        selectedRegionIdForMatrix = selectedRegionIdForMatrix,
                        selectedDataSourceIdForHub = selectedDataSourceIdForHub,
                        onNavigateToTransparencyHub = onNavigateToTransparencyHub,
                        riskMatrixViewModel = riskMatrixViewModel,
                        transparencyHubViewModel = transparencyHubViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun MainTabContent(
    currentTab: MainTab,
    selectedRegionIdForMatrix: String?,
    selectedDataSourceIdForHub: String?,
    onNavigateToTransparencyHub: (String?) -> Unit,
    riskMatrixViewModel: RiskMatrixViewModel,
    transparencyHubViewModel: TransparencyHubViewModel
) {
    when (currentTab) {
        MainTab.MAP -> ConflictMapScreen(
            onNavigateToTransparencyHub = onNavigateToTransparencyHub
        )
        MainTab.RISK_MATRIX -> RiskMatrixScreen(
            viewModel = riskMatrixViewModel,
            initialRegionId = selectedRegionIdForMatrix
        )
        MainTab.TRANSPARENCY_HUB -> TransparencyHubScreen(
            viewModel = transparencyHubViewModel,
            initialDataSourceId = selectedDataSourceIdForHub
        )
    }
}
