package com.example.dont_cross_the_streams.ui.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LayerToggleBar(
    showWildlife: Boolean,
    showHotspots: Boolean,
    showBarriers: Boolean,
    showPopulation: Boolean,
    onToggleWildlife: () -> Unit,
    onToggleHotspots: () -> Unit,
    onToggleBarriers: () -> Unit,
    onTogglePopulation: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    modifier: Modifier = Modifier,
    activePresetName: String? = null,
    activeFilterCount: Int = 0
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MAP OVERLAYS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activePresetName != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = activePresetName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenFilterSheet,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        if (activeFilterCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(text = "$activeFilterCount")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterList,
                                    contentDescription = "Filter Options"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = "Filter Options"
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LayerChip(
                    selected = showWildlife,
                    onClick = onToggleWildlife,
                    label = "Wildlife",
                    icon = Icons.Rounded.Pets,
                    activeColor = Color(0xFFFFB300)
                )

                LayerChip(
                    selected = showHotspots,
                    onClick = onToggleHotspots,
                    label = "Hotspots",
                    icon = Icons.Rounded.Warning,
                    activeColor = Color(0xFFFF3D00)
                )

                LayerChip(
                    selected = showBarriers,
                    onClick = onToggleBarriers,
                    label = "Barriers",
                    icon = Icons.Rounded.Route,
                    activeColor = Color(0xFF29B6F6)
                )

                LayerChip(
                    selected = showPopulation,
                    onClick = onTogglePopulation,
                    label = "Human Density",
                    icon = Icons.Rounded.Groups,
                    activeColor = Color(0xFFAB47BC)
                )
            }
        }
    }
}

@Composable
private fun LayerChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    activeColor: Color
) {
    val chipContainerColor by animateColorAsState(
        targetValue = if (selected) activeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainer,
        label = "chipBg"
    )
    val chipBorderColor by animateColorAsState(
        targetValue = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant,
        label = "chipBorder"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = chipContainerColor,
            selectedContainerColor = chipContainerColor,
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = chipBorderColor,
            selectedBorderColor = chipBorderColor,
            borderWidth = 1.5.dp,
            selectedBorderWidth = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
