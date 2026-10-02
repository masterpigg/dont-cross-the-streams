package com.example.dont_cross_the_streams.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import org.jetbrains.skia.Image as SkiaImage

// ---------------------------------------------------------------------------------------------
// Raster tile loading (JS side)
//
// Tiles are fetched in JS and handed to Wasm as base64 so Skia can decode them onto the Compose
// canvas. The loader caps concurrent requests, serves the newest requests first (the current view
// rather than tiles the user already zoomed past), remembers which tile server last worked, and
// lets failed tiles be retried later instead of blacklisting them forever.
// ---------------------------------------------------------------------------------------------

@JsFun("""
() => {
    if (window.__dcsTiles) return;
    const sources = [
        (z, x, y) => 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/' + z + '/' + y + '/' + x,
        (z, x, y) => 'https://a.basemaps.cartocdn.com/rastertiles/voyager/' + z + '/' + x + '/' + y + '.png',
        (z, x, y) => 'https://tile.openstreetmap.org/' + z + '/' + x + '/' + y + '.png'
    ];
    const toBase64 = (buffer) => {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.length; i += 8192) {
            binary += String.fromCharCode.apply(null, bytes.subarray(i, i + 8192));
        }
        return btoa(binary);
    };
    const T = { entries: new Map(), queue: [], active: 0, maxActive: 8, preferred: 0 };
    const finish = (key, value) => {
        T.active--;
        if (T.entries.has(key)) T.entries.set(key, value);
        T.pump();
    };
    const attempt = (job, index, tries) => {
        if (tries >= sources.length) {
            finish(job.key, { state: 'failed', at: Date.now() });
            return;
        }
        fetch(sources[index](job.z, job.x, job.y))
            .then(res => { if (!res.ok) throw new Error('HTTP ' + res.status); return res.arrayBuffer(); })
            .then(buffer => {
                T.preferred = index;
                finish(job.key, { state: 'ready', data: toBase64(buffer) });
            })
            .catch(() => attempt(job, (index + 1) % sources.length, tries + 1));
    };
    T.pump = () => {
        while (T.active < T.maxActive && T.queue.length > 0) {
            const job = T.queue.pop();
            const entry = T.entries.get(job.key);
            if (!entry || entry.state !== 'queued') continue;
            entry.state = 'loading';
            T.active++;
            attempt(job, T.preferred, 0);
        }
    };
    T.request = (z, x, y) => {
        const key = z + '/' + x + '/' + y;
        const entry = T.entries.get(key);
        if (entry) {
            if (entry.state !== 'failed' || Date.now() - entry.at < 30000) return;
            T.entries.delete(key);
        }
        T.entries.set(key, { state: 'queued' });
        T.queue.push({ key: key, z: z, x: x, y: y });
        T.pump();
    };
    T.prune = (z) => {
        T.queue = T.queue.filter(job => {
            if (job.z === z) return true;
            const entry = T.entries.get(job.key);
            if (entry && entry.state === 'queued') T.entries.delete(job.key);
            return false;
        });
    };
    T.take = (key) => {
        const entry = T.entries.get(key);
        if (!entry) return '';
        if (entry.state === 'ready') {
            T.entries.delete(key);
            return entry.data;
        }
        return entry.state === 'failed' ? '!' : '';
    };
    window.__dcsTiles = T;
}
""")
private external fun installTileLoaderJs()

@JsFun("(z, x, y) => window.__dcsTiles.request(z, x, y)")
private external fun requestTileJs(z: Int, x: Int, y: Int)

/** Drops queued (not yet started) requests for zoom levels other than [z]. */
@JsFun("(z) => window.__dcsTiles.prune(z)")
private external fun pruneTileQueueJs(z: Int)

/** Base64 PNG/JPEG when ready, "!" when every server failed, "" while still loading. */
@JsFun("(key) => window.__dcsTiles.take(key)")
private external fun takeTileJs(key: String): String

/** WheelEvent.deltaMode: 0 = pixels, 1 = lines, 2 = pages. */
@JsFun("(e) => (e && typeof e.deltaMode === 'number') ? e.deltaMode : 0")
private external fun wheelDeltaModeJs(event: JsAny?): Int

private val BASE64_TABLE = IntArray(256) { -1 }.also { table ->
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".forEachIndexed { i, c ->
        table[c.code] = i
    }
}

private fun decodeBase64(input: String): ByteArray {
    var len = input.length
    while (len > 0 && input[len - 1] == '=') len--
    val out = ByteArray(len * 6 / 8)
    var buffer = 0
    var bits = 0
    var outIdx = 0
    for (i in 0 until len) {
        val v = BASE64_TABLE[input[i].code and 0xFF]
        if (v < 0) continue
        buffer = (buffer shl 6) or v
        bits += 6
        if (bits >= 8) {
            bits -= 8
            out[outIdx++] = ((buffer shr bits) and 0xFF).toByte()
        }
    }
    return if (outIdx == out.size) out else out.copyOf(outIdx)
}

/** Browser tile source backed by the JS loader above; decodes with Skia for the Compose canvas. */
private object BrowserTileLoader : MapTileLoader {
    private var installed = false

    override fun load(z: Int, x: Int, y: Int): TileLoadResult {
        if (!installed) {
            installTileLoaderJs()
            installed = true
        }
        requestTileJs(z, x, y)
        val payload = takeTileJs("$z/$x/$y")
        return when {
            payload.isEmpty() -> TileLoadResult.Pending
            payload == "!" -> TileLoadResult.Failed
            else -> try {
                TileLoadResult.Ready(SkiaImage.makeFromEncoded(decodeBase64(payload)).toComposeImageBitmap())
            } catch (_: Exception) {
                TileLoadResult.Failed
            }
        }
    }

    override fun retainOnly(z: Int) {
        if (installed) pruneTileQueueJs(z)
    }
}

private fun browserWheelDeltaUnitPx(pointerEvent: PointerEvent): Float {
    // Compose hands us the browser WheelEvent as Any?; the JS side tolerates anything else.
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val event = pointerEvent.nativeEvent as? JsAny
    return when (wheelDeltaModeJs(event)) {
        1 -> 40f // lines (Firefox)
        2 -> 800f // pages
        else -> 1f
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
    onMapCenterAndZoomChanged: (GeoLocation, Float) -> Unit,
    wildlifeCrossings: List<WildlifeCrossing>,
    collisionReports: List<CollisionReport>
) {
    CanvasConflictMap(
        mapCenter = mapCenter,
        zoomLevel = zoomLevel,
        wildlifeOccurrences = wildlifeOccurrences,
        collisionHotspots = collisionHotspots,
        barriers = barriers,
        populationZones = populationZones,
        wildlifeCrossings = wildlifeCrossings,
        collisionReports = collisionReports,
        selectedFeature = selectedFeature,
        onResetView = onResetView,
        onFeatureSelected = onFeatureSelected,
        onSelectPreset = onSelectPreset,
        onMapCenterAndZoomChanged = onMapCenterAndZoomChanged,
        modifier = modifier,
        tileLoader = BrowserTileLoader,
        attribution = "© Esri | © OpenStreetMap contributors | © CARTO",
        wheelDeltaUnitPx = ::browserWheelDeltaUnitPx
    )
}
