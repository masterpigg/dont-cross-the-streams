package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.ui.common.ViewModel
import com.example.dont_cross_the_streams.data.repository.BarrierRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.CollisionReportRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.WildlifeRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.repository.BarrierRepository
import com.example.dont_cross_the_streams.domain.repository.CollisionReportRepository
import com.example.dont_cross_the_streams.domain.repository.ConflictMatrixRepository
import com.example.dont_cross_the_streams.domain.repository.WildlifeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val wildlifeRepository: WildlifeRepository = WildlifeRepositoryImpl(),
    private val barrierRepository: BarrierRepository = BarrierRepositoryImpl(),
    private val conflictMatrixRepository: ConflictMatrixRepository = ConflictMatrixRepositoryImpl(),
    private val collisionReportRepository: CollisionReportRepository = CollisionReportRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var collisionJob: Job? = null
    private var lastCollisionBounds: BoundingBox? = null

    init {
        loadMapData()
        scheduleCollisionRefresh(immediate = true)
    }

    fun loadMapData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                combine(
                    wildlifeRepository.getWildlifeOccurrences(),
                    wildlifeRepository.getCollisionHotspots(),
                    barrierRepository.getBarrierFeatures(),
                    conflictMatrixRepository.getPopulationDensityZones(),
                    barrierRepository.getWildlifeCrossings()
                ) { occurrences, hotspots, barriers, popZones, crossings ->
                    { state: MapUiState ->
                        state.copy(
                            allWildlifeOccurrences = occurrences,
                            allCollisionHotspots = hotspots,
                            allBarriers = barriers,
                            allPopulationZones = popZones,
                            allWildlifeCrossings = crossings,
                            isLoading = false
                        )
                    }
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error loading map data") }
                }.collect { applyData ->
                    _uiState.update(applyData)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unexpected error") }
            }
        }
    }

    fun toggleWildlifeOverlay() {
        _uiState.update { it.copy(showWildlifeOccurrences = !it.showWildlifeOccurrences) }
    }

    fun toggleHotspotsOverlay() {
        _uiState.update { it.copy(showCollisionHotspots = !it.showCollisionHotspots) }
    }

    fun toggleBarriersOverlay() {
        _uiState.update { it.copy(showBarriers = !it.showBarriers) }
    }

    fun togglePopulationDensityOverlay() {
        _uiState.update { it.copy(showPopulationDensity = !it.showPopulationDensity) }
    }

    fun toggleCrossingsOverlay() {
        _uiState.update { it.copy(showWildlifeCrossings = !it.showWildlifeCrossings) }
    }

    fun toggleCollisionReportsOverlay() {
        _uiState.update { it.copy(showCollisionReports = !it.showCollisionReports) }
        scheduleCollisionRefresh(immediate = true)
    }

    fun toggleTaxonGroup(taxonGroup: String) {
        _uiState.update { currentState ->
            val current = currentState.selectedTaxonGroups.toMutableSet()
            if (current.contains(taxonGroup)) {
                current.remove(taxonGroup)
            } else {
                current.add(taxonGroup)
            }
            currentState.copy(selectedTaxonGroups = current, activePreset = null)
        }
    }

    fun toggleBarrierType(barrierType: BarrierType) {
        _uiState.update { currentState ->
            val current = currentState.selectedBarrierTypes.toMutableSet()
            if (current.contains(barrierType)) {
                current.remove(barrierType)
            } else {
                current.add(barrierType)
            }
            currentState.copy(selectedBarrierTypes = current, activePreset = null)
        }
    }

    fun applyPreset(preset: ConflictRegionPreset) {
        _uiState.update { currentState ->
            currentState.copy(
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
        scheduleCollisionRefresh()
    }

    fun selectFeature(feature: MapFeatureSelection?) {
        _uiState.update { it.copy(selectedFeature = feature) }
    }

    fun showFilterSheet(show: Boolean) {
        _uiState.update { it.copy(isFilterSheetVisible = show) }
    }

    fun setPanOffset(dx: Float, dy: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                panOffsetX = currentState.panOffsetX + dx,
                panOffsetY = currentState.panOffsetY + dy
            )
        }
    }

    fun panDirection(dLat: Double, dLon: Double) {
        _uiState.update { currentState ->
            // Move a fixed share of the screen per click so the step feels the same at every zoom.
            val newCenter = WebMercator.panBy(
                center = currentState.mapCenter,
                zoom = currentState.zoomLevel.toDouble(),
                dx = -dLon * PAN_STEP_PX,
                dy = dLat * PAN_STEP_PX
            )
            currentState.copy(mapCenter = newCenter, panOffsetX = 0f, panOffsetY = 0f)
        }
        scheduleCollisionRefresh()
    }

    fun zoomIn() {
        _uiState.update { currentState ->
            currentState.copy(zoomLevel = (currentState.zoomLevel + 1f).coerceAtMost(WebMercator.MAX_ZOOM))
        }
        scheduleCollisionRefresh()
    }

    fun zoomOut() {
        _uiState.update { currentState ->
            currentState.copy(zoomLevel = (currentState.zoomLevel - 1f).coerceAtLeast(WebMercator.MIN_ZOOM))
        }
        scheduleCollisionRefresh()
    }

    fun resetView() {
        _uiState.update { currentState ->
            currentState.copy(
                mapCenter = GeoLocation(39.8283, -98.5795),
                zoomLevel = 4.5f,
                panOffsetX = 0f,
                panOffsetY = 0f,
                activePreset = null
            )
        }
        scheduleCollisionRefresh()
    }

    fun updateMapCenterAndZoom(center: GeoLocation, zoom: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                mapCenter = center,
                zoomLevel = zoom.coerceIn(WebMercator.MIN_ZOOM, WebMercator.MAX_ZOOM),
                panOffsetX = 0f,
                panOffsetY = 0f
            )
        }
        scheduleCollisionRefresh()
    }

    /**
     * Collision reports are fetched for the area on screen, so they follow the viewport. Gestures
     * fire many updates per second; the debounce waits for the map to settle before hitting the API.
     */
    private fun scheduleCollisionRefresh(immediate: Boolean = false) {
        val state = _uiState.value
        if (!state.showCollisionReports) {
            collisionJob?.cancel()
            return
        }
        val bounds = WebMercator.visibleBounds(
            center = state.mapCenter,
            zoom = state.zoomLevel.toDouble(),
            widthPx = ASSUMED_VIEWPORT_WIDTH_PX,
            heightPx = ASSUMED_VIEWPORT_HEIGHT_PX
        )
        val last = lastCollisionBounds
        if (last != null && last.covers(bounds) && last.areaDeg2() <= bounds.areaDeg2() * MAX_REUSE_AREA_RATIO) {
            // Still inside the last fetch and not zoomed in far enough to need denser local results.
            return
        }

        collisionJob?.cancel()
        collisionJob = viewModelScope.launch {
            if (!immediate) delay(COLLISION_REFRESH_DEBOUNCE_MS)
            _uiState.update { it.copy(collisionReportStatus = CollisionReportStatus.LOADING) }
            val reports = collisionReportRepository.getCollisionReports(bounds)
            if (reports == null) {
                // Keep whatever was already on the map; it is still real data.
                _uiState.update { it.copy(collisionReportStatus = CollisionReportStatus.UNAVAILABLE) }
            } else {
                lastCollisionBounds = bounds
                _uiState.update {
                    it.copy(collisionReports = reports, collisionReportStatus = CollisionReportStatus.LOADED)
                }
            }
        }
    }

    fun resetFilters() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedTaxonGroups = setOf("Mammals", "Birds", "Reptiles", "Fish", "Amphibians"),
                selectedBarrierTypes = BarrierType.entries.toSet(),
                showWildlifeOccurrences = true,
                showCollisionHotspots = true,
                showBarriers = true,
                showPopulationDensity = true,
                showWildlifeCrossings = true,
                showCollisionReports = true,
                activePreset = null
            )
        }
        scheduleCollisionRefresh(immediate = true)
    }
}

private const val PAN_STEP_PX = 160.0
private const val ASSUMED_VIEWPORT_WIDTH_PX = 1600.0
private const val ASSUMED_VIEWPORT_HEIGHT_PX = 1000.0
private const val COLLISION_REFRESH_DEBOUNCE_MS = 700L
private const val MAX_REUSE_AREA_RATIO = 16.0

private fun BoundingBox.covers(other: BoundingBox): Boolean =
    other.minLat >= minLat && other.maxLat <= maxLat && other.minLon >= minLon && other.maxLon <= maxLon

private fun BoundingBox.areaDeg2(): Double = (maxLat - minLat) * (maxLon - minLon)
