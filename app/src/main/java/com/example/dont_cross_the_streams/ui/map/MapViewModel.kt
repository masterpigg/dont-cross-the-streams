package com.example.dont_cross_the_streams.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dont_cross_the_streams.data.repository.BarrierRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.ConflictMatrixRepositoryImpl
import com.example.dont_cross_the_streams.data.repository.WildlifeRepositoryImpl
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.repository.BarrierRepository
import com.example.dont_cross_the_streams.domain.repository.ConflictMatrixRepository
import com.example.dont_cross_the_streams.domain.repository.WildlifeRepository
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
    private val conflictMatrixRepository: ConflictMatrixRepository = ConflictMatrixRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMapData()
    }

    fun loadMapData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                combine(
                    wildlifeRepository.getWildlifeOccurrences(),
                    wildlifeRepository.getCollisionHotspots(),
                    barrierRepository.getBarrierFeatures(),
                    conflictMatrixRepository.getPopulationDensityZones()
                ) { occurrences, hotspots, barriers, popZones ->
                    Quadruple(occurrences, hotspots, barriers, popZones)
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error loading map data") }
                }.collect { (occurrences, hotspots, barriers, popZones) ->
                    _uiState.update {
                        it.copy(
                            allWildlifeOccurrences = occurrences,
                            allCollisionHotspots = hotspots,
                            allBarriers = barriers,
                            allPopulationZones = popZones,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Unexpected error") }
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
            val scale = 0.5 / Math.pow(2.0, (currentState.zoomLevel - 4f).toDouble())
            val newLat = (currentState.mapCenter.latitude + dLat * scale).coerceIn(-85.0, 85.0)
            val newLon = (currentState.mapCenter.longitude + dLon * scale).coerceIn(-180.0, 180.0)
            currentState.copy(
                mapCenter = GeoLocation(newLat, newLon),
                panOffsetX = 0f,
                panOffsetY = 0f
            )
        }
    }

    fun zoomIn() {
        _uiState.update { currentState ->
            currentState.copy(zoomLevel = (currentState.zoomLevel + 0.8f).coerceAtMost(16f))
        }
    }

    fun zoomOut() {
        _uiState.update { currentState ->
            currentState.copy(zoomLevel = (currentState.zoomLevel - 0.8f).coerceAtLeast(2f))
        }
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
    }

    fun updateMapCenterAndZoom(center: GeoLocation, zoom: Float) {
        _uiState.update { currentState ->
            if (Math.abs(currentState.mapCenter.latitude - center.latitude) > 0.0001 ||
                Math.abs(currentState.mapCenter.longitude - center.longitude) > 0.0001 ||
                Math.abs(currentState.zoomLevel - zoom) > 0.05f) {
                currentState.copy(
                    mapCenter = center,
                    zoomLevel = zoom,
                    panOffsetX = 0f,
                    panOffsetY = 0f
                )
            } else {
                currentState
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
                activePreset = null
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
