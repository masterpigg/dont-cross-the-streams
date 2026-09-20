package com.example.dont_cross_the_streams.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ConflictMapScreenContent(
        uiState = uiState,
        onToggleWildlife = viewModel::toggleWildlifeOverlay,
        onToggleHotspots = viewModel::toggleHotspotsOverlay,
        onToggleBarriers = viewModel::toggleBarriersOverlay,
        onTogglePopulation = viewModel::togglePopulationDensityOverlay,
        onToggleTaxonGroup = viewModel::toggleTaxonGroup,
        onToggleBarrierType = viewModel::toggleBarrierType,
        onSelectPreset = viewModel::applyPreset,
        onResetFilters = viewModel::resetFilters,
        onShowFilterSheet = { viewModel.showFilterSheet(it) },
        onFeatureSelected = viewModel::selectFeature,
        onPan = viewModel::setPanOffset,
        onPanDirection = viewModel::panDirection,
        onZoomIn = viewModel::zoomIn,
        onZoomOut = viewModel::zoomOut,
        onResetView = viewModel::resetView,
        onMapCenterAndZoomChanged = viewModel::updateMapCenterAndZoom,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictMapScreenContent(
    uiState: MapUiState,
    modifier: Modifier = Modifier,
    onToggleWildlife: () -> Unit = {},
    onToggleHotspots: () -> Unit = {},
    onToggleBarriers: () -> Unit = {},
    onTogglePopulation: () -> Unit = {},
    onToggleTaxonGroup: (String) -> Unit = {},
    onToggleBarrierType: (BarrierType) -> Unit = {},
    onSelectPreset: (ConflictRegionPreset) -> Unit = {},
    onResetFilters: () -> Unit = {},
    onShowFilterSheet: (Boolean) -> Unit = {},
    onFeatureSelected: (MapFeatureSelection?) -> Unit = {},
    onPan: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    onPanDirection: (dLat: Double, dLon: Double) -> Unit = { _, _ -> },
    onZoomIn: () -> Unit = {},
    onZoomOut: () -> Unit = {},
    onResetView: () -> Unit = {},
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit = { _, _ -> }
) {
    val activeFilterCount = (5 - uiState.selectedTaxonGroups.size) + (BarrierType.entries.size - uiState.selectedBarrierTypes.size)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Map,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conflict Map",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = uiState.activePreset?.title ?: "Multi-Layer Ecological Barriers & Hotspots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onResetView) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Map Component
            InteractiveConflictMap(
                mapCenter = uiState.mapCenter,
                zoomLevel = uiState.zoomLevel,
                panOffsetX = uiState.panOffsetX,
                panOffsetY = uiState.panOffsetY,
                wildlifeOccurrences = uiState.filteredWildlifeOccurrences,
                collisionHotspots = uiState.filteredCollisionHotspots,
                barriers = uiState.filteredBarriers,
                populationZones = uiState.filteredPopulationZones,
                selectedFeature = uiState.selectedFeature,
                onPan = onPan,
                onPanDirection = onPanDirection,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onResetView = onResetView,
                onSelectPreset = onSelectPreset,
                onFeatureSelected = onFeatureSelected,
                onMapCenterAndZoomChanged = onMapCenterAndZoomChanged,
                modifier = Modifier.fillMaxSize()
            )

            // Loading Indicator Overlay
            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Loading Spatial Datasets...")
                    }
                }
            }

            // Floating Top Layer Toggle Bar
            LayerToggleBar(
                showWildlife = uiState.showWildlifeOccurrences,
                showHotspots = uiState.showCollisionHotspots,
                showBarriers = uiState.showBarriers,
                showPopulation = uiState.showPopulationDensity,
                onToggleWildlife = onToggleWildlife,
                onToggleHotspots = onToggleHotspots,
                onToggleBarriers = onToggleBarriers,
                onTogglePopulation = onTogglePopulation,
                onOpenFilterSheet = { onShowFilterSheet(true) },
                activePresetName = uiState.activePreset?.title,
                activeFilterCount = activeFilterCount,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Filter Bottom Sheet
        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                selectedTaxonGroups = uiState.selectedTaxonGroups,
                selectedBarrierTypes = uiState.selectedBarrierTypes,
                activePreset = uiState.activePreset,
                onToggleTaxonGroup = onToggleTaxonGroup,
                onToggleBarrierType = onToggleBarrierType,
                onSelectPreset = onSelectPreset,
                onResetFilters = onResetFilters,
                onDismiss = { onShowFilterSheet(false) }
            )
        }

        // Feature Detail Bottom Sheet when a feature is tapped
        uiState.selectedFeature?.let { selected ->
            FeatureDetailBottomSheet(
                feature = selected,
                onDismiss = { onFeatureSelected(null) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConflictMapScreenPreview() {
    DontcrossthestreamsTheme {
        ConflictMapScreenContent(
            uiState = MapUiState(
                mapCenter = GeoLocation(39.8283, -98.5795),
                zoomLevel = 4.5f
            ),
            onToggleWildlife = {},
            onToggleHotspots = {},
            onToggleBarriers = {},
            onTogglePopulation = {},
            onToggleTaxonGroup = {},
            onToggleBarrierType = {},
            onSelectPreset = {},
            onResetFilters = {},
            onShowFilterSheet = {},
            onFeatureSelected = {},
            onPan = { _, _ -> },
            onPanDirection = { _, _ -> },
            onZoomIn = {},
            onZoomOut = {},
            onResetView = {}
        )
    }
}
