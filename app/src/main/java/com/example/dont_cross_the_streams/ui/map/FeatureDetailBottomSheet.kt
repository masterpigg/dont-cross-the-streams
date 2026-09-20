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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeatureDetailBottomSheet(
    feature: MapFeatureSelection,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
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

            Spacer(modifier = Modifier.height(20.dp))

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

@Composable
private fun DetailCard(
    scientificName: String,
    source: String,
    location: GeoLocation,
    description: String
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                text = "Data Source: $source",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
