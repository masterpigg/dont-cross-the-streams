package com.example.dont_cross_the_streams.ui.risk

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.StudyArea
import com.example.dont_cross_the_streams.domain.model.StudyAreaStats

/** One measured row of the comparison table, with where the number comes from. */
private data class Metric(val label: String, val source: String, val value: (StudyAreaStats) -> String)

private val METRICS = listOf(
    Metric("Dead-animal reports (all years)", "iNaturalist, Alive or Dead = Dead") { s -> fmtInt(s.deadAnimalReports) },
    Metric("Research-grade vertebrate sightings", "iNaturalist") { s -> fmtInt(s.wildlifeObservations) },
    Metric("Vertebrate occurrence records", "GBIF (includes records GBIF republishes from iNaturalist)") { s -> fmtInt(s.gbifOccurrences) },
    Metric("Bridges & large culverts over water", "FHWA National Bridge Inventory") { s -> fmtInt(s.waterwayStructures) },
    Metric("Mapped dedicated wildlife crossings", "OpenStreetMap man_made=wildlife_crossing") { s -> fmtInt(s.dedicatedCrossings) },
    Metric("Major road length (motorway/trunk/primary)", "OpenStreetMap, km") { s -> s.majorRoadKm?.let { fmtDecimal(it) } ?: "—" },
    Metric("Population (2020)", "US Census, exact county") { s -> fmtInt(s.population?.population) },
    Metric("Land area", "US Census, km²") { s -> s.population?.landAreaKm2?.let { fmtDecimal(it) } ?: "—" },
    Metric("People per km² of land", "US Census") { s -> s.population?.densityPerKm2?.let { fmtInt(it.toInt()) } ?: "—" },
    Metric("Dead-animal reports per 100 km of major road", "Calculated from the rows above") { s -> s.deadReportsPer100RoadKm?.let { fmtDecimal(it) } ?: "—" },
    Metric("Waterway structures per 100 km of major road", "Calculated from the rows above") { s -> s.waterwayStructuresPer100RoadKm?.let { fmtDecimal(it) } ?: "—" }
)

@Composable
fun RiskMatrixScreen(
    modifier: Modifier = Modifier,
    viewModel: RiskMatrixViewModel = remember { RiskMatrixViewModel() },
    initialRegionId: String? = null,
    @Suppress("UNUSED_PARAMETER") onNavigateToMap: ((GeoLocation) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadIfNeeded() }
    LaunchedEffect(initialRegionId) {
        if (initialRegionId != null) viewModel.selectRegion(initialRegionId)
    }
    RiskMatrixScreenContent(uiState = uiState, onRefresh = viewModel::refresh, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskMatrixScreenContent(
    uiState: RiskMatrixUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Study Areas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Live counts for St. Louis City, St. Louis County and St. Charles County",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 4.dp), strokeWidth = 2.dp)
                    }
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reload counts")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ComparisonTable(uiState) }
            item { MethodNotes(uiState.areas) }
        }
    }
}

@Composable
private fun ComparisonTable(uiState: RiskMatrixUiState) {
    val labelWidth = 230.dp
    val columnWidth = 130.dp
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(16.dp)) {
            Row {
                Spacer(modifier = Modifier.width(labelWidth))
                uiState.areas.forEach { area ->
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(columnWidth)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            METRICS.forEach { metric ->
                Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.width(labelWidth)) {
                        Text(metric.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(metric.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    uiState.areas.forEach { area ->
                        val stats = uiState.stats[area.id]
                        Text(
                            text = when {
                                stats == null && area.id in uiState.loadingAreaIds -> "…"
                                stats == null -> "—"
                                else -> metric.value(stats)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(columnWidth)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun MethodNotes(areas: List<StudyArea>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("How these numbers are measured", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "Every value is requested live from the source named under it each time this tab loads; " +
                    "nothing is stored or estimated. \"—\" means that source could not be reached.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Population and land area use each county's exact Census FIPS code. Point counts (reports, " +
                    "sightings, structures, road length) use a rectangle around each county, so they slightly " +
                    "overlap neighbouring counties:",
                style = MaterialTheme.typography.bodySmall
            )
            areas.forEach { area ->
                val b = area.bounds
                Text(
                    "• ${area.name}: ${b.minLat}°N–${b.maxLat}°N, ${-b.maxLon}°W–${-b.minLon}°W. ${area.description}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "Community reports (iNaturalist) reflect where people look and report as well as where animals are, " +
                    "so compare rates between areas with care.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun fmtInt(value: Int?): String {
    if (value == null) return "—"
    val digits = kotlin.math.abs(value).toString().reversed().chunked(3).joinToString(",").reversed()
    return if (value < 0) "-$digits" else digits
}

private fun fmtDecimal(value: Double): String {
    val tenths = kotlin.math.round(value * 10).toLong()
    return "${fmtInt((tenths / 10).toInt())}.${kotlin.math.abs(tenths % 10)}"
}
