package com.example.dont_cross_the_streams.ui.map

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.MotionEvent
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.LocationOn
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.UrbanLevel
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.ui.theme.DontcrossthestreamsTheme
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.io.File
import kotlin.math.abs

private class ProgrammaticMapState {
    var lastTargetLat: Double = 0.0
    var lastTargetLon: Double = 0.0
    var lastTargetZoom: Double = 0.0
    var onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit = { _, _ -> }
}

val EsriWorldStreetMapTileSource = object : XYTileSource(
    "EsriWorldStreetMap",
    0,
    19,
    256,
    ".jpg",
    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$baseUrl$zoom/$y/$x"
    }
}

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
    // Pulse animation for critical collision hotspots
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlphaFraction by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (LocalInspectionMode.current) {
            // Compose Preview Fallback
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (pan != Offset.Zero) onPan(pan.x, pan.y)
                            if (zoom > 1.05f) onZoomIn() else if (zoom < 0.95f) onZoomOut()
                        }
                    }
                    .pointerInput(wildlifeOccurrences, collisionHotspots, barriers, populationZones) {
                        detectTapGestures { tapOffset ->
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()
                            val hitFeature = findHitFeature(
                                tapOffset = tapOffset,
                                mapCenter = mapCenter,
                                zoomLevel = zoomLevel,
                                panX = panOffsetX,
                                panY = panOffsetY,
                                width = width,
                                height = height,
                                wildlifeOccurrences = wildlifeOccurrences,
                                collisionHotspots = collisionHotspots,
                                barriers = barriers,
                                populationZones = populationZones
                            )
                            onFeatureSelected(hitFeature)
                        }
                    }
            ) {
                drawPreviewMapGrid(
                    mapCenter = mapCenter,
                    zoomLevel = zoomLevel,
                    panX = panOffsetX,
                    panY = panOffsetY,
                    width = size.width,
                    height = size.height
                )

                populationZones.forEach { zone ->
                    drawPopulationZone(
                        zone = zone,
                        mapCenter = mapCenter,
                        zoomLevel = zoomLevel,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        width = size.width,
                        height = size.height,
                        isSelected = selectedFeature is MapFeatureSelection.Population && selectedFeature.zone.id == zone.id
                    )
                }

                barriers.forEach { barrier ->
                    drawBarrierFeature(
                        barrier = barrier,
                        mapCenter = mapCenter,
                        zoomLevel = zoomLevel,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        width = size.width,
                        height = size.height,
                        isSelected = selectedFeature is MapFeatureSelection.Barrier && selectedFeature.barrier.id == barrier.id
                    )
                }

                collisionHotspots.forEach { hotspot ->
                    drawCollisionHotspot(
                        hotspot = hotspot,
                        mapCenter = mapCenter,
                        zoomLevel = zoomLevel,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        width = size.width,
                        height = size.height,
                        pulseRadiusFraction = pulseRadiusFraction,
                        pulseAlphaFraction = pulseAlphaFraction,
                        isSelected = selectedFeature is MapFeatureSelection.Hotspot && selectedFeature.hotspot.id == hotspot.id
                    )
                }

                wildlifeOccurrences.forEach { occ ->
                    drawWildlifeOccurrence(
                        occ = occ,
                        mapCenter = mapCenter,
                        zoomLevel = zoomLevel,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        width = size.width,
                        height = size.height,
                        isSelected = selectedFeature is MapFeatureSelection.Wildlife && selectedFeature.occurrence.id == occ.id
                    )
                }
            }
        } else {
            // Live OpenStreetMap Tile View via OSMdroid
            val conflictOverlay = remember {
                ConflictMapOverlay(
                    wildlifeOccurrences = wildlifeOccurrences,
                    collisionHotspots = collisionHotspots,
                    barriers = barriers,
                    populationZones = populationZones,
                    selectedFeature = selectedFeature,
                    pulseRadiusFraction = pulseRadiusFraction,
                    pulseAlphaFraction = pulseAlphaFraction,
                    onFeatureSelected = onFeatureSelected
                )
            }

            val mapState = remember {
                ProgrammaticMapState().apply {
                    lastTargetLat = mapCenter.latitude
                    lastTargetLon = mapCenter.longitude
                    lastTargetZoom = zoomLevel.toDouble()
                }
            }
            mapState.onMapCenterAndZoomChanged = onMapCenterAndZoomChanged

            AndroidView(
                factory = { ctx ->
                    val osmdroidDir = File(ctx.cacheDir, "osmdroid")
                    if (!osmdroidDir.exists()) {
                        osmdroidDir.mkdirs()
                    }
                    val tileCacheDir = File(osmdroidDir, "tiles")
                    if (!tileCacheDir.exists()) {
                        tileCacheDir.mkdirs()
                    }

                    val config = Configuration.getInstance()
                    config.userAgentValue = "DontCrossTheStreams-WildlifeMappingApp/1.0 (https://github.com/dontcrossthestreams)"
                    config.osmdroidBasePath = osmdroidDir
                    config.osmdroidTileCache = tileCacheDir

                    MapView(ctx).apply {
                        setTileSource(EsriWorldStreetMapTileSource)
                        isTilesScaledToDpi = true
                        setMultiTouchControls(true)
                        isClickable = true
                        isFocusable = true
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                        controller.setZoom(zoomLevel.toDouble())
                        controller.setCenter(GeoPoint(mapCenter.latitude, mapCenter.longitude))

                        overlays.add(conflictOverlay)

                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                val center = this@apply.mapCenter ?: return false
                                val newCenter = GeoLocation(
                                    latitude = center.latitude,
                                    longitude = center.longitude
                                )
                                val newZoom = this@apply.zoomLevelDouble.toFloat()
                                mapState.lastTargetLat = newCenter.latitude
                                mapState.lastTargetLon = newCenter.longitude
                                mapState.lastTargetZoom = newZoom.toDouble()
                                mapState.onMapCenterAndZoomChanged(newCenter, newZoom)
                                return false
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                val center = this@apply.mapCenter ?: return false
                                val newCenter = GeoLocation(
                                    latitude = center.latitude,
                                    longitude = center.longitude
                                )
                                val newZoom = this@apply.zoomLevelDouble.toFloat()
                                mapState.lastTargetLat = newCenter.latitude
                                mapState.lastTargetLon = newCenter.longitude
                                mapState.lastTargetZoom = newZoom.toDouble()
                                mapState.onMapCenterAndZoomChanged(newCenter, newZoom)
                                return false
                            }
                        })
                    }
                },
                update = { mapView ->
                    conflictOverlay.wildlifeOccurrences = wildlifeOccurrences
                    conflictOverlay.collisionHotspots = collisionHotspots
                    conflictOverlay.barriers = barriers
                    conflictOverlay.populationZones = populationZones
                    conflictOverlay.selectedFeature = selectedFeature
                    conflictOverlay.pulseRadiusFraction = pulseRadiusFraction
                    conflictOverlay.pulseAlphaFraction = pulseAlphaFraction
                    conflictOverlay.onFeatureSelected = onFeatureSelected

                    val targetLat = mapCenter.latitude
                    val targetLon = mapCenter.longitude
                    val targetZoom = zoomLevel.toDouble()

                    val latDiff = abs(mapState.lastTargetLat - targetLat)
                    val lonDiff = abs(mapState.lastTargetLon - targetLon)
                    val zoomDiff = abs(mapState.lastTargetZoom - targetZoom)

                    if (latDiff > 0.0001 || lonDiff > 0.0001) {
                        mapState.lastTargetLat = targetLat
                        mapState.lastTargetLon = targetLon
                        mapView.controller.animateTo(GeoPoint(targetLat, targetLon))
                    }
                    if (zoomDiff > 0.05) {
                        mapState.lastTargetZoom = targetZoom
                        mapView.controller.setZoom(targetZoom)
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Quick Jump Region Chips
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 104.dp, end = 64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jump to:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, end = 2.dp)
                )

                ConflictRegionPreset.entries.forEach { preset ->
                    val isHighlighted = preset.name.startsWith("MISSOURI") || preset.name.startsWith("OZARK") || preset.name.startsWith("ST_LOUIS") || preset.name.startsWith("ILLINOIS") || preset.name.startsWith("WASHINGTON")
                    AssistChip(
                        onClick = { onSelectPreset(preset) },
                        label = {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = if (isHighlighted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isHighlighted)
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // Top Right Compass Indicator
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 104.dp, end = 12.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f),
            shadowElevation = 6.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CompassCalibration,
                    contentDescription = "North Indicator",
                    tint = Color(0xFFE53935)
                )
            }
        }

        // Floating Navigation Helpers
        Card(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                IconButton(
                    onClick = { onPanDirection(1.0, 0.0) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Pan North",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onPanDirection(0.0, -1.0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Pan West",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { onPanDirection(0.0, 1.0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Pan East",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = { onPanDirection(-1.0, 0.0) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Pan South",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(onClick = onZoomIn) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Zoom In",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onZoomOut) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Zoom Out",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(onClick = onResetView) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "Reset View",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Bottom Map Scale Indicator
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.70f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val approxKm = (500.0 / Math.pow(2.0, zoomLevel.toDouble())).toInt().coerceAtLeast(1)
                Text(
                    text = "$approxKm km",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .size(width = 30.dp, height = 3.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = Color.White)
                    }
                }
            }
        }
    }
}

private class ConflictMapOverlay(
    var wildlifeOccurrences: List<WildlifeOccurrence>,
    var collisionHotspots: List<CollisionHotspot>,
    var barriers: List<BarrierFeature>,
    var populationZones: List<PopulationDensityZone>,
    var selectedFeature: MapFeatureSelection?,
    var pulseRadiusFraction: Float,
    var pulseAlphaFraction: Float,
    var onFeatureSelected: (MapFeatureSelection?) -> Unit
) : Overlay() {

    private val reusedPoint = Point()
    private val textPaint = Paint().apply {
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas?, mapView: MapView?, shadow: Boolean) {
        if (shadow || canvas == null || mapView == null) return
        val proj = mapView.projection

        populationZones.forEach { zone ->
            val bbox = zone.boundingBox
            val topLeftPoint = proj.toPixels(GeoPoint(bbox.maxLat, bbox.minLon), null)
            val bottomRightPoint = proj.toPixels(GeoPoint(bbox.minLat, bbox.maxLon), null)

            val left = topLeftPoint.x.toFloat()
            val top = topLeftPoint.y.toFloat()
            val right = bottomRightPoint.x.toFloat().coerceAtLeast(left + 40f)
            val bottom = bottomRightPoint.y.toFloat().coerceAtLeast(top + 40f)

            val (fillColorInt, strokeColorInt) = when (zone.urbanLevel) {
                UrbanLevel.METROPOLITAN, UrbanLevel.URBAN -> 0x55E53935.toInt() to 0xFFE53935.toInt()
                UrbanLevel.SUBURBAN -> 0x44FB8C00.toInt() to 0xFFFB8C00.toInt()
                UrbanLevel.RURAL -> 0x33FDD835.toInt() to 0xFFFDD835.toInt()
                UrbanLevel.WILDERNESS -> 0x221E88E5.toInt() to 0xFF1E88E5.toInt()
            }

            val isSelected = selectedFeature is MapFeatureSelection.Population &&
                    (selectedFeature as MapFeatureSelection.Population).zone.id == zone.id

            val rect = RectF(left, top, right, bottom)
            fillPaint.color = fillColorInt
            fillPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(rect, 24f, 24f, fillPaint)

            strokePaint.color = if (isSelected) 0xFFFFD700.toInt() else strokeColorInt
            strokePaint.strokeWidth = if (isSelected) 6f else 3f
            canvas.drawRoundRect(rect, 24f, 24f, strokePaint)

            val centerPt = proj.toPixels(GeoPoint(zone.centerLocation.latitude, zone.centerLocation.longitude), reusedPoint)
            textPaint.color = android.graphics.Color.WHITE
            textPaint.textSize = 28f
            textPaint.setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
            canvas.drawText(zone.regionName, centerPt.x.toFloat(), centerPt.y.toFloat(), textPaint)
            textPaint.clearShadowLayer()
        }

        barriers.forEach { barrier ->
            val points = if (barrier.geometryPath.size >= 2) barrier.geometryPath else {
                listOf(
                    GeoLocation(barrier.location.latitude - 0.05, barrier.location.longitude - 0.05),
                    barrier.location,
                    GeoLocation(barrier.location.latitude + 0.05, barrier.location.longitude + 0.05)
                )
            }

            val path = android.graphics.Path()
            val projectedPoints = points.map { geo ->
                proj.toPixels(GeoPoint(geo.latitude, geo.longitude), null)
            }

            projectedPoints.forEachIndexed { idx, pt ->
                if (idx == 0) path.moveTo(pt.x.toFloat(), pt.y.toFloat())
                else path.lineTo(pt.x.toFloat(), pt.y.toFloat())
            }

            val (lineColorInt, strokeWidthPx, dashEffect) = when (barrier.type) {
                BarrierType.HIGHWAY -> Triple(0xFFFF5722.toInt(), 14f, null)
                BarrierType.RAILWAY -> Triple(0xFF0288D1.toInt(), 10f, DashPathEffect(floatArrayOf(20f, 10f), 0f))
                BarrierType.DAM -> Triple(0xFF00BCD4.toInt(), 18f, null)
                BarrierType.FENCE -> Triple(0xFFFFC107.toInt(), 8f, DashPathEffect(floatArrayOf(12f, 12f), 0f))
                BarrierType.URBAN_WALL -> Triple(0xFF9C27B0.toInt(), 14f, null)
                BarrierType.CANAL -> Triple(0xFF0288D1.toInt(), 12f, null)
            }

            val isSelected = selectedFeature is MapFeatureSelection.Barrier &&
                    (selectedFeature as MapFeatureSelection.Barrier).barrier.id == barrier.id

            if (isSelected) {
                strokePaint.color = 0xFFFFD700.toInt()
                strokePaint.strokeWidth = strokeWidthPx + 10f
                strokePaint.pathEffect = null
                strokePaint.strokeCap = Paint.Cap.ROUND
                strokePaint.strokeJoin = Paint.Join.ROUND
                canvas.drawPath(path, strokePaint)
            }

            strokePaint.color = lineColorInt
            strokePaint.strokeWidth = strokeWidthPx
            strokePaint.pathEffect = dashEffect
            strokePaint.strokeCap = Paint.Cap.ROUND
            strokePaint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(path, strokePaint)
            strokePaint.pathEffect = null

            val midPt = projectedPoints[projectedPoints.size / 2]
            fillPaint.color = android.graphics.Color.BLACK
            canvas.drawCircle(midPt.x.toFloat(), midPt.y.toFloat(), 18f, fillPaint)
            fillPaint.color = lineColorInt
            canvas.drawCircle(midPt.x.toFloat(), midPt.y.toFloat(), 14f, fillPaint)
        }

        collisionHotspots.forEach { hotspot ->
            val pt = proj.toPixels(GeoPoint(hotspot.location.latitude, hotspot.location.longitude), reusedPoint)
            val px = pt.x.toFloat()
            val py = pt.y.toFloat()

            val (pinColorInt, isCritical) = when (hotspot.severity) {
                CollisionSeverity.CRITICAL -> 0xFFFF1744.toInt() to true
                CollisionSeverity.HIGH -> 0xFFFF9100.toInt() to false
                CollisionSeverity.MODERATE -> 0xFFFFEA00.toInt() to false
                CollisionSeverity.LOW -> 0xFF76FF03.toInt() to false
            }

            val baseRadius = 26f

            if (isCritical) {
                val alphaInt = (pulseAlphaFraction * 255).toInt().coerceIn(0, 255)
                fillPaint.color = (pinColorInt and 0x00FFFFFF) or (alphaInt shl 24)
                canvas.drawCircle(px, py, baseRadius * pulseRadiusFraction, fillPaint)
            }

            val isSelected = selectedFeature is MapFeatureSelection.Hotspot &&
                    (selectedFeature as MapFeatureSelection.Hotspot).hotspot.id == hotspot.id

            if (isSelected) {
                fillPaint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(px, py, baseRadius + 12f, fillPaint)
            }

            fillPaint.color = android.graphics.Color.BLACK
            canvas.drawCircle(px, py, baseRadius + 4f, fillPaint)

            fillPaint.color = pinColorInt
            canvas.drawCircle(px, py, baseRadius, fillPaint)

            textPaint.color = android.graphics.Color.BLACK
            textPaint.textSize = 24f
            canvas.drawText("${hotspot.incidentCount}", px, py + 8f, textPaint)
        }

        wildlifeOccurrences.forEach { occ ->
            val pt = proj.toPixels(GeoPoint(occ.location.latitude, occ.location.longitude), reusedPoint)
            val px = pt.x.toFloat()
            val py = pt.y.toFloat()

            val taxon = occ.taxonGroup.lowercase()
            val markerColorInt = when {
                taxon.contains("mammal") -> 0xFFFFB300.toInt()
                taxon.contains("bird") || taxon.contains("aves") -> 0xFF29B6F6.toInt()
                taxon.contains("reptile") || taxon.contains("amphibian") -> 0xFF66BB6A.toInt()
                taxon.contains("fish") || taxon.contains("aquatic") -> 0xFF26A69A.toInt()
                else -> 0xFFAB47BC.toInt()
            }

            val radius = 22f

            val isSelected = selectedFeature is MapFeatureSelection.Wildlife &&
                    (selectedFeature as MapFeatureSelection.Wildlife).occurrence.id == occ.id

            if (isSelected) {
                fillPaint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(px, py, radius + 12f, fillPaint)
            }

            fillPaint.color = android.graphics.Color.WHITE
            canvas.drawCircle(px, py, radius + 4f, fillPaint)

            fillPaint.color = markerColorInt
            canvas.drawCircle(px, py, radius, fillPaint)

            val symbolLetter = when {
                taxon.contains("mammal") -> "M"
                taxon.contains("bird") || taxon.contains("aves") -> "B"
                taxon.contains("reptile") || taxon.contains("amphibian") -> "R"
                taxon.contains("fish") -> "F"
                else -> "W"
            }

            textPaint.color = android.graphics.Color.WHITE
            textPaint.textSize = 22f
            canvas.drawText(symbolLetter, px, py + 7f, textPaint)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
        if (e == null || mapView == null) return false
        val tapX = e.x
        val tapY = e.y
        val proj = mapView.projection
        val maxDistPx = 60f

        wildlifeOccurrences.forEach { occ ->
            val pt = proj.toPixels(GeoPoint(occ.location.latitude, occ.location.longitude), reusedPoint)
            val dx = pt.x - tapX
            val dy = pt.y - tapY
            if (dx * dx + dy * dy <= maxDistPx * maxDistPx) {
                onFeatureSelected(MapFeatureSelection.Wildlife(occ))
                return true
            }
        }

        collisionHotspots.forEach { hs ->
            val pt = proj.toPixels(GeoPoint(hs.location.latitude, hs.location.longitude), reusedPoint)
            val dx = pt.x - tapX
            val dy = pt.y - tapY
            if (dx * dx + dy * dy <= maxDistPx * maxDistPx) {
                onFeatureSelected(MapFeatureSelection.Hotspot(hs))
                return true
            }
        }

        barriers.forEach { barrier ->
            val pt = proj.toPixels(GeoPoint(barrier.location.latitude, barrier.location.longitude), reusedPoint)
            val dx = pt.x - tapX
            val dy = pt.y - tapY
            if (dx * dx + dy * dy <= (maxDistPx + 20f) * (maxDistPx + 20f)) {
                onFeatureSelected(MapFeatureSelection.Barrier(barrier))
                return true
            }
        }

        populationZones.forEach { zone ->
            val pt = proj.toPixels(GeoPoint(zone.centerLocation.latitude, zone.centerLocation.longitude), reusedPoint)
            val dx = pt.x - tapX
            val dy = pt.y - tapY
            if (dx * dx + dy * dy <= (maxDistPx + 40f) * (maxDistPx + 40f)) {
                onFeatureSelected(MapFeatureSelection.Population(zone))
                return true
            }
        }

        onFeatureSelected(null)
        return false
    }
}

private fun projectGeoToCanvas(
    location: GeoLocation,
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float
): Offset {
    val centerLatRad = Math.toRadians(mapCenter.latitude)
    val scale = (Math.pow(2.0, zoomLevel.toDouble()) * (width / 60.0)).toFloat()

    val deltaLon = (location.longitude - mapCenter.longitude).toFloat()
    val deltaLat = (location.latitude - mapCenter.latitude).toFloat()

    val x = (width / 2f) + panX + (deltaLon * scale * Math.cos(centerLatRad).toFloat())
    val y = (height / 2f) + panY - (deltaLat * scale)
    return Offset(x, y)
}

private fun DrawScope.drawPreviewMapGrid(
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float
) {
    drawRect(color = Color(0xFF0F172A))

    val labelPaint = Paint().apply {
        color = android.graphics.Color.argb(120, 255, 255, 255)
        textSize = 24f
        isAntiAlias = true
    }

    for (lat in -80..80 step 5) {
        val pt = projectGeoToCanvas(GeoLocation(lat.toDouble(), mapCenter.longitude), mapCenter, zoomLevel, panX, panY, width, height)
        if (pt.y in 0f..height) {
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, pt.y),
                end = Offset(width, pt.y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${if (lat >= 0) "$lat°N" else "${-lat}°S"}",
                12f,
                pt.y - 6f,
                labelPaint
            )
        }
    }

    for (lon in -180..180 step 5) {
        val pt = projectGeoToCanvas(GeoLocation(mapCenter.latitude, lon.toDouble()), mapCenter, zoomLevel, panX, panY, width, height)
        if (pt.x in 0f..width) {
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(pt.x, 0f),
                end = Offset(pt.x, height),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${if (lon >= 0) "$lon°E" else "${-lon}°W"}",
                pt.x + 6f,
                height - 12f,
                labelPaint
            )
        }
    }
}

private fun DrawScope.drawPopulationZone(
    zone: PopulationDensityZone,
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float,
    isSelected: Boolean
) {
    val bbox = zone.boundingBox
    val topLeft = projectGeoToCanvas(GeoLocation(bbox.maxLat, bbox.minLon), mapCenter, zoomLevel, panX, panY, width, height)
    val bottomRight = projectGeoToCanvas(GeoLocation(bbox.minLat, bbox.maxLon), mapCenter, zoomLevel, panX, panY, width, height)

    val rectWidth = (bottomRight.x - topLeft.x).coerceAtLeast(40f)
    val rectHeight = (bottomRight.y - topLeft.y).coerceAtLeast(40f)

    val (fillColor, strokeColor) = when (zone.urbanLevel) {
        UrbanLevel.METROPOLITAN, UrbanLevel.URBAN -> Color(0x66E53935) to Color(0xFFE53935)
        UrbanLevel.SUBURBAN -> Color(0x55FB8C00) to Color(0xFFFB8C00)
        UrbanLevel.RURAL -> Color(0x44FDD835) to Color(0xFFFDD835)
        UrbanLevel.WILDERNESS -> Color(0x221E88E5) to Color(0xFF1E88E5)
    }

    drawRoundRect(
        color = fillColor,
        topLeft = topLeft,
        size = Size(rectWidth, rectHeight),
        cornerRadius = CornerRadius(16f)
    )

    drawRoundRect(
        color = if (isSelected) Color(0xFFFFD700) else strokeColor.copy(alpha = 0.6f),
        topLeft = topLeft,
        size = Size(rectWidth, rectHeight),
        cornerRadius = CornerRadius(16f),
        style = Stroke(width = if (isSelected) 4f else 2f)
    )

    val centerPt = projectGeoToCanvas(zone.centerLocation, mapCenter, zoomLevel, panX, panY, width, height)
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 26f
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(
        zone.regionName,
        centerPt.x,
        centerPt.y,
        textPaint
    )
}

private fun DrawScope.drawBarrierFeature(
    barrier: BarrierFeature,
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float,
    isSelected: Boolean
) {
    val points = if (barrier.geometryPath.size >= 2) barrier.geometryPath else {
        listOf(
            GeoLocation(barrier.location.latitude - 0.05, barrier.location.longitude - 0.05),
            barrier.location,
            GeoLocation(barrier.location.latitude + 0.05, barrier.location.longitude + 0.05)
        )
    }

    val projectedPoints = points.map { projectGeoToCanvas(it, mapCenter, zoomLevel, panX, panY, width, height) }

    val path = Path().apply {
        projectedPoints.forEachIndexed { idx, pt ->
            if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
    }

    val (lineColor, strokeWidthPx, pathEffect) = when (barrier.type) {
        BarrierType.HIGHWAY -> Triple(Color(0xFFFF5722), 12f, null)
        BarrierType.RAILWAY -> Triple(Color(0xFF0288D1), 8f, PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
        BarrierType.DAM -> Triple(Color(0xFF00BCD4), 16f, null)
        BarrierType.FENCE -> Triple(Color(0xFFFFC107), 6f, PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        BarrierType.URBAN_WALL -> Triple(Color(0xFF9C27B0), 12f, null)
        BarrierType.CANAL -> Triple(Color(0xFF0288D1), 10f, null)
    }

    if (isSelected) {
        drawPath(
            path = path,
            color = Color(0xFFFFD700),
            style = Stroke(width = strokeWidthPx + 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = pathEffect
        )
    )

    val midPt = projectedPoints[projectedPoints.size / 2]
    drawCircle(
        color = Color.Black,
        radius = 16f,
        center = midPt
    )
    drawCircle(
        color = lineColor,
        radius = 12f,
        center = midPt
    )
}

private fun DrawScope.drawCollisionHotspot(
    hotspot: CollisionHotspot,
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float,
    pulseRadiusFraction: Float,
    pulseAlphaFraction: Float,
    isSelected: Boolean
) {
    val pt = projectGeoToCanvas(hotspot.location, mapCenter, zoomLevel, panX, panY, width, height)

    val (pinColor, isCritical) = when (hotspot.severity) {
        CollisionSeverity.CRITICAL -> Color(0xFFFF1744) to true
        CollisionSeverity.HIGH -> Color(0xFFFF9100) to false
        CollisionSeverity.MODERATE -> Color(0xFFFFEA00) to false
        CollisionSeverity.LOW -> Color(0xFF76FF03) to false
    }

    val baseRadius = 24f

    if (isCritical) {
        drawCircle(
            color = pinColor.copy(alpha = pulseAlphaFraction),
            radius = baseRadius * pulseRadiusFraction,
            center = pt
        )
    }

    if (isSelected) {
        drawCircle(
            color = Color(0xFFFFD700),
            radius = baseRadius + 10f,
            center = pt
        )
    }

    drawCircle(
        color = Color.Black,
        radius = baseRadius + 3f,
        center = pt
    )
    drawCircle(
        color = pinColor,
        radius = baseRadius,
        center = pt
    )

    val textPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 22f
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(
        "${hotspot.incidentCount}",
        pt.x,
        pt.y + 8f,
        textPaint
    )
}

private fun DrawScope.drawWildlifeOccurrence(
    occ: WildlifeOccurrence,
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float,
    isSelected: Boolean
) {
    val pt = projectGeoToCanvas(occ.location, mapCenter, zoomLevel, panX, panY, width, height)

    val taxon = occ.taxonGroup.lowercase()
    val markerColor = when {
        taxon.contains("mammal") -> Color(0xFFFFB300)
        taxon.contains("bird") || taxon.contains("aves") -> Color(0xFF29B6F6)
        taxon.contains("reptile") || taxon.contains("amphibian") -> Color(0xFF66BB6A)
        taxon.contains("fish") || taxon.contains("aquatic") -> Color(0xFF26A69A)
        else -> Color(0xFFAB47BC)
    }

    val radius = 20f

    if (isSelected) {
        drawCircle(
            color = Color(0xFFFFD700),
            radius = radius + 10f,
            center = pt
        )
    }

    drawCircle(
        color = Color.White,
        radius = radius + 3f,
        center = pt
    )

    drawCircle(
        color = markerColor,
        radius = radius,
        center = pt
    )

    val symbolLetter = when {
        taxon.contains("mammal") -> "M"
        taxon.contains("bird") || taxon.contains("aves") -> "B"
        taxon.contains("reptile") || taxon.contains("amphibian") -> "R"
        taxon.contains("fish") -> "F"
        else -> "W"
    }

    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 20f
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(
        symbolLetter,
        pt.x,
        pt.y + 7f,
        textPaint
    )
}

private fun findHitFeature(
    tapOffset: Offset,
    mapCenter: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    width: Float,
    height: Float,
    wildlifeOccurrences: List<WildlifeOccurrence>,
    collisionHotspots: List<CollisionHotspot>,
    barriers: List<BarrierFeature>,
    populationZones: List<PopulationDensityZone>
): MapFeatureSelection? {
    val maxHitDistancePx = 60f

    wildlifeOccurrences.forEach { occ ->
        val pt = projectGeoToCanvas(occ.location, mapCenter, zoomLevel, panX, panY, width, height)
        val dist = (pt - tapOffset).getDistance()
        if (dist <= maxHitDistancePx) {
            return MapFeatureSelection.Wildlife(occ)
        }
    }

    collisionHotspots.forEach { hs ->
        val pt = projectGeoToCanvas(hs.location, mapCenter, zoomLevel, panX, panY, width, height)
        val dist = (pt - tapOffset).getDistance()
        if (dist <= maxHitDistancePx) {
            return MapFeatureSelection.Hotspot(hs)
        }
    }

    barriers.forEach { barrier ->
        val pt = projectGeoToCanvas(barrier.location, mapCenter, zoomLevel, panX, panY, width, height)
        val dist = (pt - tapOffset).getDistance()
        if (dist <= maxHitDistancePx + 20f) {
            return MapFeatureSelection.Barrier(barrier)
        }
    }

    populationZones.forEach { zone ->
        val pt = projectGeoToCanvas(zone.centerLocation, mapCenter, zoomLevel, panX, panY, width, height)
        val dist = (pt - tapOffset).getDistance()
        if (dist <= maxHitDistancePx + 40f) {
            return MapFeatureSelection.Population(zone)
        }
    }

    return null
}
