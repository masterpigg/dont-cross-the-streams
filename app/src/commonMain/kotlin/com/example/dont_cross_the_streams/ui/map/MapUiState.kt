package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

enum class CollisionReportStatus { IDLE, LOADING, LOADED, UNAVAILABLE }

data class MapUiState(
    val mapCenter: GeoLocation = GeoLocation(39.8283, -98.5795),
    val zoomLevel: Float = 4.5f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,

    val showWildlifeOccurrences: Boolean = true,
    val showCollisionHotspots: Boolean = true,
    val showBarriers: Boolean = true,
    val showPopulationDensity: Boolean = true,
    val showWildlifeCrossings: Boolean = true,
    val showCollisionReports: Boolean = true,

    val selectedTaxonGroups: Set<String> = setOf("Mammals", "Birds", "Reptiles", "Fish", "Amphibians"),
    val selectedBarrierTypes: Set<BarrierType> = BarrierType.entries.toSet(),
    val activePreset: ConflictRegionPreset? = null,

    val allWildlifeOccurrences: List<WildlifeOccurrence> = emptyList(),
    val allCollisionHotspots: List<CollisionHotspot> = emptyList(),
    val allBarriers: List<BarrierFeature> = emptyList(),
    val allPopulationZones: List<PopulationDensityZone> = emptyList(),
    val allWildlifeCrossings: List<WildlifeCrossing> = emptyList(),
    val collisionReports: List<CollisionReport> = emptyList(),
    val collisionReportStatus: CollisionReportStatus = CollisionReportStatus.IDLE,

    val selectedFeature: MapFeatureSelection? = null,
    val isFilterSheetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredWildlifeOccurrences: List<WildlifeOccurrence>
        get() = if (!showWildlifeOccurrences) emptyList() else {
            allWildlifeOccurrences.filter { occ ->
                val group = occ.taxonGroup
                selectedTaxonGroups.any { selected ->
                    group.contains(selected, ignoreCase = true) ||
                            (selected.equals("Fish", ignoreCase = true) && (group.contains("Aquatic", ignoreCase = true) || group.contains("Actinopterygii", ignoreCase = true))) ||
                            (selected.equals("Reptiles", ignoreCase = true) && (group.contains("Amphibian", ignoreCase = true) || group.contains("Reptile", ignoreCase = true)))
                }
            }
        }

    val filteredCollisionHotspots: List<CollisionHotspot>
        get() = if (!showCollisionHotspots) emptyList() else allCollisionHotspots

    val filteredBarriers: List<BarrierFeature>
        get() = if (!showBarriers) emptyList() else {
            allBarriers.filter { barrier ->
                selectedBarrierTypes.contains(barrier.type)
            }
        }

    val filteredPopulationZones: List<PopulationDensityZone>
        get() = if (!showPopulationDensity) emptyList() else allPopulationZones

    val filteredWildlifeCrossings: List<WildlifeCrossing>
        get() = if (!showWildlifeCrossings) emptyList() else allWildlifeCrossings

    val filteredCollisionReports: List<CollisionReport>
        get() = if (!showCollisionReports) emptyList() else collisionReports
}
