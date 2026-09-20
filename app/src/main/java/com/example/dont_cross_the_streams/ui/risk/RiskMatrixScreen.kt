package com.example.dont_cross_the_streams.ui.risk

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.MitigationSolution
import com.example.dont_cross_the_streams.domain.model.RiskLevel
import com.example.dont_cross_the_streams.domain.model.RiskMatrixScore
import com.example.dont_cross_the_streams.domain.model.SeveranceCause
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskMatrixScreen(
    modifier: Modifier = Modifier,
    viewModel: RiskMatrixViewModel = viewModel(),
    initialRegionId: String? = null,
    onNavigateToMap: ((GeoLocation) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialRegionId) {
        if (initialRegionId != null) {
            viewModel.selectRegion(initialRegionId)
        }
    }

    RiskMatrixScreenContent(
        uiState = uiState,
        onSelectRegion = viewModel::selectRegionByScore,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onFilterRiskLevelChange = viewModel::setFilterRiskLevel,
        onNavigateToMap = onNavigateToMap,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RiskMatrixScreenContent(
    uiState: RiskMatrixUiState,
    onSelectRegion: (RiskMatrixScore) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterRiskLevelChange: (RiskLevel?) -> Unit,
    onNavigateToMap: ((GeoLocation) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<RiskMatrixScore>()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Conflict Risk Scorecard & Matrix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Regional Connectivity Risk & Ecological Fragmentation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    RegionListPane(
                        uiState = uiState,
                        onRegionClicked = { score ->
                            onSelectRegion(score)
                            coroutineScope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, score)
                            }
                        },
                        onSearchQueryChange = onSearchQueryChange,
                        onFilterRiskLevelChange = onFilterRiskLevelChange
                    )
                },
                detailPane = {
                    val currentScore = uiState.selectedScore
                    if (currentScore != null) {
                        RegionDetailPane(
                            score = currentScore,
                            showBackButton = navigator.canNavigateBack(),
                            onBackClicked = {
                                coroutineScope.launch {
                                    navigator.navigateBack()
                                }
                            },
                            onNavigateToMap = onNavigateToMap
                        )
                    } else {
                        EmptyDetailPane()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegionListPane(
    uiState: RiskMatrixUiState,
    onRegionClicked: (RiskMatrixScore) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterRiskLevelChange: (RiskLevel?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search corridor or region...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Risk Level Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = uiState.filterRiskLevel == null,
                onClick = { onFilterRiskLevelChange(null) },
                label = { Text("All Regions (${uiState.regions.size})") }
            )
            FilterChip(
                selected = uiState.filterRiskLevel == RiskLevel.HIGH,
                onClick = { onFilterRiskLevelChange(RiskLevel.HIGH) },
                label = { Text("High Risk") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
            FilterChip(
                selected = uiState.filterRiskLevel == RiskLevel.MEDIUM,
                onClick = { onFilterRiskLevelChange(RiskLevel.MEDIUM) },
                label = { Text("Medium Risk") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
            FilterChip(
                selected = uiState.filterRiskLevel == RiskLevel.LOW,
                onClick = { onFilterRiskLevelChange(RiskLevel.LOW) },
                label = { Text("Low Risk") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Region Cards List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredRegions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching regional corridors found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredRegions, key = { it.id }) { item ->
                    val isSelected = uiState.selectedScore?.id == item.id
                    RegionCardItem(
                        score = item,
                        isSelected = isSelected,
                        onClick = { onRegionClicked(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegionCardItem(
    score: RiskMatrixScore,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = score.region,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                RiskBadge(level = score.riskLevel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${score.centerLocation.latitude.toString().take(6)}, ${score.centerLocation.longitude.toString().take(6)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Score: ${score.compositeConflictScore}/100",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = getRiskColor(score.riskLevel)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini Composite Gauge Progress Bar
            val animatedProgress by animateFloatAsState(
                targetValue = score.compositeConflictScore / 100f,
                animationSpec = tween(durationMillis = 600),
                label = "MiniProgress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                color = getRiskColor(score.riskLevel),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun RegionDetailPane(
    score: RiskMatrixScore,
    showBackButton: Boolean,
    onBackClicked: () -> Unit,
    onNavigateToMap: ((GeoLocation) -> Unit)?
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        item {
            if (showBackButton) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onBackClicked)
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back to list"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Back to Region List",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Region Title & Map Button Header
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = score.region,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lat: ${score.centerLocation.latitude}, Long: ${score.centerLocation.longitude}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onNavigateToMap != null) {
                    AssistChip(
                        onClick = { onNavigateToMap(score.centerLocation) },
                        label = { Text("View on Map") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Composite Conflict Score Card
        item {
            CompositeScoreCard(score = score)
        }

        // Sub-Factor Gauge Meters Card
        item {
            SubFactorGaugesCard(score = score)
        }

        // Problem & Severance Causes Analysis
        item {
            SeveranceAnalysisCard(score = score)
        }

        // Actionable Mitigation Solutions Card
        item {
            MitigationSolutionsCard(solutions = score.mitigationSolutions)
        }
    }
}

@Composable
private fun CompositeScoreCard(score: RiskMatrixScore) {
    val animatedScoreProgress by animateFloatAsState(
        targetValue = score.compositeConflictScore / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "CompositeGauge"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "COMPOSITE CONFLICT RISK SCORE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Gauge Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                // Background Track Ring
                CircularProgressIndicator(
                    progress = { 1f },
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.fillMaxSize()
                )

                // Active Score Gauge Ring
                CircularProgressIndicator(
                    progress = { animatedScoreProgress },
                    color = getRiskColor(score.riskLevel),
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.fillMaxSize()
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${score.compositeConflictScore}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = getRiskColor(score.riskLevel)
                    )
                    Text(
                        text = "out of 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            RiskBadgeLarge(level = score.riskLevel)
        }
    }
}

@Composable
private fun SubFactorGaugesCard(score: RiskMatrixScore) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Sub-Factor Risk Gauge Meters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quantified components feeding into the composite conflict score.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            GaugeMeterItem(
                title = "Animal Density Score",
                value = score.animalDensityScore.roundToInt(),
                displayValue = "${score.animalDensityScore.roundToInt()}/100",
                icon = Icons.Rounded.Pets,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaugeMeterItem(
                title = "Infrastructure Barrier Density",
                value = score.barrierDensityScore.roundToInt(),
                displayValue = "${score.barrierDensityScore.roundToInt()}/100",
                icon = Icons.Rounded.Shield,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(12.dp))

            val collisionScore = minOf(100, (score.collisionCount * 0.5).roundToInt())
            GaugeMeterItem(
                title = "Collision Hotspot Frequency",
                value = collisionScore,
                displayValue = "${score.collisionCount} incidents recorded",
                icon = Icons.Rounded.DirectionsCar,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            GaugeMeterItem(
                title = "Human Pressure Score",
                value = score.humanPressure.roundToInt(),
                displayValue = "${score.humanPressure.roundToInt()}/100",
                icon = Icons.Rounded.Speed,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun GaugeMeterItem(
    title: String,
    value: Int,
    displayValue: String,
    icon: ImageVector,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "GaugeItem_$title"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = displayValue,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun SeveranceAnalysisCard(score: RiskMatrixScore) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Problem & Severance Causes Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = score.severityAnalysis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (score.severanceCauses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                score.severanceCauses.forEach { cause ->
                    SeveranceCauseRow(cause = cause)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SeveranceCauseRow(cause: SeveranceCause) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(top = 6.dp)
                    .background(
                        color = when (cause.impactSeverity.lowercase()) {
                            "critical" -> MaterialTheme.colorScheme.error
                            "high" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cause.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (cause.impactSeverity.lowercase()) {
                            "critical" -> MaterialTheme.colorScheme.errorContainer
                            "high" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = cause.impactSeverity.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = cause.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MitigationSolutionsCard(solutions: List<MitigationSolution>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Actionable Closed-Loop Mitigation Solutions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (solutions.isEmpty()) {
                Text(
                    text = "No custom solutions logged for this area.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                solutions.forEach { solution ->
                    MitigationSolutionItem(solution = solution)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MitigationSolutionItem(solution: MitigationSolution) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = solution.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "-${solution.expectedRiskReductionPercent}% Risk Reduction",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = solution.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = solution.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Est. Cost: ${solution.estimatedCost}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Status: ${solution.implementationStatus}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyDetailPane() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select a Regional Corridor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a region from the list to inspect its conflict risk scorecard, sub-factor gauges, and closed-loop mitigations.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskBadge(level: RiskLevel) {
    val color = getRiskColor(level)
    val containerColor = when (level) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
        RiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Text(
            text = level.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RiskBadgeLarge(level: RiskLevel) {
    val color = getRiskColor(level)
    val containerColor = when (level) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
        RiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${level.name} RISK REGION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun getRiskColor(level: RiskLevel): Color {
    return when (level) {
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
        RiskLevel.LOW -> MaterialTheme.colorScheme.primary
    }
}

@Preview(showBackground = true)
@Composable
fun RiskMatrixScreenPreview() {
    DontcrossthestreamsTheme {
        RiskMatrixScreenContent(
            uiState = RiskMatrixUiState(
                regions = listOf(
                    RiskMatrixScore(
                        id = "rms_001",
                        region = "I-70 Vail Pass Mountain Corridor",
                        centerLocation = GeoLocation(39.638, -105.897),
                        animalDensityScore = 85.0,
                        barrierDensityScore = 92.0,
                        collisionCount = 184,
                        humanPressure = 78.0,
                        compositeConflictScore = 89,
                        riskLevel = RiskLevel.HIGH,
                        severityAnalysis = "Severe habitat fragmentation caused by 6-lane interstate bisecting elk migration routes.",
                        severanceCauses = listOf(
                            SeveranceCause("6-Lane Mountain Interstate", "High-speed multi-lane traffic creates a lethal physical barrier.", "Critical")
                        ),
                        mitigationSolutions = listOf(
                            MitigationSolution("Large Wildlife Overpass", "Wildlife Overpass", "Construct 150ft wide vegetated arch bridge over I-70.", "$15M", 90, "In Planning")
                        )
                    )
                ),
                filteredRegions = listOf(
                    RiskMatrixScore(
                        id = "rms_001",
                        region = "I-70 Vail Pass Mountain Corridor",
                        centerLocation = GeoLocation(39.638, -105.897),
                        animalDensityScore = 85.0,
                        barrierDensityScore = 92.0,
                        collisionCount = 184,
                        humanPressure = 78.0,
                        compositeConflictScore = 89,
                        riskLevel = RiskLevel.HIGH,
                        severityAnalysis = "Severe habitat fragmentation caused by 6-lane interstate bisecting elk migration routes.",
                        severanceCauses = listOf(
                            SeveranceCause("6-Lane Mountain Interstate", "High-speed multi-lane traffic creates a lethal physical barrier.", "Critical")
                        ),
                        mitigationSolutions = listOf(
                            MitigationSolution("Large Wildlife Overpass", "Wildlife Overpass", "Construct 150ft wide vegetated arch bridge over I-70.", "$15M", 90, "In Planning")
                        )
                    )
                ),
                selectedScore = RiskMatrixScore(
                    id = "rms_001",
                    region = "I-70 Vail Pass Mountain Corridor",
                    centerLocation = GeoLocation(39.638, -105.897),
                    animalDensityScore = 85.0,
                    barrierDensityScore = 92.0,
                    collisionCount = 184,
                    humanPressure = 78.0,
                    compositeConflictScore = 89,
                    riskLevel = RiskLevel.HIGH,
                    severityAnalysis = "Severe habitat fragmentation caused by 6-lane interstate bisecting elk migration routes.",
                    severanceCauses = listOf(
                        SeveranceCause("6-Lane Mountain Interstate", "High-speed multi-lane traffic creates a lethal physical barrier.", "Critical")
                    ),
                        mitigationSolutions = listOf(
                            MitigationSolution("Large Wildlife Overpass", "Wildlife Overpass", "Construct 150ft wide vegetated arch bridge over I-70.", "$15M", 90, "In Planning")
                        )
                )
            ),
            onSelectRegion = {},
            onSearchQueryChange = {},
            onFilterRiskLevelChange = {}
        )
    }
}
