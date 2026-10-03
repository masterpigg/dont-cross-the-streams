package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.data.analysis.HotspotClustering
import com.example.dont_cross_the_streams.data.repository.LiveGeoDataRepository
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.repository.GeoDataRepository
import com.example.dont_cross_the_streams.ui.common.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: GeoDataRepository = LiveGeoDataRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val layerJobs = mutableMapOf<MapLayer, Job>()

    /** Area (and zoom) each layer was last fetched for, so small pans don't re-query the APIs. */
    private val loadedFor = mutableMapOf<MapLayer, Pair<BoundingBox, Float>>()

    init {
        refreshViewport(immediate = true)
    }

    // ---- Layer toggles -------------------------------------------------------------------------

    fun toggleWildlifeOverlay() = toggle { it.copy(showWildlifeOccurrences = !it.showWildlifeOccurrences) }

    fun toggleHotspotsOverlay() = toggle { it.copy(showCollisionHotspots = !it.showCollisionHotspots) }

    fun toggleBarriersOverlay() = toggle { it.copy(showBarriers = !it.showBarriers) }

    fun togglePopulationDensityOverlay() = toggle { it.copy(showPopulationDensity = !it.showPopulationDensity) }

    fun toggleCrossingsOverlay() = toggle { it.copy(showWildlifeCrossings = !it.showWildlifeCrossings) }

    fun toggleCollisionReportsOverlay() = toggle { it.copy(showCollisionReports = !it.showCollisionReports) }

    private fun toggle(change: (MapUiState) -> MapUiState) {
        _uiState.update(change)
        refreshViewport(immediate = true)
    }

    // ---- Filters -------------------------------------------------------------------------------

    fun toggleTaxonGroup(taxonGroup: String) {
        _uiState.update { state ->
            val groups = state.selectedTaxonGroups.toMutableSet()
            if (!groups.remove(taxonGroup)) groups.add(taxonGroup)
            state.copy(selectedTaxonGroups = groups, activePreset = null)
        }
        recomputeHotspots()
    }

    fun toggleBarrierType(barrierType: BarrierType) {
        _uiState.update { state ->
            val types = state.selectedBarrierTypes.toMutableSet()
            if (!types.remove(barrierType)) types.add(barrierType)
            state.copy(selectedBarrierTypes = types, activePreset = null)
        }
    }

    /** Adds or removes a month (1-12) from the seasonal filter. No months selected means all months. */
    fun toggleMonth(month: Int) {
        if (month !in 1..12) return
        _uiState.update { state ->
            val months = state.selectedMonths.toMutableSet()
            if (!months.remove(month)) months.add(month)
            state.copy(selectedMonths = months)
        }
        recomputeHotspots()
    }

    fun clearMonths() {
        _uiState.update { it.copy(selectedMonths = emptySet()) }
        recomputeHotspots()
    }

    fun resetFilters() {
        _uiState.update { state ->
            val defaults = MapUiState()
            state.copy(
                selectedTaxonGroups = defaults.selectedTaxonGroups,
                selectedBarrierTypes = defaults.selectedBarrierTypes,
                selectedMonths = emptySet(),
                showWildlifeOccurrences = true,
                showCollisionHotspots = true,
                showBarriers = true,
                showPopulationDensity = true,
                showWildlifeCrossings = true,
                showCollisionReports = true,
                activePreset = null
            )
        }
        recomputeHotspots()
        refreshViewport(immediate = true)
    }

    // ---- Camera --------------------------------------------------------------------------------

    fun applyPreset(preset: ConflictRegionPreset) {
        _uiState.update { state ->
            state.copy(
                mapCenter = preset.center,
                zoomLevel = preset.zoomLevel,
                panOffsetX = 0f,
                panOffsetY = 0f,
                selectedTaxonGroups = preset.taxonGroups,
                selectedBarrierTypes = preset.barrierTypes,
                activePreset = preset,
                isFilterSheetVisible = false
            )
        }
        recomputeHotspots()
        refreshViewport()
    }

    fun selectFeature(feature: MapFeatureSelection?) {
        _uiState.update { it.copy(selectedFeature = feature) }
    }

    fun showFilterSheet(show: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = show) }
    }

    fun setPanOffset(dx: Float, dy: Float) {
        _uiState.update { it.copy(panOffsetX = it.panOffsetX + dx, panOffsetY = it.panOffsetY + dy) }
    }

    fun panDirection(dLat: Double, dLon: Double) {
        _uiState.update { state ->
            // Move a fixed share of the screen per click so the step feels the same at every zoom.
            val newCenter = WebMercator.panBy(
                center = state.mapCenter,
                zoom = state.zoomLevel.toDouble(),
                dx = -dLon * PAN_STEP_PX,
                dy = dLat * PAN_STEP_PX
            )
            state.copy(mapCenter = newCenter, panOffsetX = 0f, panOffsetY = 0f)
        }
        refreshViewport()
    }

    fun zoomIn() {
        _uiState.update { it.copy(zoomLevel = (it.zoomLevel + 1f).coerceAtMost(WebMercator.MAX_ZOOM)) }
        refreshViewport()
    }

    fun zoomOut() {
        _uiState.update { it.copy(zoomLevel = (it.zoomLevel - 1f).coerceAtLeast(WebMercator.MIN_ZOOM)) }
        refreshViewport()
    }

    fun resetView() {
        _uiState.update {
            it.copy(mapCenter = DEFAULT_MAP_CENTER, zoomLevel = DEFAULT_MAP_ZOOM, panOffsetX = 0f, panOffsetY = 0f, activePreset = null)
        }
        refreshViewport()
    }

    fun updateMapCenterAndZoom(center: GeoLocation, zoom: Float) {
        _uiState.update {
            it.copy(
                mapCenter = center,
                zoomLevel = zoom.coerceIn(WebMercator.MIN_ZOOM, WebMercator.MAX_ZOOM),
                panOffsetX = 0f,
                panOffsetY = 0f
            )
        }
        refreshViewport()
    }

    // ---- Live data -----------------------------------------------------------------------------

    private fun isWanted(layer: MapLayer, state: MapUiState): Boolean = when (layer) {
        MapLayer.WILDLIFE -> state.showWildlifeOccurrences
        MapLayer.COLLISIONS -> state.showCollisionReports || state.showCollisionHotspots
        MapLayer.INFRASTRUCTURE -> state.showBarriers || state.showWildlifeCrossings
        MapLayer.STRUCTURES -> state.showWildlifeCrossings
        MapLayer.POPULATION -> state.showPopulationDensity
    }

    /**
     * Fetches every visible layer for the area on screen. Gestures fire many camera updates per
     * second, so requests wait for the map to settle; an area already covered by the last fetch at a
     * similar zoom is not requested again.
     */
    private fun refreshViewport(immediate: Boolean = false) {
        val state = _uiState.value
        val zoom = state.zoomLevel
        val bounds = WebMercator.visibleBounds(
            center = state.mapCenter,
            zoom = zoom.toDouble(),
            widthPx = ASSUMED_VIEWPORT_WIDTH_PX,
            heightPx = ASSUMED_VIEWPORT_HEIGHT_PX
        )
        for (layer in MapLayer.entries) {
            if (!isWanted(layer, state)) {
                layerJobs.remove(layer)?.cancel()
                continue
            }
            if (zoom < layer.minZoom) {
                layerJobs.remove(layer)?.cancel()
                setStatus(layer, LayerLoadState.ZOOM_IN)
                continue
            }
            val previous = loadedFor[layer]
            val needsCulverts = layer == MapLayer.INFRASTRUCTURE && zoom >= CULVERT_MIN_ZOOM &&
                (previous?.second ?: 0f) < CULVERT_MIN_ZOOM
            if (previous != null && !needsCulverts && previous.first.covers(bounds) &&
                previous.first.areaDeg2() <= bounds.areaDeg2() * MAX_REUSE_AREA_RATIO
            ) {
                if (state.statusOf(layer) == LayerLoadState.ZOOM_IN) setStatus(layer, LayerLoadState.LOADED)
                continue
            }
            layerJobs.remove(layer)?.cancel()
            layerJobs[layer] = viewModelScope.launch {
                if (!immediate) delay(VIEWPORT_DEBOUNCE_MS)
                setStatus(layer, LayerLoadState.LOADING)
                val ok = load(layer, bounds, zoom)
                if (ok) loadedFor[layer] = bounds to zoom
                setStatus(layer, if (ok) LayerLoadState.LOADED else LayerLoadState.UNAVAILABLE)
            }
        }
    }

    /** Loads one layer and replaces its data. Returns false if the source could not be reached. */
    private suspend fun load(layer: MapLayer, bounds: BoundingBox, zoom: Float): Boolean = when (layer) {
        MapLayer.WILDLIFE -> repository.wildlifeObservations(bounds)?.let { list ->
            _uiState.update { it.copy(allWildlifeOccurrences = list) }
        } != null

        MapLayer.COLLISIONS -> repository.collisionReports(bounds)?.let { list ->
            _uiState.update { it.copy(collisionReports = list) }
            recomputeHotspots()
        } != null

        MapLayer.INFRASTRUCTURE -> repository.infrastructure(bounds, includeCulverts = zoom >= CULVERT_MIN_ZOOM)?.let { infra ->
            _uiState.update { it.copy(allBarriers = infra.barriers, osmCrossings = infra.crossings) }
            recomputeHotspots() // hotspot labels name the nearest major road
        } != null

        MapLayer.STRUCTURES -> repository.waterwayStructures(bounds)?.let { list ->
            _uiState.update { it.copy(nbiStructures = list) }
        } != null

        MapLayer.POPULATION -> repository.censusTracts(bounds)?.let { list ->
            _uiState.update { it.copy(allPopulationZones = list) }
        } != null
    }

    private fun setStatus(layer: MapLayer, status: LayerLoadState) {
        _uiState.update { it.copy(layerStatus = it.layerStatus + (layer to status)) }
    }

    private fun recomputeHotspots() {
        _uiState.update { state ->
            state.copy(allCollisionHotspots = HotspotClustering.cluster(state.filteredCollisionReportsAll, state.allBarriers))
        }
    }
}

private const val PAN_STEP_PX = 160.0
private const val ASSUMED_VIEWPORT_WIDTH_PX = 1600.0
private const val ASSUMED_VIEWPORT_HEIGHT_PX = 1000.0
private const val VIEWPORT_DEBOUNCE_MS = 700L
private const val MAX_REUSE_AREA_RATIO = 16.0

private fun BoundingBox.covers(other: BoundingBox): Boolean =
    other.minLat >= minLat && other.maxLat <= maxLat && other.minLon >= minLon && other.maxLon <= maxLon

private fun BoundingBox.areaDeg2(): Double = (maxLat - minLat) * (maxLon - minLon)
