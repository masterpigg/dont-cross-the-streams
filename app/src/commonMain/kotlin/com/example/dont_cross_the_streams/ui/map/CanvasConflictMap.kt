package com.example.dont_cross_the_streams.ui.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Result of asking a [MapTileLoader] for one tile. */
sealed interface TileLoadResult {
    data object Pending : TileLoadResult
    data object Failed : TileLoadResult
    class Ready(val image: ImageBitmap) : TileLoadResult
}

/**
 * Platform raster tile source. [load] is polled: it starts the download on first call and returns
 * [TileLoadResult.Ready] exactly once when the image is decoded, so the caller owns caching.
 */
interface MapTileLoader {
    fun load(z: Int, x: Int, y: Int): TileLoadResult

    /** Hint that only tiles at zoom [z] are wanted now; queued requests for other levels can go. */
    fun retainOnly(z: Int) {}
}

// ---------------------------------------------------------------------------------------------
// Camera & projection
// ---------------------------------------------------------------------------------------------

private data class MapCamera(val center: GeoLocation, val zoom: Float)

private data class TileRange(val z: Int, val minX: Int, val maxX: Int, val minY: Int, val maxY: Int)

private const val MIN_TILE_ZOOM = 0
private const val MAX_TILE_ZOOM = 19
private const val MAX_CACHED_TILES = 320

/** Mouse wheel: ~100 px per notch -> half a zoom level. */
private const val WHEEL_ZOOM_PER_PX = 0.005f

/** Trackpad pinch arrives as ctrl+wheel with small deltas, so it needs a higher gain. */
private const val PINCH_ZOOM_PER_PX = 0.02f

private fun tileKey(z: Int, x: Int, y: Int) = "$z/$x/$y"

private fun tileZoomFor(zoom: Float): Int = zoom.roundToInt().coerceIn(MIN_TILE_ZOOM, MAX_TILE_ZOOM)

private fun visibleTileRange(camera: MapCamera, size: Size): TileRange? {
    if (size.width <= 0f || size.height <= 0f) return null
    val z = tileZoomFor(camera.zoom)
    val cx = WebMercator.lonToX(camera.center.longitude, z.toDouble())
    val cy = WebMercator.latToY(camera.center.latitude, z.toDouble())
    // Screen pixels per tile-zoom pixel.
    val scale = 2.0.pow((camera.zoom - z).toDouble())
    val halfW = size.width / 2.0 / scale
    val halfH = size.height / 2.0 / scale
    val n = 1 shl z
    return TileRange(
        z = z,
        minX = floor((cx - halfW) / WebMercator.TILE_SIZE).toInt(),
        maxX = floor((cx + halfW) / WebMercator.TILE_SIZE).toInt(),
        minY = floor((cy - halfH) / WebMercator.TILE_SIZE).toInt().coerceIn(0, n - 1),
        maxY = floor((cy + halfH) / WebMercator.TILE_SIZE).toInt().coerceIn(0, n - 1)
    )
}

private fun wrapTileX(x: Int, z: Int): Int {
    val n = 1 shl z
    return ((x % n) + n) % n
}

/** Screen position of [geo] for the given camera. */
private fun project(geo: GeoLocation, camera: MapCamera, size: Size): Offset {
    val zoom = camera.zoom.toDouble()
    var dx = WebMercator.lonToX(geo.longitude, zoom) - WebMercator.lonToX(camera.center.longitude, zoom)
    // Take the shortest way around the globe so features near the antimeridian stay put.
    val world = WebMercator.worldSize(zoom)
    if (dx > world / 2) dx -= world else if (dx < -world / 2) dx += world
    val dy = WebMercator.latToY(geo.latitude, zoom) - WebMercator.latToY(camera.center.latitude, zoom)
    return Offset((size.width / 2.0 + dx).toFloat(), (size.height / 2.0 + dy).toFloat())
}

private fun Offset.isNear(size: Size, marginPx: Float): Boolean =
    x >= -marginPx && y >= -marginPx && x <= size.width + marginPx && y <= size.height + marginPx

private class PushHolder {
    var job: Job? = null
}

/**
 * Slippy-map renderer shared by Web and iOS: Web Mercator projection, live drag/pinch/wheel camera,
 * raster tiles from [tileLoader] (or a moving graticule when there is none), and all overlay layers.
 */
@Composable
fun CanvasConflictMap(
    mapCenter: GeoLocation,
    zoomLevel: Float,
    wildlifeOccurrences: List<WildlifeOccurrence>,
    collisionHotspots: List<CollisionHotspot>,
    barriers: List<BarrierFeature>,
    populationZones: List<PopulationDensityZone>,
    wildlifeCrossings: List<WildlifeCrossing>,
    collisionReports: List<CollisionReport>,
    selectedFeature: MapFeatureSelection?,
    onResetView: () -> Unit,
    onFeatureSelected: (MapFeatureSelection?) -> Unit,
    onSelectPreset: (ConflictRegionPreset) -> Unit,
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit,
    modifier: Modifier = Modifier,
    tileLoader: MapTileLoader? = null,
    attribution: String = "",
    // Converts a platform wheel event's delta units to pixels (browsers may report lines/pages).
    wheelDeltaUnitPx: (nativeEvent: Any?) -> Float = { 1f }
) {
    // The map owns a live camera that gestures update synchronously every pointer event. Waiting
    // for each move to round-trip through the ViewModel is what made dragging lag, jump back and
    // "stick". The ViewModel is told about the camera once a gesture settles.
    var camera by remember {
        mutableStateOf(MapCamera(WebMercator.normalize(mapCenter), zoomLevel.coerceIn(WebMercator.MIN_ZOOM, WebMercator.MAX_ZOOM)))
    }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val scope = rememberCoroutineScope()
    val pushHolder = remember { PushHolder() }
    // Cameras we reported to the ViewModel. When one comes back as props it's our own echo and is
    // ignored; anything else (preset, reset button) is an external move and replaces the camera.
    val echoes = remember { ArrayDeque<MapCamera>() }

    val currentOnCameraChanged by rememberUpdatedState(onMapCenterAndZoomChanged)
    val currentOnFeatureSelected by rememberUpdatedState(onFeatureSelected)
    val currentWildlife by rememberUpdatedState(wildlifeOccurrences)
    val currentHotspots by rememberUpdatedState(collisionHotspots)
    val currentBarriers by rememberUpdatedState(barriers)
    val currentZones by rememberUpdatedState(populationZones)
    val currentCrossings by rememberUpdatedState(wildlifeCrossings)
    val currentReports by rememberUpdatedState(collisionReports)
    val currentWheelDeltaUnitPx by rememberUpdatedState(wheelDeltaUnitPx)

    LaunchedEffect(mapCenter, zoomLevel) {
        val incoming = MapCamera(mapCenter, zoomLevel)
        if (incoming !in echoes) {
            camera = MapCamera(WebMercator.normalize(mapCenter), zoomLevel.coerceIn(WebMercator.MIN_ZOOM, WebMercator.MAX_ZOOM))
        }
    }

    fun pushCamera(delayMs: Long = 0L) {
        pushHolder.job?.cancel()
        pushHolder.job = scope.launch {
            if (delayMs > 0) delay(delayMs)
            val settled = camera
            echoes.addLast(settled)
            while (echoes.size > 16) echoes.removeFirst()
            currentOnCameraChanged(settled.center, settled.zoom)
        }
    }

    fun moveCamera(panX: Float, panY: Float, zoomDelta: Float, focus: Offset?) {
        val current = camera
        var center = if (panX != 0f || panY != 0f) {
            WebMercator.panBy(current.center, current.zoom.toDouble(), panX.toDouble(), panY.toDouble())
        } else {
            current.center
        }
        val newZoom = (current.zoom + zoomDelta).coerceIn(WebMercator.MIN_ZOOM, WebMercator.MAX_ZOOM)
        if (newZoom != current.zoom) {
            val size = canvasSize
            val fx = focus?.let { it.x - size.width / 2f } ?: 0f
            val fy = focus?.let { it.y - size.height / 2f } ?: 0f
            center = WebMercator.zoomAround(center, current.zoom.toDouble(), newZoom.toDouble(), fx.toDouble(), fy.toDouble())
        }
        camera = MapCamera(center, newZoom)
    }

    // Raster tiles, keyed "z/x/y" with x wrapped into range.
    val tileCache = remember { mutableStateMapOf<String, ImageBitmap>() }
    val tileOrder = remember { ArrayDeque<String>() }

    LaunchedEffect(tileLoader) {
        val loader = tileLoader ?: return@LaunchedEffect
        snapshotFlow { visibleTileRange(camera, canvasSize) }
            .distinctUntilChanged()
            .collectLatest { range ->
                if (range == null) return@collectLatest
                loader.retainOnly(range.z)
                while (isActive) {
                    var pending = false
                    for (ty in range.minY..range.maxY) {
                        for (tx in range.minX..range.maxX) {
                            val wx = wrapTileX(tx, range.z)
                            val key = tileKey(range.z, wx, ty)
                            if (tileCache.containsKey(key)) continue
                            when (val result = loader.load(range.z, wx, ty)) {
                                TileLoadResult.Pending -> pending = true
                                TileLoadResult.Failed -> Unit // retried on a later view
                                is TileLoadResult.Ready -> {
                                    tileCache[key] = result.image
                                    tileOrder.addLast(key)
                                }
                            }
                        }
                    }
                    if (!pending) break
                    delay(50)
                }
                evictTiles(tileCache, tileOrder, range)
            }
    }

    // Measured labels are reused across frames; the default cache (8) thrashes with this many labels.
    val textMeasurer = rememberTextMeasurer(cacheSize = 256)

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

    Box(modifier = modifier.fillMaxSize()) {
        // Three stacked layers share one gesture surface: the street map, the animated hotspot
        // pulses, then the markers. Only the cheap pulse layer repaints every animation frame, and
        // the pulses sit under the markers instead of tinting them.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                // Mouse wheel and trackpad: zoom around the cursor, proportional to the scroll amount.
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type != PointerEventType.Scroll) continue
                            val change = event.changes.firstOrNull() ?: continue
                            val deltaY = change.scrollDelta.y
                            if (deltaY == 0f) continue
                            event.changes.forEach { it.consume() }
                            val unitPx = currentWheelDeltaUnitPx(event.nativeEvent)
                            val gain = if (event.keyboardModifiers.isCtrlPressed) PINCH_ZOOM_PER_PX else WHEEL_ZOOM_PER_PX
                            val zoomDelta = (-deltaY * unitPx * gain).coerceIn(-1f, 1f)
                            moveCamera(0f, 0f, zoomDelta, change.position)
                            pushCamera(delayMs = 250L)
                        }
                    }
                }
                // Drag to pan (mouse or one finger) and pinch to zoom (two fingers), applied live.
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var pastSlop = false
                        var accumulatedZoom = 1f
                        var accumulatedPan = Offset.Zero
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                var pan = panChange
                                var zoom = zoomChange
                                if (!pastSlop) {
                                    accumulatedZoom *= zoomChange
                                    accumulatedPan += panChange
                                    val zoomMotion = abs(1 - accumulatedZoom) * event.calculateCentroidSize(useCurrent = false)
                                    val touchSlop = viewConfiguration.touchSlop
                                    pastSlop = zoomMotion > touchSlop || accumulatedPan.getDistance() > touchSlop
                                    // Catch up on the movement made inside the slop window, so the spot
                                    // that was grabbed stays exactly under the cursor/finger.
                                    pan = accumulatedPan
                                    zoom = accumulatedZoom
                                }
                                if (pastSlop) {
                                    if (zoom != 1f || pan != Offset.Zero) {
                                        moveCamera(
                                            panX = pan.x,
                                            panY = pan.y,
                                            zoomDelta = log2(zoom),
                                            focus = event.calculateCentroid(useCurrent = false)
                                        )
                                    }
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        } while (!canceled && event.changes.any { it.pressed })
                        if (pastSlop) pushCamera()
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tap ->
                            moveCamera(0f, 0f, 1f, tap)
                            pushCamera()
                        },
                        onTap = { tap ->
                            val size = canvasSize
                            if (size.width <= 0f || size.height <= 0f) return@detectTapGestures
                            currentOnFeatureSelected(
                                hitTest(
                                    tap = tap,
                                    camera = camera,
                                    size = size,
                                    thresholdPx = 24.dp.toPx(),
                                    reports = currentReports,
                                    crossings = currentCrossings,
                                    hotspots = currentHotspots,
                                    wildlife = currentWildlife,
                                    barriers = currentBarriers,
                                    zones = currentZones
                                )
                            )
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cam = camera
                drawRect(color = Color(0xFF161C26))
                if (tileLoader != null) drawTiles(cam, tileCache) else drawGraticule(cam)
                // Dark tint so the colored overlays stay legible on top of the street map.
                drawRect(color = Color(0x660D121B))

                drawPopulationZones(cam, populationZones, textMeasurer)
                drawBarriers(cam, barriers, textMeasurer)
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cam = camera
                for (hotspot in collisionHotspots) {
                    val pos = project(hotspot.location, cam, size)
                    if (!pos.isNear(size, 60f)) continue
                    drawCircle(
                        color = getSeverityColor(hotspot.severity).copy(alpha = pulseAlpha),
                        radius = 12.dp.toPx() * pulseScale,
                        center = pos
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cam = camera
                drawWildlife(cam, wildlifeOccurrences, textMeasurer)
                drawHotspots(cam, collisionHotspots, textMeasurer)
                for (crossing in wildlifeCrossings) {
                    val pos = project(crossing.location, cam, size)
                    if (!pos.isNear(size, 30f)) continue
                    drawWildlifeCrossingMarker(pos, selected = selectedFeature?.id == crossing.id)
                }
                for (report in collisionReports) {
                    val pos = project(report.location, cam, size)
                    if (!pos.isNear(size, 10f)) continue
                    drawCollisionReportMarker(pos, selected = selectedFeature?.id == report.id)
                }

                selectedFeature?.let { selection ->
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 20.dp.toPx(),
                        center = project(selection.location, cam, size),
                        style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )
                }

                if (attribution.isNotEmpty()) {
                    val attributionLayout = textMeasurer.measure(
                        text = attribution,
                        style = TextStyle(color = Color(0xEEFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    )
                    drawText(
                        textLayoutResult = attributionLayout,
                        topLeft = Offset(16f, size.height - attributionLayout.size.height - 6f)
                    )
                }
            }
        }

        CollisionCrossingLegend(
            showCollisionReports = collisionReports.isNotEmpty(),
            showCrossings = wildlifeCrossings.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 96.dp)
        )

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

        // Overlay: Map Control Buttons (Pan, Zoom, Recenter). These move the live camera directly
        // so they compose correctly with any gesture that's still settling.
        val panStepPx = 160f
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
                    onClick = { moveCamera(0f, panStepPx, 0f, null); pushCamera() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.KeyboardArrowUp, contentDescription = "Pan North")
                }
                Row {
                    IconButton(
                        onClick = { moveCamera(panStepPx, 0f, 0f, null); pushCamera() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Pan West")
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
                        onClick = { moveCamera(-panStepPx, 0f, 0f, null); pushCamera() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Pan East")
                    }
                }
                IconButton(
                    onClick = { moveCamera(0f, -panStepPx, 0f, null); pushCamera() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = "Pan South")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.size(width = 36.dp, height = 1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}
                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = { moveCamera(0f, 0f, 1f, null); pushCamera() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Zoom In")
                }
                Text(
                    text = "${camera.zoom.roundToInt()}x",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { moveCamera(0f, 0f, -1f, null); pushCamera() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Zoom Out")
                }
            }
        }
    }
}

private fun evictTiles(cache: MutableMap<String, ImageBitmap>, order: ArrayDeque<String>, visible: TileRange) {
    if (cache.size <= MAX_CACHED_TILES) return
    // Keep the visible tiles and their immediate parents/children (zoom fallbacks); drop the oldest rest.
    val iterator = order.iterator()
    while (cache.size > MAX_CACHED_TILES && iterator.hasNext()) {
        val key = iterator.next()
        val z = key.substringBefore('/').toIntOrNull()
        if (z != null && abs(z - visible.z) <= 1) continue
        cache.remove(key)
        iterator.remove()
    }
}

// ---------------------------------------------------------------------------------------------
// Drawing
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drawTiles(camera: MapCamera, cache: Map<String, ImageBitmap>) {
    val range = visibleTileRange(camera, size) ?: return
    val z = range.z
    val scale = 2.0.pow((camera.zoom - z).toDouble())
    val tileScreenSize = WebMercator.TILE_SIZE * scale
    val cx = WebMercator.lonToX(camera.center.longitude, z.toDouble())
    val cy = WebMercator.latToY(camera.center.latitude, z.toDouble())

    for (ty in range.minY..range.maxY) {
        for (tx in range.minX..range.maxX) {
            // Round both edges (not origin + size) so neighbouring tiles share an edge: no seams.
            val left = (size.width / 2.0 + (tx * WebMercator.TILE_SIZE - cx) * scale)
            val top = (size.height / 2.0 + (ty * WebMercator.TILE_SIZE - cy) * scale)
            val l = left.roundToInt()
            val t = top.roundToInt()
            val dstOffset = IntOffset(l, t)
            val dstSize = IntSize((left + tileScreenSize).roundToInt() - l, (top + tileScreenSize).roundToInt() - t)
            val wx = wrapTileX(tx, z)

            val exact = cache[tileKey(z, wx, ty)]
            if (exact != null) {
                drawImage(image = exact, dstOffset = dstOffset, dstSize = dstSize)
                continue
            }
            // While a tile loads, stretch the nearest cached ancestor so zooming never flashes blank...
            for (d in 1..5) {
                val az = z - d
                if (az < MIN_TILE_ZOOM) break
                val ancestor = cache[tileKey(az, wx shr d, ty shr d)] ?: continue
                val sub = 256 shr d
                drawImage(
                    image = ancestor,
                    srcOffset = IntOffset((wx - ((wx shr d) shl d)) * sub, (ty - ((ty shr d) shl d)) * sub),
                    srcSize = IntSize(sub, sub),
                    dstOffset = dstOffset,
                    dstSize = dstSize
                )
                break
            }
            // ...and after zooming out, reuse the sharper child tiles that are already loaded.
            if (z + 1 <= MAX_TILE_ZOOM) {
                val halfW = dstSize.width / 2
                val halfH = dstSize.height / 2
                for (dy in 0..1) {
                    for (dx in 0..1) {
                        val child = cache[tileKey(z + 1, wx * 2 + dx, ty * 2 + dy)] ?: continue
                        drawImage(
                            image = child,
                            dstOffset = IntOffset(l + dx * halfW, t + dy * halfH),
                            dstSize = IntSize(
                                if (dx == 0) halfW else dstSize.width - halfW,
                                if (dy == 0) halfH else dstSize.height - halfH
                            )
                        )
                    }
                }
            }
        }
    }
}

/** Lat/lon grid that moves with the map; the basemap stand-in when no tile source is available. */
private fun DrawScope.drawGraticule(camera: MapCamera) {
    val stepDeg = when {
        camera.zoom < 4f -> 20.0
        camera.zoom < 6f -> 10.0
        camera.zoom < 8f -> 2.0
        camera.zoom < 10f -> 1.0
        camera.zoom < 12f -> 0.25
        else -> 0.05
    }
    val bounds = WebMercator.visibleBounds(camera.center, camera.zoom.toDouble(), size.width.toDouble(), size.height.toDouble())
    val lineColor = Color(0x334A608A)
    var lon = floor(bounds.minLon / stepDeg) * stepDeg
    while (lon <= bounds.maxLon) {
        val x = project(GeoLocation(camera.center.latitude, lon), camera, size).x
        drawLine(color = lineColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
        lon += stepDeg
    }
    var lat = floor(bounds.minLat / stepDeg) * stepDeg
    while (lat <= bounds.maxLat) {
        val y = project(GeoLocation(lat, camera.center.longitude), camera, size).y
        drawLine(color = lineColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
        lat += stepDeg
    }
}

private fun DrawScope.drawPopulationZones(camera: MapCamera, zones: List<PopulationDensityZone>, textMeasurer: TextMeasurer) {
    val minRadius = 6.dp.toPx()
    for (zone in zones) {
        // Size the heat blob from the zone's real extent so it scales with the map, not the screen.
        val box = zone.boundingBox
        val nw = project(GeoLocation(box.maxLat, box.minLon), camera, size)
        val se = project(GeoLocation(box.minLat, box.maxLon), camera, size)
        val center = project(zone.centerLocation, camera, size)
        val radius = maxOf(abs(se.x - nw.x), abs(se.y - nw.y), 2 * minRadius) / 2f
        if (!center.isNear(size, radius)) continue

        val heatColor = when {
            zone.densityScore > 1000 -> Color(0x66FF3D00)
            zone.densityScore > 500 -> Color(0x55FF9100)
            zone.densityScore > 200 -> Color(0x44FFEA00)
            else -> Color(0x3300E676)
        }
        drawCircle(color = heatColor, radius = radius, center = center)
        drawCircle(color = heatColor.copy(alpha = 0.8f), radius = radius, center = center, style = Stroke(width = 1.5f))

        if (radius > 40.dp.toPx()) {
            val label = textMeasurer.measure(
                text = "${zone.regionName} (${zone.densityScore.toInt()}/km²)",
                style = TextStyle(color = Color(0xDDFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
            drawText(textLayoutResult = label, topLeft = Offset(center.x - label.size.width / 2f, center.y - label.size.height / 2f))
        }
    }
}

private fun DrawScope.drawBarriers(camera: MapCamera, barriers: List<BarrierFeature>, textMeasurer: TextMeasurer) {
    val showLabels = camera.zoom >= 6.5f
    for (barrier in barriers) {
        val barrierColor = getBarrierColor(barrier.type)
        val centerPt = project(barrier.location, camera, size)
        val path = Path()
        if (barrier.geometryPath.size >= 2) {
            barrier.geometryPath.forEachIndexed { index, geoPoint ->
                val pt = project(geoPoint, camera, size)
                if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
        } else {
            if (!centerPt.isNear(size, 80f)) continue
            path.moveTo(centerPt.x - 24.dp.toPx(), centerPt.y - 8.dp.toPx())
            path.lineTo(centerPt.x + 24.dp.toPx(), centerPt.y + 8.dp.toPx())
        }

        val strokeStyle = if (barrier.type == BarrierType.FENCE) {
            Stroke(width = 4f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
        } else {
            Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        }
        drawPath(path = path, color = barrierColor, style = strokeStyle)

        if (!showLabels || !centerPt.isNear(size, 100f)) continue
        val textResult = textMeasurer.measure(
            text = barrier.name,
            style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        )
        val badgeWidth = textResult.size.width + 12f
        val badgeHeight = textResult.size.height + 6f
        val badgeTopLeft = Offset(centerPt.x - badgeWidth / 2f, centerPt.y - badgeHeight / 2f)
        drawRoundRect(color = Color(0xEE1E2836), topLeft = badgeTopLeft, size = Size(badgeWidth, badgeHeight), cornerRadius = CornerRadius(4f))
        drawRoundRect(
            color = barrierColor,
            topLeft = badgeTopLeft,
            size = Size(badgeWidth, badgeHeight),
            cornerRadius = CornerRadius(4f),
            style = Stroke(width = 1f)
        )
        drawText(textLayoutResult = textResult, topLeft = Offset(centerPt.x - textResult.size.width / 2f, centerPt.y - textResult.size.height / 2f))
    }
}

private fun DrawScope.drawWildlife(camera: MapCamera, occurrences: List<WildlifeOccurrence>, textMeasurer: TextMeasurer) {
    val radius = 8.dp.toPx()
    for (wildlife in occurrences) {
        val pos = project(wildlife.location, camera, size)
        if (!pos.isNear(size, 20f)) continue
        val taxonColor = getTaxonColor(wildlife.taxonGroup)
        drawCircle(color = taxonColor.copy(alpha = 0.25f), radius = radius * 1.8f, center = pos)
        drawCircle(color = taxonColor, radius = radius, center = pos)
        drawCircle(color = Color.White, radius = radius, center = pos, style = Stroke(width = 2f))

        if (wildlife.observationCount > 1) {
            val countLayout = textMeasurer.measure(
                text = "${wildlife.observationCount}",
                style = TextStyle(color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            )
            drawText(
                textLayoutResult = countLayout,
                topLeft = Offset(pos.x - countLayout.size.width / 2f, pos.y - countLayout.size.height / 2f)
            )
        }
    }
}

private fun DrawScope.drawHotspots(camera: MapCamera, hotspots: List<CollisionHotspot>, textMeasurer: TextMeasurer) {
    val radius = 11.dp.toPx()
    for (hotspot in hotspots) {
        val pos = project(hotspot.location, camera, size)
        if (!pos.isNear(size, 20f)) continue
        val severityColor = getSeverityColor(hotspot.severity)
        drawCircle(color = severityColor, radius = radius, center = pos)
        drawCircle(color = Color.White, radius = radius, center = pos, style = Stroke(width = 2f))
        val incidentLayout = textMeasurer.measure(
            text = "${hotspot.incidentCount}",
            style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        )
        drawText(
            textLayoutResult = incidentLayout,
            topLeft = Offset(pos.x - incidentLayout.size.width / 2f, pos.y - incidentLayout.size.height / 2f)
        )
    }
}

/** Nearest feature to the tap, small point markers first so they stay clickable inside big blobs. */
private fun hitTest(
    tap: Offset,
    camera: MapCamera,
    size: Size,
    thresholdPx: Float,
    reports: List<CollisionReport>,
    crossings: List<WildlifeCrossing>,
    hotspots: List<CollisionHotspot>,
    wildlife: List<WildlifeOccurrence>,
    barriers: List<BarrierFeature>,
    zones: List<PopulationDensityZone>
): MapFeatureSelection? {
    var best: MapFeatureSelection? = null
    var bestDistance = thresholdPx
    fun consider(location: GeoLocation, selection: () -> MapFeatureSelection) {
        val distance = distanceBetween(tap, project(location, camera, size))
        if (distance < bestDistance) {
            bestDistance = distance
            best = selection()
        }
    }
    reports.forEach { r -> consider(r.location) { MapFeatureSelection.Collision(r) } }
    crossings.forEach { c -> consider(c.location) { MapFeatureSelection.Crossing(c) } }
    hotspots.forEach { h -> consider(h.location) { MapFeatureSelection.Hotspot(h) } }
    wildlife.forEach { w -> consider(w.location) { MapFeatureSelection.Wildlife(w) } }
    barriers.forEach { b -> consider(b.location) { MapFeatureSelection.Barrier(b) } }
    if (best == null) {
        zones.forEach { z -> consider(z.centerLocation) { MapFeatureSelection.Population(z) } }
    }
    return best
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
