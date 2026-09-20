package com.example.dont_cross_the_streams.ui.map

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme
import java.net.URLEncoder
import java.util.Locale

/**
 * Maps known data source names or keywords to official web portal URLs.
 */
fun resolveDataSourceUrl(source: String): String {
    val trimmed = source.trim()
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed
    }

    val lower = trimmed.lowercase(Locale.US)
    return when {
        lower.contains("gbif") -> "https://www.gbif.org/"
        lower.contains("inaturalist") -> "https://www.inaturalist.org/"
        lower.contains("movebank") -> "https://www.movebank.org/"
        lower.contains("ebird") -> "https://ebird.org/"
        lower.contains("overpass") || lower.contains("openstreetmap") || lower.contains("osm") -> "https://overpass-api.de/"
        lower.contains("nid") || (lower.contains("usace") && lower.contains("dam")) -> "https://nid.sec.usace.army.mil/"
        lower.contains("usace") -> "https://www.nwd.usace.army.mil/"
        lower.contains("mdc") || lower.contains("missouri department of conservation") -> "https://mdc.mo.gov/"
        lower.contains("modot") -> "https://www.modot.org/"
        lower.contains("census") || lower.contains("tiger") -> "https://www.census.gov/"
        lower.contains("idnr") || lower.contains("illinois dnr") || lower.contains("illinois department of natural resources") -> "https://dnr.illinois.gov/"
        lower.contains("wdfw") || lower.contains("washington department of fish") -> "https://wdfw.wa.gov/"
        lower.contains("faa") || lower.contains("nwsd") -> "https://wildlife.faa.gov/"
        lower.contains("fars") || lower.contains("nhtsa") -> "https://crashviewer.nhtsa.dot.gov/CrashAPI"
        lower.contains("ipac") || lower.contains("usfws") -> "https://ipac.ecosphere.fws.gov/"
        lower.contains("usgs") || lower.contains("gap") -> "https://gapanalysis.usgs.gov/"
        lower.contains("natureserve") -> "https://explorer.natureserve.org/"
        lower.contains("nasa") || lower.contains("sedac") || lower.contains("human footprint") -> "https://sedac.ciesin.columbia.edu/"
        lower.contains("caltrans") -> "https://dot.ca.gov/"
        lower.contains("idot") -> "https://idot.illinois.gov/"
        lower.contains("wsdot") -> "https://wsdot.wa.gov/"
        lower.contains("noaa") -> "https://www.fisheries.noaa.gov/"
        else -> try {
            "https://www.google.com/search?q=" + URLEncoder.encode(trimmed, "UTF-8")
        } catch (_: Exception) {
            "https://www.google.com/search?q=$trimmed"
        }
    }
}

/**
 * Resolves data source string to a corresponding Transparency Hub DataSourceInfo ID.
 */
fun resolveTransparencyHubDataSourceId(source: String): String? {
    val lower = source.lowercase(Locale.US)
    return when {
        lower.contains("gbif") -> "gbif"
        lower.contains("inaturalist") -> "inaturalist"
        lower.contains("movebank") -> "movebank"
        lower.contains("ebird") -> "ebird"
        lower.contains("overpass") || lower.contains("openstreetmap") || lower.contains("osm") -> "osm_overpass"
        lower.contains("nid") || (lower.contains("usace") && lower.contains("dam")) -> "usace_dams"
        lower.contains("salmon") && lower.contains("usace") -> "usace_nwd_salmon"
        lower.contains("usace") -> "usace_dams"
        lower.contains("mdc") -> "mdc_wildlife"
        lower.contains("modot") -> "modot_wvc"
        lower.contains("census") || lower.contains("tiger") -> "us_census"
        lower.contains("idnr") || lower.contains("illinois dnr") -> "idnr_wildlife"
        lower.contains("wdfw") -> "wdfw_salmon"
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

/**
 * Splits compound data source strings into individual provider names.
 */
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
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    val currentSource = when (feature) {
        is MapFeatureSelection.Wildlife -> feature.occurrence.source
        is MapFeatureSelection.Hotspot -> feature.hotspot.source
        is MapFeatureSelection.Barrier -> feature.barrier.source
        is MapFeatureSelection.Population -> feature.zone.source
    }
    val primarySource = splitSourceNames(currentSource).firstOrNull() ?: currentSource
    val primaryUrl = resolveDataSourceUrl(primarySource)
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
            // Header
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
                    }
                    val iconTint = when (feature) {
                        is MapFeatureSelection.Wildlife -> MaterialTheme.colorScheme.primary
                        is MapFeatureSelection.Hotspot -> MaterialTheme.colorScheme.error
                        is MapFeatureSelection.Barrier -> MaterialTheme.colorScheme.tertiary
                        is MapFeatureSelection.Population -> MaterialTheme.colorScheme.secondary
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

            // Specific Feature Details
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
                        if (!occ.conservationStatus.isNull_or_blank()) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
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
                        text = "Open Live API Web Portal ($primarySource)",
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

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailCard(
    scientificName: String,
    source: String,
    location: GeoLocation,
    description: String
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: Exception) {
            }
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
                Text(
                    text = "Lat: ${String.format(Locale.US, "%.4f", location.latitude)}, Lon: ${String.format(
                        Locale.US, "%.4f", location.longitude)}",
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

            // Clickable Data Source Section
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FeatureDetailBottomSheetPreview() {
    DontcrossthestreamsTheme {
        Surface {
            FeatureDetailBottomSheet(
                feature = MapFeatureSelection.Wildlife(
                    occurrence = WildlifeOccurrence(
                        id = "occ_preview",
                        species = "Puma concolor",
                        commonName = "Cougar / Mountain Lion",
                        location = GeoLocation(34.134, -118.321),
                        taxonGroup = "Mammals",
                        observationCount = 1,
                        timestamp = System.currentTimeMillis(),
                        source = "Movebank GPS Tracking",
                        conservationStatus = "Specially Protected"
                    )
                ),
                onDismiss = {}
            )
        }
    }
}
