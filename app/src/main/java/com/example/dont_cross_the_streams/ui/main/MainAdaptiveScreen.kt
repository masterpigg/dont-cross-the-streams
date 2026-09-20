package com.example.dont_cross_the_streams.ui.main

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.dont_cross_the_streams.ui.map.ConflictMapScreen
import com.example.dont_cross_the_streams.ui.risk.RiskMatrixScreen
import com.example.dont_cross_the_streams.ui.risk.RiskMatrixViewModel
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme
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
    mainViewModel: MainViewModel = viewModel(),
    riskMatrixViewModel: RiskMatrixViewModel = viewModel(),
    transparencyHubViewModel: TransparencyHubViewModel = viewModel()
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    MainAdaptiveScreenContent(
        currentTab = mainUiState.currentTab,
        selectedRegionIdForMatrix = mainUiState.selectedRegionIdForMatrix,
        onTabSelected = mainViewModel::selectTab,
        riskMatrixViewModel = riskMatrixViewModel,
        transparencyHubViewModel = transparencyHubViewModel,
        modifier = modifier
    )
}

@Suppress("DEPRECATION")
@Composable
fun MainAdaptiveScreenContent(
    currentTab: MainTab,
    selectedRegionIdForMatrix: String?,
    onTabSelected: (MainTab) -> Unit,
    riskMatrixViewModel: RiskMatrixViewModel = viewModel(),
    transparencyHubViewModel: TransparencyHubViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val isWideScreen = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

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

    if (isWideScreen) {
        // Wide-screen / Tablet layout: Navigation Rail on the left
        Row(modifier = modifier.fillMaxSize()) {
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
                    riskMatrixViewModel = riskMatrixViewModel,
                    transparencyHubViewModel = transparencyHubViewModel
                )
            }
        }
    } else {
        // Compact / Phone layout: Bottom Navigation Bar
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
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MainTabContent(
                    currentTab = currentTab,
                    selectedRegionIdForMatrix = selectedRegionIdForMatrix,
                    riskMatrixViewModel = riskMatrixViewModel,
                    transparencyHubViewModel = transparencyHubViewModel
                )
            }
        }
    }
}

@Composable
private fun MainTabContent(
    currentTab: MainTab,
    selectedRegionIdForMatrix: String?,
    riskMatrixViewModel: RiskMatrixViewModel,
    transparencyHubViewModel: TransparencyHubViewModel
) {
    // Navigation 3 Backstack rendering via NavDisplay
    val currentKey: NavRoute = when (currentTab) {
        MainTab.MAP -> NavRoute.Map
        MainTab.RISK_MATRIX -> NavRoute.RiskMatrix(selectedRegionIdForMatrix)
        MainTab.TRANSPARENCY_HUB -> NavRoute.TransparencyHub()
    }

    val backStack = remember(currentKey) { mutableStateListOf(currentKey) }

    NavDisplay(
        backStack = backStack,
        entryProvider = { key: NavRoute ->
            when (key) {
                is NavRoute.Map -> NavEntry(key) {
                    ConflictMapScreen()
                }
                is NavRoute.RiskMatrix -> NavEntry(key) {
                    RiskMatrixScreen(
                        viewModel = riskMatrixViewModel,
                        initialRegionId = key.regionId ?: selectedRegionIdForMatrix
                    )
                }
                is NavRoute.TransparencyHub -> NavEntry(key) {
                    TransparencyHubScreen(
                        viewModel = transparencyHubViewModel,
                        initialDataSourceId = key.dataSourceId
                    )
                }
            }
        }
    )
}

@Preview(name = "Phone Layout", showBackground = true)
@Composable
fun MainAdaptiveScreenPhonePreview() {
    DontcrossthestreamsTheme {
        MainAdaptiveScreenContent(
            currentTab = MainTab.RISK_MATRIX,
            selectedRegionIdForMatrix = null,
            onTabSelected = {}
        )
    }
}

@Preview(name = "Wide Tablet Layout", device = "spec:width=1280dp,height=800dp", showBackground = true)
@Composable
fun MainAdaptiveScreenTabletPreview() {
    DontcrossthestreamsTheme {
        MainAdaptiveScreenContent(
            currentTab = MainTab.TRANSPARENCY_HUB,
            selectedRegionIdForMatrix = null,
            onTabSelected = {}
        )
    }
}
