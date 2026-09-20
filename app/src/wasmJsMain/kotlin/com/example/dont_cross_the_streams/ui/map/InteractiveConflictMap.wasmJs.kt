package com.example.dont_cross_the_streams.ui.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skia.Image as SkiaImage
import kotlin.js.ExperimentalJsExport
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalJsExport::class)
@JsFun("(lat, lon, zoom) => { if (window.syncWebMap) window.syncWebMap(lat, lon, zoom); }")
private external fun syncWebMapJs(lat: Double, lon: Double, zoom: Double)

@OptIn(ExperimentalJsExport::class)
@JsFun("""
(z, x, y) => {
    if (!window.wasmMapTiles) window.wasmMapTiles = {};
    const tileKey = z + '_' + x + '_' + y;
    if (window.wasmMapTiles[tileKey]) return;

    window.wasmMapTiles[tileKey] = 'LOADING';

    const urls = [
        'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/' + z + '/' + y + '/' + x,
        'https://tile.openstreetmap.org/' + z + '/' + x + '/' + y + '.png',
        'https://a.basemaps.cartocdn.com/rastertiles/voyager/' + z + '/' + x + '/' + y + '.png'
    ];

    function fetchUrl(index) {
        if (index >= urls.length) {
            window.wasmMapTiles[tileKey] = 'FAILED';
            return;
        }
        fetch(urls[index])
            .then(res => {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.arrayBuffer();
            })
            .then(buffer => {
                const bytes = new Uint8Array(buffer);
                let binary = '';
                const chunkSize = 8192;
                for (let i = 0; i < bytes.length; i += chunkSize) {
                    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunkSize));
                }
                window.wasmMapTiles[tileKey] = btoa(binary);
            })
            .catch(() => {
                fetchUrl(index + 1);
            });
    }

    fetchUrl(0);
}
""")
private external fun requestWasmTileJs(z: Int, x: Int, y: Int)

@OptIn(ExperimentalJsExport::class)
@JsFun("""
(tileKey) => {
    if (window.wasmMapTiles && window.wasmMapTiles[tileKey] && window.wasmMapTiles[tileKey] !== 'LOADING' && window.wasmMapTiles[tileKey] !== 'FAILED') {
        return window.wasmMapTiles[tileKey];
    }
    return null;
}
""")
private external fun getWasmTileBase64Js(tileKey: String): String?

private fun decodeBase64(input: String): ByteArray {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val table = IntArray(256) { -1 }
    for (i in chars.indices) table[chars[i].code] = i

    val cleanInput = input.replace("=", "").replace("\n", "").replace("\r", "")
    val len = cleanInput.length
    val outLen = (len * 6) / 8
    val out = ByteArray(outLen)

    var buffer = 0
    var bits = 0
    var outIdx = 0

    for (i in 0 until len) {
        val v = table[cleanInput[i].code]
        if (v >= 0) {
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out[outIdx++] = ((buffer shr bits) and 0xFF).toByte()
            }
        }
    }
    return out
}

private fun lonToWorldX(lon: Double, z: Int): Double {
    val numTiles = 1 shl z
    return ((lon + 180.0) / 360.0) * numTiles * 256.0
}

private fun latToWorldY(lat: Double, z: Int): Double {
    val numTiles = 1 shl z
    val rad = lat.coerceIn(-85.05112878, 85.05112878) * PI / 180.0
    val sinRad = sin(rad)
    val y = 0.5 - (ln((1.0 + sinRad) / (1.0 - sinRad)) / (4.0 * PI))
    return y * numTiles * 256.0
}

private fun geoToPixel(
    geo: GeoLocation,
    centerGeo: GeoLocation,
    zoomLevel: Float,
    panX: Float,
    panY: Float,
    canvasSize: Size
): Offset {
    val z = zoomLevel.roundToInt().coerceIn(2, 18)
    val scale = 2.0.pow((zoomLevel - z).toDouble()).toFloat()

    val centerWx = lonToWorldX(centerGeo.longitude, z)
    val centerWy = latToWorldY(centerGeo.latitude, z)

    val wx = lonToWorldX(geo.longitude, z)
    val wy = latToWorldY(geo.latitude, z)

    val x = (canvasSize.width / 2f) + panX + ((wx - centerWx) * scale).toFloat()
    val y = (canvasSize.height / 2f) + panY + ((wy - centerWy) * scale).toFloat()

    return Offset(x, y)
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
    // Synchronize Leaflet Web Map background with current map center & zoom
    LaunchedEffect(mapCenter.latitude, mapCenter.longitude, zoomLevel) {
        syncWebMapJs(mapCenter.latitude, mapCenter.longitude, zoomLevel.toDouble())
    }

    val textMeasurer = rememberTextMeasurer()

    // Cache for decoded raster map tile bitmaps
    val tileBitmapCache = remember { mutableStateMapOf<String, ImageBitmap>() }

    // Pulse animation for hotspot hazard pins
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

    // Background tile loading worker
    LaunchedEffect(mapCenter, zoomLevel, panOffsetX, panOffsetY, canvasSize) {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@LaunchedEffect

        val z = zoomLevel.roundToInt().coerceIn(2, 18)
        val scale = 2.0.pow((zoomLevel - z).toDouble()).toFloat()
        val centerWx = lonToWorldX(mapCenter.longitude, z)
        val centerWy = latToWorldY(mapCenter.latitude, z)

        val halfW = (canvasSize.width / 2f) / scale
        val halfH = (canvasSize.height / 2f) / scale
        val minWx = centerWx - halfW - panOffsetX / scale
        val maxWx = centerWx + halfW - panOffsetX / scale
        val minWy = centerWy - halfH - panOffsetY / scale
        val maxWy = centerWy + halfH - panOffsetY / scale

        val numTiles = 1 shl z
        val minTileX = (minWx / 256.0).toInt().coerceIn(0, numTiles - 1)
        val maxTileX = (maxWx / 256.0).toInt().coerceIn(0, numTiles - 1)
        val minTileY = (minWy / 256.0).toInt().coerceIn(0, numTiles - 1)
        val maxTileY = (maxWy / 256.0).toInt().coerceIn(0, numTiles - 1)

        // Poll for newly downloaded tile images in JS
        while (isActive) {
            var anyPending = false
            for (tx in minTileX..maxTileX) {
                for (ty in minTileY..maxTileY) {
                    val tileKey = "${z}_${tx}_${ty}"
                    if (!tileBitmapCache.containsKey(tileKey)) {
                        requestWasmTileJs(z, tx, ty)
                        val base64 = getWasmTileBase64Js(tileKey)
                        if (base64 != null) {
                            try {
                                val bytes = decodeBase64(base64)
                                val skiaImg = SkiaImage.makeFromEncoded(bytes)
                                val composeBmp = skiaImg.toComposeImageBitmap()
                                tileBitmapCache[tileKey] = composeBmp
                            } catch (e: Exception) {
                                // ignore corrupt tile
                            }
                        } else {
                            anyPending = true
                        }
                    }
                }
            }
            if (!anyPending) break
            delay(100)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(mapCenter, zoomLevel, panOffsetX, panOffsetY) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onPan(dragAmount.x, dragAmount.y)
                    }
                }
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

            // 1. Draw solid GIS map background base color
            drawRect(color = Color(0xFF161C26))

            // 2. Draw Raster Map Tiles (Esri World Street Map / OSM)
            val z = zoomLevel.roundToInt().coerceIn(2, 18)
            val scale = 2.0.pow((zoomLevel - z).toDouble()).toFloat()
            val centerWx = lonToWorldX(mapCenter.longitude, z)
            val centerWy = latToWorldY(mapCenter.latitude, z)

            val tileSizeOnCanvas = (256.0 * scale).toFloat()

            val halfW = (size.width / 2f) / scale
            val halfH = (size.height / 2f) / scale
            val minWx = centerWx - halfW - panOffsetX / scale
            val maxWx = centerWx + halfW - panOffsetX / scale
            val minWy = centerWy - halfH - panOffsetY / scale
            val maxWy = centerWy + halfH - panOffsetY / scale

            val numTiles = 1 shl z
            val minTileX = (minWx / 256.0).toInt().coerceIn(0, numTiles - 1)
            val maxTileX = (maxWx / 256.0).toInt().coerceIn(0, numTiles - 1)
            val minTileY = (minWy / 256.0).toInt().coerceIn(0, numTiles - 1)
            val maxTileY = (maxWy / 256.0).toInt().coerceIn(0, numTiles - 1)

            for (tx in minTileX..maxTileX) {
                for (ty in minTileY..maxTileY) {
                    val tileKey = "${z}_${tx}_${ty}"
                    val tileX = (size.width / 2f + panOffsetX) + ((tx * 256.0 - centerWx) * scale).toFloat()
                    val tileY = (size.height / 2f + panOffsetY) + ((ty * 256.0 - centerWy) * scale).toFloat()

                    val bitmap = tileBitmapCache[tileKey]
                    if (bitmap != null) {
                        drawImage(
                            image = bitmap,
                            dstOffset = IntOffset(tileX.roundToInt(), tileY.roundToInt()),
                            dstSize = IntSize(tileSizeOnCanvas.roundToInt(), tileSizeOnCanvas.roundToInt())
                        )
                    }
                }
            }

            // 3. Semi-transparent dark GIS overlay tint for high contrast UI overlays
            drawRect(color = Color(0x770D121B))

            // 4. Latitude & Longitude Coordinate Grid Lines & Tick Labels
            val gridStepPixels = 80f * (2.0f.pow((zoomLevel - 8f).coerceIn(-2f, 4f)))
            var xGrid = (size.width / 2f + panOffsetX) % gridStepPixels
            if (xGrid < 0) xGrid += gridStepPixels

            while (xGrid < size.width) {
                drawLine(
                    color = Color(0x334A608A),
                    start = Offset(xGrid, 0f),
                    end = Offset(xGrid, size.height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
                xGrid += gridStepPixels
            }

            var yGrid = (size.height / 2f + panOffsetY) % gridStepPixels
            if (yGrid < 0) yGrid += gridStepPixels

            while (yGrid < size.height) {
                drawLine(
                    color = Color(0x334A608A),
                    start = Offset(0f, yGrid),
                    end = Offset(size.width, yGrid),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
                yGrid += gridStepPixels
            }

            // 5. Geographic State Boundary Polygons & Outlines
            val stateBoundaries = listOf(
                // Missouri Outline
                listOf(
                    GeoLocation(40.60, -95.77), GeoLocation(40.60, -91.73), GeoLocation(40.38, -91.49),
                    GeoLocation(39.14, -90.65), GeoLocation(38.85, -90.12), GeoLocation(37.00, -89.15),
                    GeoLocation(36.00, -89.64), GeoLocation(36.00, -90.31), GeoLocation(36.50, -94.62),
                    GeoLocation(36.50, -94.62), GeoLocation(39.10, -94.61), GeoLocation(40.60, -95.77)
                ),
                // Illinois West Boundary
                listOf(
                    GeoLocation(42.50, -90.64), GeoLocation(41.50, -90.50), GeoLocation(39.70, -91.35),
                    GeoLocation(38.81, -90.12), GeoLocation(37.00, -89.15)
                )
            )

            for (boundary in stateBoundaries) {
                val path = Path()
                var isFirst = true
                for (pt in boundary) {
                    val p = geoToPixel(pt, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                    if (isFirst) {
                        path.moveTo(p.x, p.y)
                        isFirst = false
                    } else {
                        path.lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(0x6680CBC4),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }

            // 6. Major Cities Vector Markers & Name Labels
            val cities = listOf(
                Pair(GeoLocation(39.10, -94.58), "Kansas City"),
                Pair(GeoLocation(38.63, -90.20), "St. Louis"),
                Pair(GeoLocation(37.21, -93.29), "Springfield"),
                Pair(GeoLocation(38.95, -92.33), "Columbia"),
                Pair(GeoLocation(38.58, -92.17), "Jefferson City"),
                Pair(GeoLocation(37.31, -89.52), "Cape Girardeau")
            )

            for ((cityLoc, cityName) in cities) {
                val cityPt = geoToPixel(cityLoc, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                drawCircle(color = Color(0xFF4FC3F7), radius = 5f, center = cityPt)
                drawCircle(color = Color.White, radius = 5f, center = cityPt, style = Stroke(width = 1.5f))

                val cityText = textMeasurer.measure(
                    text = cityName,
                    style = TextStyle(color = Color(0xFFE0F7FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = cityText,
                    topLeft = Offset(cityPt.x + 8f, cityPt.y - 8f)
                )
            }

            // 7. Topological Vector Rivers (Missouri & Mississippi River Networks)
            val missouriRiverGeo = listOf(
                GeoLocation(39.10, -94.60), // Kansas City
                GeoLocation(38.95, -92.33), // Columbia
                GeoLocation(38.57, -92.17), // Jefferson City
                GeoLocation(38.80, -90.70), // St. Charles
                GeoLocation(38.81, -90.12)  // St. Louis Confluence
            )
            val missouriPath = Path()
            var isFirstPt = true
            for (geoPt in missouriRiverGeo) {
                val pt = geoToPixel(geoPt, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                if (isFirstPt) {
                    missouriPath.moveTo(pt.x, pt.y)
                    isFirstPt = false
                } else {
                    missouriPath.lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = missouriPath,
                color = Color(0xB329B6F6),
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val mississippiRiverGeo = listOf(
                GeoLocation(41.50, -90.50), // Quad Cities
                GeoLocation(39.70, -91.35), // Hannibal
                GeoLocation(38.81, -90.12), // St. Louis Confluence
                GeoLocation(37.70, -89.80), // Ste. Genevieve
                GeoLocation(37.50, -89.40), // LaRue Swamp / Cape Girardeau
                GeoLocation(36.00, -89.60)  // Bootheel / Memphis
            )
            val mississippiPath = Path()
            isFirstPt = true
            for (geoPt in mississippiRiverGeo) {
                val pt = geoToPixel(geoPt, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
                if (isFirstPt) {
                    mississippiPath.moveTo(pt.x, pt.y)
                    isFirstPt = false
                } else {
                    mississippiPath.lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = mississippiPath,
                color = Color(0xCC0288D1),
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 8. Topological Ozark Highlands Contour Curves
            val ozarkCenterGeo = GeoLocation(37.80, -92.50)
            val ozarkPixel = geoToPixel(ozarkCenterGeo, mapCenter, zoomLevel, panOffsetX, panOffsetY, size)
            val contourRadius = 220f * (2.0f.pow((zoomLevel - 8f).coerceIn(-1f, 3f)))
            drawCircle(
                color = Color(0x3381C784),
                radius = contourRadius,
                center = ozarkPixel,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
            drawCircle(
                color = Color(0x2281C784),
                radius = contourRadius * 0.65f,
                center = ozarkPixel,
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )

            // Geographic Regional Label
            val ozarkLabel = textMeasurer.measure(
                text = "OZARK PLATEAU HIGHLANDS",
                style = TextStyle(color = Color(0x99A5D6A7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            drawText(
                textLayoutResult = ozarkLabel,
                topLeft = Offset(ozarkPixel.x - ozarkLabel.size.width / 2f, ozarkPixel.y - 12f)
            )

            // 9. Render Population Density Zones (Translucent glowing heatmaps)
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

            // 10. Render Barrier Features (Highways, Railways, Dams, Fences, Canals)
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

            // 11. Render Wildlife Occurrences (Species pins)
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

            // 12. Render Collision Hotspots (Hazard pins with pulsing rings)
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

            // 13. Selected feature highlight ring
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

            // Scale & Map Attribution Text
            val attrText = textMeasurer.measure(
                text = "© Esri World Street Map | OpenStreetMap | CARTO | Kotlin/Wasm GIS Engine",
                style = TextStyle(color = Color(0xEEFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Medium)
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

        // Overlay: Map Control Buttons (Pan, Zoom, Recenter)
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
                    onClick = { onPanDirection(1.0, 0.0) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Pan North"
                    )
                }
                Row {
                    IconButton(
                        onClick = { onPanDirection(0.0, -1.0) },
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
                        onClick = { onPanDirection(0.0, 1.0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Pan East"
                        )
                    }
                }
                IconButton(
                    onClick = { onPanDirection(-1.0, 0.0) },
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
                    text = "${zoomLevel.roundToInt()}x",
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
