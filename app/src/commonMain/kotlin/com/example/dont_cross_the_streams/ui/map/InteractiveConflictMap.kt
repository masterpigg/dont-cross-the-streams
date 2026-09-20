package com.example.dont_cross_the_streams.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

@Composable
expect fun InteractiveConflictMap(
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    wildlifeOccurrences: List<WildlifeOccurrence>,
    collisionHotspots: List<CollisionHotspot>,
    barriers: List<BarrierFeature>,
    populationZones: List<PopulationDensityZone>,
    selectedFeature: MapFeatureSelection?,
    onPan: (dx: Float, dy: Float) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    onFeatureSelected: (MapFeatureSelection?) -> Unit,
    modifier: Modifier = Modifier,
    onPanDirection: (dLat: Double, dLon: Double) -> Unit = { _, _ -> },
    onSelectPreset: (ConflictRegionPreset) -> Unit = {},
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit = { _, _ -> }
)
