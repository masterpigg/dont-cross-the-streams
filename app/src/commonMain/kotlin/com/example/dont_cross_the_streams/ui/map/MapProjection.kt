package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh

/**
 * Spherical Web Mercator in "world pixels": at fractional zoom z the world is 256 * 2^z pixels wide,
 * matching the slippy-map tile grid. All map math goes through here so dragging, wheel-zoom, tile
 * placement and hit-testing can never disagree with each other.
 */
object WebMercator {
    const val TILE_SIZE = 256.0
    const val MAX_LATITUDE = 85.05112878
    const val MIN_ZOOM = 2f
    const val MAX_ZOOM = 18f

    fun worldSize(zoom: Double): Double = TILE_SIZE * 2.0.pow(zoom)

    fun lonToX(lon: Double, zoom: Double): Double = (lon + 180.0) / 360.0 * worldSize(zoom)

    fun latToY(lat: Double, zoom: Double): Double {
        val sinLat = sin(lat.coerceIn(-MAX_LATITUDE, MAX_LATITUDE) * PI / 180.0)
        val y = 0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * PI)
        return y * worldSize(zoom)
    }

    fun xToLon(x: Double, zoom: Double): Double = x / worldSize(zoom) * 360.0 - 180.0

    fun yToLat(y: Double, zoom: Double): Double {
        val n = PI * (1.0 - 2.0 * y / worldSize(zoom))
        return atan(sinh(n)) * 180.0 / PI
    }

    /** Clamps a center so the map can't be dragged past the poles; longitude wraps instead of sticking. */
    fun normalize(location: GeoLocation): GeoLocation {
        var lon = location.longitude
        while (lon > 180.0) lon -= 360.0
        while (lon < -180.0) lon += 360.0
        return GeoLocation(location.latitude.coerceIn(-MAX_LATITUDE, MAX_LATITUDE), lon)
    }

    /** Center after moving the view by a screen-space delta (positive dx = content moves right). */
    fun panBy(center: GeoLocation, zoom: Double, dx: Double, dy: Double): GeoLocation {
        val x = lonToX(center.longitude, zoom) - dx
        val y = (latToY(center.latitude, zoom) - dy).coerceIn(0.0, worldSize(zoom))
        return normalize(GeoLocation(yToLat(y, zoom), xToLon(x, zoom)))
    }

    /**
     * New center that keeps the geographic point under [focusX],[focusY] (relative to the view
     * center) fixed on screen while zooming from [fromZoom] to [toZoom].
     */
    fun zoomAround(
        center: GeoLocation,
        fromZoom: Double,
        toZoom: Double,
        focusX: Double,
        focusY: Double
    ): GeoLocation {
        val focusLon = xToLon(lonToX(center.longitude, fromZoom) + focusX, fromZoom)
        val focusLat = yToLat(latToY(center.latitude, fromZoom) + focusY, fromZoom)
        val newX = lonToX(focusLon, toZoom) - focusX
        val newY = latToY(focusLat, toZoom) - focusY
        return normalize(GeoLocation(yToLat(newY, toZoom), xToLon(newX, toZoom)))
    }

    fun visibleBounds(center: GeoLocation, zoom: Double, widthPx: Double, heightPx: Double): BoundingBox {
        val cx = lonToX(center.longitude, zoom)
        val cy = latToY(center.latitude, zoom)
        val world = worldSize(zoom)
        val halfW = widthPx / 2.0
        return BoundingBox(
            minLat = yToLat((cy + heightPx / 2.0).coerceAtMost(world), zoom),
            minLon = if (widthPx >= world) -180.0 else xToLon(cx - halfW, zoom).coerceAtLeast(-180.0),
            maxLat = yToLat((cy - heightPx / 2.0).coerceAtLeast(0.0), zoom),
            maxLon = if (widthPx >= world) 180.0 else xToLon(cx + halfW, zoom).coerceAtMost(180.0)
        )
    }
}
