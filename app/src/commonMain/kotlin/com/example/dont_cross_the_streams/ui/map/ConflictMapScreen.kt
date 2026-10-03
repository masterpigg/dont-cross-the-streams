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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictMapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = remember { MapViewModel() },
    onNavigateToTransparencyHub: ((String?) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    ConflictMapScreenContent(
        uiState = uiState,
        onToggleWildlife = viewModel::toggleWildlifeOverlay,
        onToggleHotspots = viewModel::toggleHotspotsOverlay,
        onToggleBarriers = viewModel::toggleBarriersOverlay,
        onTogglePopulation = viewModel::togglePopulationDensityOverlay,
        onToggleCrossings = viewModel::toggleCrossingsOverlay,
        onToggleCollisionReports = viewModel::toggleCollisionReportsOverlay,
        onToggleMonth = viewModel::toggleMonth,
        onClearMonths = viewModel::clearMonths,
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
        onNavigateToTransparencyHub = onNavigateToTransparencyHub,
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
    onToggleCrossings: () -> Unit = {},
    onToggleCollisionReports: () -> Unit = {},
    onToggleMonth: (Int) -> Unit = {},
    onClearMonths: () -> Unit = {},
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
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit = { _, _ -> },
    onNavigateToTransparencyHub: ((String?) -> Unit)? = null
) {
    val defaults = remember { MapUiState() }
    val activeFilterCount = (defaults.selectedTaxonGroups - uiState.selectedTaxonGroups).size +
        (BarrierType.entries.size - uiState.selectedBarrierTypes.size) +
        (if (uiState.selectedMonths.isEmpty()) 0 else 1)

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
                            text = uiState.activePreset?.title ?: "Live data: iNaturalist · GBIF · OpenStreetMap · NBI · Census",
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
            InteractiveConflictMap(
                mapCenter = uiState.mapCenter,
                zoomLevel = uiState.zoomLevel,
                panOffsetX = uiState.panOffsetX,
                panOffsetY = uiState.panOffsetY,
                wildlifeOccurrences = uiState.filteredWildlifeOccurrences,
                collisionHotspots = uiState.filteredCollisionHotspots,
                barriers = uiState.filteredBarriers,
                populationZones = uiState.filteredPopulationZones,
                wildlifeCrossings = uiState.filteredWildlifeCrossings,
                collisionReports = uiState.filteredCollisionReports,
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

            LayerToggleBar(
                showWildlife = uiState.showWildlifeOccurrences,
                showHotspots = uiState.showCollisionHotspots,
                showBarriers = uiState.showBarriers,
                showPopulation = uiState.showPopulationDensity,
                showCrossings = uiState.showWildlifeCrossings,
                showCollisionReports = uiState.showCollisionReports,
                layerSummary = { layer -> layerSummary(uiState, layer) },
                hotspotCount = uiState.filteredCollisionHotspots.size,
                onToggleWildlife = onToggleWildlife,
                onToggleHotspots = onToggleHotspots,
                onToggleBarriers = onToggleBarriers,
                onTogglePopulation = onTogglePopulation,
                onToggleCrossings = onToggleCrossings,
                onToggleCollisionReports = onToggleCollisionReports,
                onOpenFilterSheet = { onShowFilterSheet(true) },
                activePresetName = uiState.activePreset?.title,
                activeFilterCount = activeFilterCount,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                selectedTaxonGroups = uiState.selectedTaxonGroups,
                selectedBarrierTypes = uiState.selectedBarrierTypes,
                activePreset = uiState.activePreset,
                selectedMonths = uiState.selectedMonths,
                onToggleMonth = onToggleMonth,
                onClearMonths = onClearMonths,
                onToggleTaxonGroup = onToggleTaxonGroup,
                onToggleBarrierType = onToggleBarrierType,
                onSelectPreset = onSelectPreset,
                onResetFilters = onResetFilters,
                onDismiss = { onShowFilterSheet(false) }
            )
        }

        uiState.selectedFeature?.let { selected ->
            FeatureDetailBottomSheet(
                feature = selected,
                onDismiss = { onFeatureSelected(null) },
                onNavigateToTransparencyHub = onNavigateToTransparencyHub,
                crossings = uiState.allWildlifeCrossings,
                hotspots = uiState.allCollisionHotspots,
                collisionReports = uiState.filteredCollisionReportsAll
            )
        }
    }
}

/** Chip text for a layer: its live count, or why there is nothing to show yet. */
internal fun layerSummary(state: MapUiState, layer: MapLayer): String {
    val count = when (layer) {
        MapLayer.WILDLIFE -> state.filteredWildlifeOccurrences.size
        MapLayer.COLLISIONS -> state.filteredCollisionReportsAll.size
        MapLayer.INFRASTRUCTURE -> state.allBarriers.size
        MapLayer.STRUCTURES -> state.allWildlifeCrossings.size
        MapLayer.POPULATION -> state.allPopulationZones.size
    }
    return when (state.statusOf(layer)) {
        LayerLoadState.LOADING -> "loading…"
        LayerLoadState.ZOOM_IN -> if (count > 0) "$count · zoom in to update" else "zoom in"
        LayerLoadState.UNAVAILABLE -> if (count > 0) "$count · offline" else "unavailable"
        LayerLoadState.IDLE, LayerLoadState.LOADED -> "$count"
    }
}

