package com.example.dont_cross_the_streams.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

// Same renderer and gestures as the web build. The iOS target has no HTTP client yet, so it runs
// without raster tiles (CanvasConflictMap draws a moving lat/lon graticule instead); plugging in a
// MapTileLoader backed by NSURLSession would bring the street map here too.
@Composable
actual fun InteractiveConflictMap(
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
    modifier: Modifier,
    onPanDirection: (dLat: Double, dLon: Double) -> Unit,
    onSelectPreset: (ConflictRegionPreset) -> Unit,
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit,
    wildlifeCrossings: List<WildlifeCrossing>,
    collisionReports: List<CollisionReport>
) {
    CanvasConflictMap(
        mapCenter = mapCenter,
        zoomLevel = zoomLevel,
        wildlifeOccurrences = wildlifeOccurrences,
        collisionHotspots = collisionHotspots,
        barriers = barriers,
        populationZones = populationZones,
        wildlifeCrossings = wildlifeCrossings,
        collisionReports = collisionReports,
        selectedFeature = selectedFeature,
        onResetView = onResetView,
        onFeatureSelected = onFeatureSelected,
        onSelectPreset = onSelectPreset,
        onMapCenterAndZoomChanged = onMapCenterAndZoomChanged,
        modifier = modifier
    )
}
