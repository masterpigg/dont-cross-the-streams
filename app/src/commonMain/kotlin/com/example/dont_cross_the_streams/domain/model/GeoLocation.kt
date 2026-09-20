package com.example.dont_cross_the_streams.domain.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoLocation(
    val latitude: Double,
    val longitude: Double
) {
    fun distanceToKm(other: GeoLocation): Double {
        val lat1Rad = latitude * (PI / 180.0)
        val lat2Rad = other.latitude * (PI / 180.0)
        val deltaLatRad = (other.latitude - latitude) * (PI / 180.0)
        val deltaLonRad = (other.longitude - longitude) * (PI / 180.0)

        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLonRad / 2) * sin(deltaLonRad / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val earthRadiusKm = 6371.0
        return earthRadiusKm * c
    }
}

data class BoundingBox(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double
) {
    fun contains(location: GeoLocation): Boolean {
        return location.latitude in minLat..maxLat &&
                location.longitude in minLon..maxLon
    }
}
