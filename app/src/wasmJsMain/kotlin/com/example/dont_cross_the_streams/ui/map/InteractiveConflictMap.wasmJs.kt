package com.example.dont_cross_the_streams.ui.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CompassCalibration
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
actual fun InteractiveConflictMap(
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    wildlifeOccurrences: List<WildlifeOccurrence>,
    collisionHotspots: List<CollisionHotspot>,
    barriers: List<BarrierFeature>,
    populationZones: List<PopulationDensityZone>,
    selectedFeature: MapFeatureSelection?,
    onPan: (dx: Float, dy: Float) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    onFeatureSelected: (MapFeatureSelection?) -> Unit,
    modifier: Modifier,
    onPanDirection: (dLat: Double, dLon: Double) -> Unit,
    onSelectPreset: (ConflictRegionPreset) -> Unit,
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation for hotspot pins
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(mapCenter, zoomLevel, panOffsetX, panOffsetY) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (pan.x != 0f || pan.y != 0f) {
                            onPan(pan.x, pan.y)
                        }
                        if (zoom > 1.05f) {
                            onZoomIn()
                        } else if (zoom < 0.95f) {
                            onZoomOut()
                        }
                    }
                }
                .pointerInput(
                    mapCenter, zoomLevel, panOffsetX, panOffsetY,
                    wildlifeOccurrences, collisionHotspots, barriers, populationZones
                ) {
                    detectTapGestures { tapOffset ->
                        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@detectTapGestures

                        var clickedSelection: MapFeatureSelection? = null
                        var minDistance = 32f // Hit test threshold in pixels

                        // Check collision hotspots
                        for (hotspot in collisionHotspots) {
                            val pos = geoToPixel(hotspot.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, canvasSize)
                            val dist = distanceBetween(tapOffset, pos)
                            if (dist < minDistance) {
                                minDistance = dist
                                clickedSelection = MapFeatureSelection.Hotspot(hotspot)
                            }
                        }

                        // Check wildlife occurrences
                        for (wildlife in wildlifeOccurrences) {
                            val pos = geoToPixel(wildlife.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, canvasSize)
                            val dist = distanceBetween(tapOffset, pos)
                            if (dist < minDistance) {
                                minDistance = dist
                                clickedSelection = MapFeatureSelection.Wildlife(wildlife)
                            }
                        }

                        // Check barriers
                        for (barrier in barriers) {
                            val pos = geoToPixel(barrier.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, canvasSize)
                            val dist = distanceBetween(tapOffset, pos)
                            if (dist < minDistance) {
                                minDistance = dist
                                clickedSelection = MapFeatureSelection.Barrier(barrier)
                            }
                        }

                        // Check population density zones
                        for (zone in populationZones) {
                            val pos = geoToPixel(zone.centerLocation, mapCenter, zoomLevel, panOffsetX, panOffsetY, canvasSize)
                            val dist = distanceBetween(tapOffset, pos)
                            if (dist < minDistance) {
                                minDistance = dist
                                clickedSelection = MapFeatureSelection.Population(zone)
                            }
                        }

                        onFeatureSelected(clickedSelection)
                    }
                }
        ) {
            canvasSize = size

            // 1. Render Map Basemap background grid & tiles styling
            drawRect(color = Color(0xFF131722)) // Dark GIS map background

            // Grid lines (Latitude / Longitude)
            val gridStep = 40f * (2.0f.pow((zoomLevel - 8f).coerceIn(-2f, 4f)))
            var xGrid = (size.width / 2f + panOffsetX) % gridStep
            while (xGrid < size.width) {
                drawLine(
                    color = Color(0xFF1E2538),
                    start = Offset(xGrid, 0f),
                    end = Offset(xGrid, size.height),
                    strokeWidth = 1f
                )
                xGrid += gridStep
            }

            var yGrid = (size.height / 2f + panOffsetY) % gridStep
            while (yGrid < size.height) {
                drawLine(
                    color = Color(0xFF1E2538),
                    start = Offset(0f, yGrid),
                    end = Offset(size.width, yGrid),
                    strokeWidth = 1f
                )
                yGrid += gridStep
            }

            // Tile grid outlines & labels (Simulating Esri / OSM tiles)
            val centerPixel = Offset(size.width / 2f + panOffsetX, size.height / 2f + panOffsetY)
            drawCircle(
                color = Color(0xFF263238),
                radius = 350f * (zoomLevel / 10f),
                center = centerPixel,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
            )

            // 2. Render Population Density Zones (Translucent heatmaps)
            for (zone in populationZones) {
                val center = geoToPixel(zone.centerLocation, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                val radius = (zone.densityScore.toFloat() / 20f).coerceIn(40f, 180f) * (zoomLevel / 10f)

                val heatColor = when {
                    zone.densityScore > 1000 -> Color(0x66FF3D00)
                    zone.densityScore > 500 -> Color(0x55FF9100)
                    zone.densityScore > 200 -> Color(0x44FFEA00)
                    else -> Color(0x3300E676)
                }

                drawCircle(
                    color = heatColor,
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = heatColor.copy(alpha = 0.8f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                val labelResult = textMeasurer.measure(
                    text = "${zone.regionName} (${zone.densityScore.toInt()}/km²)",
                    style = TextStyle(color = Color(0xDDFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = labelResult,
                    topLeft = Offset(center.x - labelResult.size.width / 2f, center.y - 6f)
                )
            }

            // 3. Render Barrier Features (Highways, Railways, Dams, Fences, Canals)
            for (barrier in barriers) {
                val path = Path()
                val barrierColor = getBarrierColor(barrier.type)

                if (barrier.geometryPath.isNotEmpty()) {
                    var isFirst = true
                    for (geoPoint in barrier.geometryPath) {
                        val pt = geoToPixel(geoPoint, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                        if (isFirst) {
                            path.moveTo(pt.x, pt.y)
                            isFirst = false
                        } else {
                            path.lineTo(pt.x, pt.y)
                        }
                    }
                } else {
                    // Draw a representative barrier line segment through the central location
                    val centerPt = geoToPixel(barrier.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                    path.moveTo(centerPt.x - 60f, centerPt.y - 20f)
                    path.lineTo(centerPt.x + 60f, centerPt.y + 20f)
                }

                val strokeStyle = if (barrier.type == BarrierType.FENCE) {
                    Stroke(width = 4f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
                } else {
                    Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                }

                drawPath(path = path, color = barrierColor, style = strokeStyle)

                // Barrier name badge
                val centerPt = geoToPixel(barrier.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                val textResult = textMeasurer.measure(
                    text = barrier.name,
                    style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                val badgeWidth = textResult.size.width + 12f
                val badgeHeight = textResult.size.height + 6f
                drawRoundRect(
                    color = Color(0xEE1E2836),
                    topLeft = Offset(centerPt.x - badgeWidth / 2f, centerPt.y - badgeHeight / 2f),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(4f)
                )
                drawRoundRect(
                    color = barrierColor,
                    topLeft = Offset(centerPt.x - badgeWidth / 2f, centerPt.y - badgeHeight / 2f),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(4f),
                    style = Stroke(width = 1f)
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(centerPt.x - textResult.size.width / 2f, centerPt.y - textResult.size.height / 2f)
                )
            }

            // 4. Render Wildlife Occurrences (Species pins)
            for (wildlife in wildlifeOccurrences) {
                val pos = geoToPixel(wildlife.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                val taxonColor = getTaxonColor(wildlife.taxonGroup)

                drawCircle(
                    color = taxonColor.copy(alpha = 0.25f),
                    radius = 18f,
                    center = pos
                )
                drawCircle(
                    color = taxonColor,
                    radius = 10f,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = pos,
                    style = Stroke(width = 2f)
                )

                val countText = if (wildlife.observationCount > 1) "${wildlife.observationCount}" else ""
                if (countText.isNotEmpty()) {
                    val countLayout = textMeasurer.measure(
                        text = countText,
                        style = TextStyle(color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    )
                    drawText(
                        textLayoutResult = countLayout,
                        topLeft = Offset(pos.x - countLayout.size.width / 2f, pos.y - countLayout.size.height / 2f)
                    )
                }
            }

            // 5. Render Collision Hotspots (Hazard pins with pulsing rings)
            for (hotspot in collisionHotspots) {
                val pos = geoToPixel(hotspot.location, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                val severityColor = getSeverityColor(hotspot.severity)

                // Outer pulsing ring
                drawCircle(
                    color = severityColor.copy(alpha = pulseAlpha),
                    radius = 16f * pulseScale,
                    center = pos
                )

                // Hotspot pin shape
                drawCircle(
                    color = severityColor,
                    radius = 13f,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = 13f,
                    center = pos,
                    style = Stroke(width = 2f)
                )

                val incidentText = "${hotspot.incidentCount}"
                val incidentLayout = textMeasurer.measure(
                    text = incidentText,
                    style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = incidentLayout,
                    topLeft = Offset(pos.x - incidentLayout.size.width / 2f, pos.y - incidentLayout.size.height / 2f)
                )
            }

            // 6. Selected feature highlight ring
            selectedFeature?.let { selection ->
                val selectedLoc = when (selection) {
                    is MapFeatureSelection.Wildlife -> selection.occurrence.location
                    is MapFeatureSelection.Hotspot -> selection.hotspot.location
                    is MapFeatureSelection.Barrier -> selection.barrier.location
                    is MapFeatureSelection.Population -> selection.zone.centerLocation
                }
                val selPos = geoToPixel(selectedLoc, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 28f,
                    center = selPos,
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                )
            }

            // Scale & Map Attribution
            val attrText = textMeasurer.measure(
                text = "© Esri World Street Map | OpenStreetMap | NASA Footprint | Web/Wasm GIS Engine",
                style = TextStyle(color = Color(0xAAFFFFFF), fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = attrText,
                topLeft = Offset(16f, size.height - 24f)
            )
        }

        // Overlay: Quick Region Presets selector row
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 36.dp, start = 12.dp, end = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
                ConflictRegionPreset.entries.forEach { preset ->
                    AssistChip(
                        onClick = { onSelectPreset(preset) },
                        label = { Text(preset.title, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // Overlay: Map Control Buttons (Pan, Zoom, Compass, Reset)
        Card(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onPanDirection(0.02, 0.0) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Pan North"
                    )
                }
                Row {
                    IconButton(
                        onClick = { onPanDirection(0.0, -0.02) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Pan West"
                        )
                    }
                    IconButton(
                        onClick = onResetView,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Recenter Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onPanDirection(0.0, 0.02) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Pan East"
                        )
                    }
                }
                IconButton(
                    onClick = { onPanDirection(-0.02, 0.0) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Pan South"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.size(width = 36.dp, height = 1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Zoom In"
                    )
                }
                Text(
                    text = "${zoomLevel.toInt()}x",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Zoom Out"
                    )
                }
            }
        }
    }
}

private fun geoToPixel(
    geo: GeoLocation,
    centerGeo: GeoLocation,
    zoom: Float,
    panX: Float,
    panY: Float,
    canvasSize: Size
): Offset {
    val scale = 2.0.pow(zoom.toDouble()).toFloat() * 1200f
    val cosLat = cos(centerGeo.latitude * PI / 180.0).toFloat().coerceAtLeast(0.1f)

    val dLon = (geo.longitude - centerGeo.longitude).toFloat()
    val dLat = (geo.latitude - centerGeo.latitude).toFloat()

    val x = (canvasSize.width / 2f) + panX + (dLon * scale * cosLat)
    val y = (canvasSize.height / 2f) + panY - (dLat * scale)
    return Offset(x, y)
}

private fun distanceBetween(p1: Offset, p2: Offset): Float {
    val dx = p1.x - p2.x
    val dy = p1.y - p2.y
    return sqrt(dx * dx + dy * dy)
}

private fun getTaxonColor(taxonGroup: String): Color {
    return when (taxonGroup) {
        "Mammals" -> Color(0xFF4CAF50)
        "Birds" -> Color(0xFF2196F3)
        "Reptiles" -> Color(0xFFFFC107)
        "Amphibians" -> Color(0xFF009688)
        "Fish" -> Color(0xFF00BCD4)
        else -> Color(0xFF8BC34A)
    }
}

private fun getSeverityColor(severity: CollisionSeverity): Color {
    return when (severity) {
        CollisionSeverity.CRITICAL -> Color(0xFFE53935)
        CollisionSeverity.HIGH -> Color(0xFFFB8C00)
        CollisionSeverity.MODERATE -> Color(0xFFFDD835)
        CollisionSeverity.LOW -> Color(0xFF8E24AA)
    }
}

private fun getBarrierColor(type: BarrierType): Color {
    return when (type) {
        BarrierType.HIGHWAY -> Color(0xFFFF5252)
        BarrierType.RAILWAY -> Color(0xFFFF9800)
        BarrierType.DAM -> Color(0xFF00E5FF)
        BarrierType.FENCE -> Color(0xFFE040FB)
        BarrierType.CANAL -> Color(0xFF448AFF)
        BarrierType.URBAN_WALL -> Color(0xFF78909C)
    }
}
