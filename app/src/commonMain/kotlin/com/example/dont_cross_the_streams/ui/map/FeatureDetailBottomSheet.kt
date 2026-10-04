package com.example.dont_cross_the_streams.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CarCrash
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dont_cross_the_streams.data.datasource.DataSourceCatalog
import com.example.dont_cross_the_streams.data.remote.encodeUrlParam
import com.example.dont_cross_the_streams.domain.model.CrossingKind
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme

fun resolveDataSourceUrl(source: String): String {
    val trimmed = source.trim()
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed
    }
    val hubId = resolveTransparencyHubDataSourceId(trimmed)
    DataSourceCatalog.dataSources.firstOrNull { it.id == hubId }?.let { return it.documentationUrl }
    return "https://www.google.com/search?q=${encodeUrlParam(trimmed).replace("%20", "+")}"
}

fun resolveTransparencyHubDataSourceId(source: String): String? {
    val lower = source.lowercase()
    return when {
        lower.contains("inaturalist") -> "inaturalist"
        lower.contains("gbif") -> "gbif"
        lower.contains("overpass") || lower.contains("openstreetmap") || lower.contains("osm") -> "osm_overpass"
        lower.contains("national bridge") || lower.contains("nbi") || lower.contains("fhwa") -> "fhwa_nbi"
        lower.contains("census") || lower.contains("tigerweb") -> "census_tigerweb"
        else -> null
    }
}

fun splitSourceNames(source: String): List<String> {
    if (source.contains(" / ")) {
        return source.split(" / ").map { it.trim() }.filter { it.isNotEmpty() }
    }
    if (source.contains(", ")) {
        return source.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
    }
    return listOf(source.trim())
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeatureDetailBottomSheet(
    feature: MapFeatureSelection,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToTransparencyHub: ((String?) -> Unit)? = null,
    crossings: List<WildlifeCrossing> = emptyList(),
    hotspots: List<CollisionHotspot> = emptyList(),
    collisionReports: List<CollisionReport> = emptyList(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val uriHandler = LocalUriHandler.current

    fun openUrl(url: String) {
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
        }
    }

    val currentSource = when (feature) {
        is MapFeatureSelection.Wildlife -> feature.occurrence.source
        is MapFeatureSelection.Hotspot -> feature.hotspot.source
        is MapFeatureSelection.Barrier -> feature.barrier.source
        is MapFeatureSelection.Population -> feature.zone.source
        is MapFeatureSelection.Crossing -> feature.crossing.source
        is MapFeatureSelection.Collision -> feature.report.source
    }
    val primarySource = splitSourceNames(currentSource).firstOrNull() ?: currentSource
    // A single collision report links to its own observation page, not just the provider.
    // Every feature that maps to a single source record links straight to that record.
    val recordUrl = when (feature) {
        is MapFeatureSelection.Collision -> feature.report.observationUrl
        is MapFeatureSelection.Wildlife -> feature.occurrence.recordUrl
        is MapFeatureSelection.Crossing -> feature.crossing.recordUrl
        is MapFeatureSelection.Barrier -> feature.barrier.id.toOsmUrl()
        else -> null
    }
    val primaryUrl = recordUrl ?: resolveDataSourceUrl(primarySource)
    val hubDataSourceId = resolveTransparencyHubDataSourceId(currentSource)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (feature) {
                        is MapFeatureSelection.Wildlife -> Icons.Rounded.Pets
                        is MapFeatureSelection.Hotspot -> Icons.Rounded.Warning
                        is MapFeatureSelection.Barrier -> Icons.Rounded.Route
                        is MapFeatureSelection.Population -> Icons.Rounded.Info
                        is MapFeatureSelection.Crossing -> Icons.Rounded.Forest
                        is MapFeatureSelection.Collision -> Icons.Rounded.CarCrash
                    }
                    val iconTint = when (feature) {
                        is MapFeatureSelection.Wildlife -> MaterialTheme.colorScheme.primary
                        is MapFeatureSelection.Hotspot -> MaterialTheme.colorScheme.error
                        is MapFeatureSelection.Barrier -> MaterialTheme.colorScheme.tertiary
                        is MapFeatureSelection.Population -> MaterialTheme.colorScheme.secondary
                        is MapFeatureSelection.Crossing -> CrossingGreen
                        is MapFeatureSelection.Collision -> MaterialTheme.colorScheme.error
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = feature.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = feature.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Sheet"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            when (feature) {
                is MapFeatureSelection.Wildlife -> {
                    val occ = feature.occurrence
                    InfoChips(
                        listOfNotNull(
                            "Taxon: ${occ.taxonGroup}",
                            occ.observedOn?.let { "Observed: ${it.take(10)}" },
                            occ.observationCount.takeIf { it > 1 }?.let { "Individuals: $it" },
                            occ.conservationStatus?.let { "Status: $it" }
                        )
                    )
                    DetailCard(
                        scientificName = occ.species,
                        source = occ.source,
                        location = occ.location,
                        description = "Sighting record from ${occ.source}."
                    )
                }

                is MapFeatureSelection.Hotspot -> {
                    val hs = feature.hotspot
                    InfoChips(
                        listOfNotNull(
                            "Reports: ${hs.incidentCount}",
                            "Severity: ${hs.severity.name}",
                            hs.highwayOrRouteName?.let { "Near: $it" }
                        )
                    )
                    DetailCard(
                        scientificName = "Most reported: ${hs.primarySpeciesAffected}",
                        source = hs.source,
                        location = hs.location,
                        description = (hs.description ?: "") + " Hotspots are computed in the app from the loaded " +
                            "reports (no hand-placed points); changing the month filter or map area recomputes them."
                    )
                    CollisionVsCrossingCard(
                        lines = listOfNotNull(
                            nearestCrossingLine(hs.location, crossings),
                            nearbyReportsLine(hs.location, collisionReports)
                        )
                    )
                }

                is MapFeatureSelection.Barrier -> {
                    val bar = feature.barrier
                    InfoChips(
                        listOfNotNull(
                            "Type: ${bar.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            "Impact class: ${bar.impactLevel.name.lowercase()}",
                            bar.lengthKm?.let { "Segment: ${formatKm(it)}" }
                        )
                    )
                    DetailCard(
                        scientificName = bar.name,
                        source = bar.source,
                        location = bar.location,
                        description = (bar.description?.let { "$it. " } ?: "") +
                            "Impact class follows the road class: motorway severe, trunk high, primary moderate."
                    )
                }

                is MapFeatureSelection.Population -> {
                    val pop = feature.zone
                    InfoChips(
                        listOf(
                            "Population: ${formatCount(pop.population)}",
                            "Land: ${formatDecimal(pop.landAreaKm2)} km²",
                            "Density: ${formatCount(pop.densityScore.toInt())} / km²"
                        )
                    )
                    DetailCard(
                        scientificName = pop.regionName,
                        source = pop.source,
                        location = pop.centerLocation,
                        description = "2020 Census count for this tract. Density is people per km² of land."
                    )
                }

                is MapFeatureSelection.Crossing -> {
                    val xing = feature.crossing
                    InfoChips(
                        listOfNotNull(
                            if (xing.kind == CrossingKind.DEDICATED) "Dedicated wildlife crossing" else "Existing waterway structure",
                            "Type: ${xing.structureType.name.lowercase().replace('_', ' ')}",
                            xing.averageDailyTraffic?.let { "Traffic: ${formatCount(it)} vehicles/day" },
                            xing.yearBuilt?.let { "Built: $it" }
                        )
                    )
                    DetailCard(
                        scientificName = listOfNotNull(
                            xing.carries?.let { "Carries: $it" },
                            xing.crosses?.let { "Over: $it" }
                        ).joinToString(" · ").ifBlank { xing.name },
                        source = xing.source,
                        location = xing.location,
                        description = (xing.description?.let { "$it " } ?: "") + if (xing.kind == CrossingKind.WATERWAY_STRUCTURE) {
                            "Not built for wildlife, but stream corridors under roads are routes animals already use."
                        } else {
                            "Built for animals to cross the road."
                        }
                    )
                    CollisionVsCrossingCard(
                        lines = listOfNotNull(
                            nearbyHotspotsLine(xing.location, hotspots),
                            nearbyReportsLine(xing.location, collisionReports)
                        )
                    )
                }

                is MapFeatureSelection.Collision -> {
                    val report = feature.report
                    InfoChips(listOfNotNull("Group: ${report.taxonGroup}", report.observedOn?.let { "Observed: ${it.take(10)}" }))
                    DetailCard(
                        scientificName = report.species,
                        source = report.source,
                        location = report.location,
                        description = "Community-reported dead animal at this exact spot. Most of these reports are " +
                            "road mortality, but the dead-animal annotation alone doesn't confirm a vehicle strike."
                    )
                    CollisionVsCrossingCard(
                        lines = listOfNotNull(nearestCrossingLine(report.location, crossings))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { openUrl(primaryUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (recordUrl != null) {
                            "View Original Record"
                        } else {
                            "Open Data Source ($primarySource)"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (onNavigateToTransparencyHub != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onNavigateToTransparencyHub(hubDataSourceId)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore Data Source in Transparency Hub",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailCard(
    scientificName: String,
    source: String,
    location: GeoLocation,
    description: String
) {
    val uriHandler = LocalUriHandler.current

    fun openUrl(url: String) {
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
        }
    }

    val sourceList = splitSourceNames(source)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = scientificName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                val latFormatted = ((location.latitude * 10000).toInt() / 10000.0).toString()
                val lonFormatted = ((location.longitude * 10000).toInt() / 10000.0).toString()
                Text(
                    text = "Lat: $latFormatted, Lon: $lonFormatted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Data Source Link(s):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sourceList.forEach { singleSource ->
                        val url = resolveDataSourceUrl(singleSource)
                        AssistChip(
                            onClick = { openUrl(url) },
                            label = {
                                Text(
                                    text = singleSource,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = "Open $singleSource link",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

private val CrossingGreen = Color(0xFF2E7D32)

/** Puts collisions and crossings side by side for the selected feature. */
@Composable
private fun CollisionVsCrossingCard(lines: List<String>) {
    if (lines.isEmpty()) return
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Collisions vs. Crossings",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            lines.forEach { line ->
                Text(
                    text = "• $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private const val NEARBY_REPORT_RADIUS_KM = 10.0
private const val NEARBY_HOTSPOT_RADIUS_KM = 30.0

internal fun nearestCrossingLine(from: GeoLocation, crossings: List<WildlifeCrossing>): String? {
    val nearest = crossings.minByOrNull { it.location.distanceToKm(from) } ?: return null
    return "Nearest wildlife crossing: ${nearest.name} (${formatKm(nearest.location.distanceToKm(from))} away)"
}

internal fun nearbyReportsLine(from: GeoLocation, reports: List<CollisionReport>): String? {
    if (reports.isEmpty()) return null
    val count = reports.count { it.location.distanceToKm(from) <= NEARBY_REPORT_RADIUS_KM }
    val noun = if (count == 1) "report" else "reports"
    return "$count dead-animal $noun within ${NEARBY_REPORT_RADIUS_KM.toInt()} km in the loaded iNaturalist data"
}

internal fun nearbyHotspotsLine(from: GeoLocation, hotspots: List<CollisionHotspot>): String? {
    val nearby = hotspots.filter { it.location.distanceToKm(from) <= NEARBY_HOTSPOT_RADIUS_KM }
    if (nearby.isEmpty()) {
        return if (hotspots.isEmpty()) null else "No mapped collision hotspot within ${NEARBY_HOTSPOT_RADIUS_KM.toInt()} km"
    }
    val incidents = nearby.sumOf { it.incidentCount }
    return "${nearby.size} collision hotspot(s) within ${NEARBY_HOTSPOT_RADIUS_KM.toInt()} km, $incidents recorded incidents"
}

internal fun formatKm(km: Double): String {
    return if (km < 10.0) "${(km * 10).toInt() / 10.0} km" else "${km.toInt()} km"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoChips(labels: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        labels.forEach { label -> SuggestionChip(onClick = {}, label = { Text(label) }) }
    }
}

/** "osm_way_123" -> https://www.openstreetmap.org/way/123 */
internal fun String.toOsmUrl(): String? {
    val parts = split('_')
    if (parts.size != 3 || parts[0] != "osm" || parts[1] !in setOf("way", "node", "relation")) return null
    return "https://www.openstreetmap.org/${parts[1]}/${parts[2]}"
}

internal fun formatCount(n: Int): String {
    val digits = kotlin.math.abs(n).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (n < 0) "-$grouped" else grouped
}

internal fun formatDecimal(value: Double): String {
    val tenths = kotlin.math.round(value * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
