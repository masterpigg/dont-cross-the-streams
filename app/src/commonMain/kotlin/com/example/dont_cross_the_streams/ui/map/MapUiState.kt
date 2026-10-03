package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

/**
 * Each layer is fetched live for the visible area. [minZoom] keeps requests to a size the public
 * APIs answer quickly (e.g. every road and culvert in Missouri at once would time out).
 */
enum class MapLayer(val minZoom: Float) {
    COLLISIONS(8f),
    WILDLIFE(9f),
    POPULATION(9f),
    INFRASTRUCTURE(10f),
    STRUCTURES(11f)
}

enum class LayerLoadState { IDLE, ZOOM_IN, LOADING, LOADED, UNAVAILABLE }

/** Zoom at which OpenStreetMap culverts under major roads are added to the infrastructure query. */
const val CULVERT_MIN_ZOOM = 12f

val DEFAULT_MAP_CENTER = GeoLocation(38.70, -90.45) // St. Louis City, St. Louis County and St. Charles
const val DEFAULT_MAP_ZOOM = 10f

val MONTH_LABELS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** Month (1-12) from an ISO-style date such as "2024-10-31" or "2024-10-31T06:00:00". */
fun monthOf(isoDate: String?): Int? =
    isoDate?.takeIf { it.length >= 7 && it[4] == '-' }?.substring(5, 7)?.toIntOrNull()?.takeIf { it in 1..12 }

data class MapUiState(
    val mapCenter: GeoLocation = DEFAULT_MAP_CENTER,
    val zoomLevel: Float = DEFAULT_MAP_ZOOM,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,

    val showWildlifeOccurrences: Boolean = true,
    val showCollisionHotspots: Boolean = true,
    val showBarriers: Boolean = true,
    val showPopulationDensity: Boolean = true,
    val showWildlifeCrossings: Boolean = true,
    val showCollisionReports: Boolean = true,

    val selectedTaxonGroups: Set<String> = setOf("Mammals", "Birds", "Reptiles", "Amphibians"),
    val selectedBarrierTypes: Set<BarrierType> = BarrierType.entries.toSet(),
    /** Months (1-12) to include; empty means all months. Applies to sightings and collision reports. */
    val selectedMonths: Set<Int> = emptySet(),
    val activePreset: ConflictRegionPreset? = null,

    val allWildlifeOccurrences: List<WildlifeOccurrence> = emptyList(),
    val collisionReports: List<CollisionReport> = emptyList(),
    /** Derived from [filteredCollisionReports] by the ViewModel; never hand-entered. */
    val allCollisionHotspots: List<CollisionHotspot> = emptyList(),
    val allBarriers: List<BarrierFeature> = emptyList(),
    val osmCrossings: List<WildlifeCrossing> = emptyList(),
    val nbiStructures: List<WildlifeCrossing> = emptyList(),
    val allPopulationZones: List<PopulationDensityZone> = emptyList(),
    val layerStatus: Map<MapLayer, LayerLoadState> = emptyMap(),

    val selectedFeature: MapFeatureSelection? = null,
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null
) {
    fun statusOf(layer: MapLayer): LayerLoadState = layerStatus[layer] ?: LayerLoadState.IDLE

    val allWildlifeCrossings: List<WildlifeCrossing>
        get() = osmCrossings + nbiStructures

    private fun monthMatches(date: String?): Boolean =
        selectedMonths.isEmpty() || monthOf(date)?.let { it in selectedMonths } == true

    val filteredWildlifeOccurrences: List<WildlifeOccurrence>
        get() = if (!showWildlifeOccurrences) emptyList() else allWildlifeOccurrences.filter {
            it.taxonGroup in selectedTaxonGroups && monthMatches(it.observedOn)
        }

    /** Reports passing the taxon and month filters, whether or not the report layer is shown. */
    val filteredCollisionReportsAll: List<CollisionReport>
        get() = collisionReports.filter { it.taxonGroup in selectedTaxonGroups && monthMatches(it.observedOn) }

    val filteredCollisionReports: List<CollisionReport>
        get() = if (!showCollisionReports) emptyList() else filteredCollisionReportsAll

    val filteredCollisionHotspots: List<CollisionHotspot>
        get() = if (!showCollisionHotspots) emptyList() else allCollisionHotspots

    val filteredBarriers: List<BarrierFeature>
        get() = if (!showBarriers) emptyList() else allBarriers.filter { it.type in selectedBarrierTypes }

    val filteredPopulationZones: List<PopulationDensityZone>
        get() = if (!showPopulationDensity) emptyList() else allPopulationZones

    val filteredWildlifeCrossings: List<WildlifeCrossing>
        get() = if (!showWildlifeCrossings) emptyList() else allWildlifeCrossings
}
