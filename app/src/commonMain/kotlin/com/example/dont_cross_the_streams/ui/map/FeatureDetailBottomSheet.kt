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
import androidx.compose.material.icons.rounded.OpenInNew
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
import com.example.dont_cross_the_streams.data.datasource.MockDatasetTransparencyDataSource
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

    // Providers without a Transparency Hub entry go straight to their homepage. Checked first so
    // that e.g. "North Carolina DOT" isn't swallowed by the hub's generic "dot" catch-all.
    resolveProviderHomepageUrl(trimmed)?.let { return it }

    // Hub-backed sources share one curated link per provider, so the map and the hub never disagree.
    val hubId = resolveTransparencyHubDataSourceId(trimmed)
    MockDatasetTransparencyDataSource.dataSources.firstOrNull { it.id == hubId }?.let { return it.documentationUrl }

    return "https://www.google.com/search?q=${encodeQueryParam(trimmed)}"
}

private fun resolveProviderHomepageUrl(source: String): String? {
    val lower = source.lowercase()
    return when {
        lower.contains("idot") -> "https://idot.illinois.gov/"
        lower.contains("colorado dot") -> "https://www.codot.gov/"
        lower.contains("wyoming dot") -> "https://www.dot.state.wy.us/"
        lower.contains("wyoming game") -> "https://wgfd.wyoming.gov/"
        lower.contains("north carolina dot") -> "https://www.ncdot.gov/"
        lower.contains("florida fish and wildlife") -> "https://myfwc.com/"
        lower.contains("national park service") -> "https://www.nps.gov/"
        lower.contains("us forest service") || lower == "usfs" || lower.startsWith("usfs ") -> "https://www.fs.usda.gov/"
        lower.contains("federal railroad") -> "https://railroads.dot.gov/"
        lower.contains("bureau of reclamation") -> "https://www.usbr.gov/"
        lower.contains("blm") -> "https://www.blm.gov/"
        lower.contains("msdis") || lower.contains("missouri spatial data") -> "https://msdis.missouri.edu/"
        lower.contains("missouri state highway patrol") -> "https://www.mshp.dps.missouri.gov/"
        lower.contains("illinois natural history survey") -> "https://inhs.illinois.edu/"
        lower.contains("elwha klallam") -> "https://www.elwha.org/"
        lower.contains("orca network") -> "https://www.orcanetwork.org/"
        lower.contains("audubon") -> "https://www.audubon.org/"
        lower.contains("east-west gateway") -> "https://www.ewgateway.org/"
        lower.contains("puget sound regional council") -> "https://www.psrc.org/"
        lower == "marc" -> "https://www.marc.org/"
        lower == "oto" -> "https://www.ozarkstransportation.org/"
        lower.contains("noaa") -> "https://www.fisheries.noaa.gov/"
        else -> null
    }
}

private fun encodeQueryParam(value: String): String = buildString {
    for (byte in value.encodeToByteArray()) {
        val c = byte.toInt().toChar()
        when {
            c.isLetterOrDigit() && byte >= 0 || c in "-_.~" -> append(c)
            c == ' ' -> append('+')
            else -> {
                val v = byte.toInt() and 0xFF
                append('%')
                append("0123456789ABCDEF"[v shr 4])
                append("0123456789ABCDEF"[v and 0x0F])
            }
        }
    }
}

fun resolveTransparencyHubDataSourceId(source: String): String? {
    val lower = source.lowercase()
    return when {
        lower.contains("wcpp") || (lower.contains("usdot") && lower.contains("pilot")) -> "usdot_wcpp"
        lower.contains("caltrans") -> "caltrans_crossings"
        lower.contains("cdot") -> "cdot_crossings"
        lower.contains("wsdot") && (lower.contains("crossing") || lower.contains("barrier") || lower.contains("fish") || lower.contains("pass") || lower.contains("snoqualmie")) -> "wsdot_fish_wildlife"
        lower.contains("gbif") -> "gbif"
        lower.contains("inaturalist") -> "inaturalist"
        lower.contains("movebank") -> "movebank"
        lower.contains("ebird") -> "ebird"
        lower.contains("overpass") || lower.contains("openstreetmap") || lower.contains("osm") -> "osm_overpass"
        lower.contains("nid") || (lower.contains("usace") && lower.contains("dam")) -> "usace_dams"
        lower.contains("usace") && (lower.contains("salmon") || lower.contains("nwd") || lower.contains("northwestern") || lower.contains("seattle")) -> "usace_nwd_salmon"
        lower.contains("usace") -> "usace_dams"
        lower.contains("mdc") -> "mdc_wildlife"
        lower.contains("modot") -> "modot_wvc"
        lower.contains("census") || lower.contains("tiger") -> "us_census"
        lower.contains("idnr") || lower.contains("illinois dnr") -> "idnr_wildlife"
        lower.contains("wdfw") -> "wdfw_salmon"
        lower.contains("wsdot") -> "wsdot_fish_wildlife"
        lower.contains("faa") || lower.contains("nwsd") -> "faa_nwsd"
        lower.contains("fars") || lower.contains("nhtsa") -> "nhtsa_fars"
        lower.contains("ipac") || lower.contains("usfws") -> "usfws_ipac"
        lower.contains("usgs") || lower.contains("gap") -> "usgs_gap"
        lower.contains("natureserve") -> "natureserve"
        lower.contains("nasa") || lower.contains("sedac") || lower.contains("human footprint") -> "nasa_human_footprint"
        lower.contains("dot") -> "state_dot_wvc"
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
    val primaryUrl = (feature as? MapFeatureSelection.Collision)?.report?.observationUrl
        ?: resolveDataSourceUrl(primarySource)
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Taxon: ${occ.taxonGroup}") }
                        )
                        if (!occ.conservationStatus.isNullOrBlank()) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Status: ${occ.conservationStatus}") }
                            )
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Observations: ${occ.observationCount}") }
                        )
                    }

                    DetailCard(
                        scientificName = occ.species,
                        source = occ.source,
                        location = occ.location,
                        description = "Recorded observation in conflict zone database."
                    )
                }

                is MapFeatureSelection.Hotspot -> {
                    val hs = feature.hotspot
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Severity: ${hs.severity.name}") }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Incidents: ${hs.incidentCount}") }
                        )
                    }

                    DetailCard(
                        scientificName = "Primary Species: ${hs.primarySpeciesAffected}",
                        source = hs.source,
                        location = hs.location,
                        description = hs.description ?: "High-frequency wildlife vehicle collision area requiring mitigation."
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Type: ${bar.type.name}") }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Impact: ${bar.impactLevel.name}") }
                        )
                        if (bar.lengthKm != null) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Length: ${bar.lengthKm} km") }
                            )
                        }
                    }

                    DetailCard(
                        scientificName = "Barrier ID: ${bar.id}",
                        source = bar.source,
                        location = bar.location,
                        description = bar.description ?: "Physical structure restricting terrestrial or aquatic organism movement."
                    )
                }

                is MapFeatureSelection.Population -> {
                    val pop = feature.zone
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Level: ${pop.urbanLevel.name}") }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Density: ${pop.densityScore.toInt()} / km²") }
                        )
                    }

                    DetailCard(
                        scientificName = "Region: ${pop.regionName}",
                        source = pop.source,
                        location = pop.centerLocation,
                        description = "Census population density zone indicating potential anthropogenic footprint pressure."
                    )
                }

                is MapFeatureSelection.Crossing -> {
                    val xing = feature.crossing
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Type: ${xing.structureType.name.replace('_', ' ')}") }
                        )
                        if (xing.structureCount > 1) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Structures: ${xing.structureCount}") }
                            )
                        }
                    }

                    DetailCard(
                        scientificName = "Target Species: ${xing.targetSpecies}",
                        source = xing.source,
                        location = xing.location,
                        description = xing.description ?: "Structure that lets wildlife cross a road or dam safely."
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Group: ${report.taxonGroup}") }
                        )
                        report.observedOn?.let { date ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Observed: $date") }
                            )
                        }
                    }

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
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (feature is MapFeatureSelection.Collision && feature.report.observationUrl != null) {
                            "View Original Report"
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
                                    imageVector = Icons.Rounded.OpenInNew,
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
