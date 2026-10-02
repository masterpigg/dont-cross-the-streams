package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.ui.map.WebMercator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebMercatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 1e-6) {
        assertTrue(abs(expected - actual) <= tolerance, "expected $expected but was $actual")
    }

    @Test
    fun projection_roundTripsCoordinates() {
        val zoom = 7.3
        for (location in listOf(GeoLocation(38.6, -90.2), GeoLocation(-33.9, 151.2), GeoLocation(0.0, 0.0))) {
            assertClose(location.longitude, WebMercator.xToLon(WebMercator.lonToX(location.longitude, zoom), zoom))
            assertClose(location.latitude, WebMercator.yToLat(WebMercator.latToY(location.latitude, zoom), zoom))
        }
    }

    @Test
    fun worldAtZoomZero_isOneTile() {
        assertClose(256.0, WebMercator.worldSize(0.0))
        assertClose(128.0, WebMercator.lonToX(0.0, 0.0))
        assertClose(128.0, WebMercator.latToY(0.0, 0.0))
    }

    @Test
    fun panBy_movesTheMapWithTheFinger() {
        val center = GeoLocation(39.0, -94.0)
        val zoom = 8.0
        // Dragging content right means the camera looks further west, by exactly that many pixels.
        val panned = WebMercator.panBy(center, zoom, dx = 100.0, dy = 0.0)
        assertTrue(panned.longitude < center.longitude)
        assertClose(
            WebMercator.lonToX(center.longitude, zoom) - 100.0,
            WebMercator.lonToX(panned.longitude, zoom),
            1e-6
        )
        assertClose(center.latitude, panned.latitude, 1e-9)

        // Dragging content down means the camera looks further north.
        assertTrue(WebMercator.panBy(center, zoom, dx = 0.0, dy = 100.0).latitude > center.latitude)
    }

    @Test
    fun panBy_wrapsLongitudeInsteadOfSticking() {
        val nearDateLine = GeoLocation(10.0, 179.9)
        val panned = WebMercator.panBy(nearDateLine, 6.0, dx = -500.0, dy = 0.0)
        assertTrue(panned.longitude in -180.0..-160.0, "wrapped to ${panned.longitude}")
    }

    @Test
    fun zoomAround_keepsThePointUnderTheCursorFixed() {
        val center = GeoLocation(38.6, -90.2)
        val focusX = 220.0
        val focusY = -140.0
        val fromZoom = 6.0
        val toZoom = 7.5

        fun geoUnderFocus(c: GeoLocation, z: Double) = GeoLocation(
            WebMercator.yToLat(WebMercator.latToY(c.latitude, z) + focusY, z),
            WebMercator.xToLon(WebMercator.lonToX(c.longitude, z) + focusX, z)
        )

        val before = geoUnderFocus(center, fromZoom)
        val newCenter = WebMercator.zoomAround(center, fromZoom, toZoom, focusX, focusY)
        val after = geoUnderFocus(newCenter, toZoom)
        assertClose(before.latitude, after.latitude, 1e-9)
        assertClose(before.longitude, after.longitude, 1e-9)
    }

    @Test
    fun visibleBounds_containsCenterAndShrinksWhenZoomingIn() {
        val center = GeoLocation(38.6, -90.2)
        val wide = WebMercator.visibleBounds(center, 5.0, 1600.0, 1000.0)
        val narrow = WebMercator.visibleBounds(center, 9.0, 1600.0, 1000.0)
        assertTrue(wide.contains(center))
        assertTrue(narrow.contains(center))
        assertTrue((narrow.maxLon - narrow.minLon) < (wide.maxLon - wide.minLon))
        assertClose(16.0, (wide.maxLon - wide.minLon) / (narrow.maxLon - narrow.minLon), 1e-6)
    }

    @Test
    fun visibleBounds_coversWholeWorldWhenZoomedFarOut() {
        val bounds = WebMercator.visibleBounds(GeoLocation(0.0, 0.0), 0.0, 1600.0, 1000.0)
        assertEquals(-180.0, bounds.minLon)
        assertEquals(180.0, bounds.maxLon)
    }
}
