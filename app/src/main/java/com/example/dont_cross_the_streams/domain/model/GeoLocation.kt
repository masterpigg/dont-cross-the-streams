package com.example.dont_cross_the_streams.domain.model

import java.io.Serializable

data class GeoLocation(
    val latitude: Double,
    val longitude: Double
) : Serializable {
    fun distanceToKm(other: GeoLocation): Double {
        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(other.latitude)
        val deltaLatRad = Math.toRadians(other.latitude - latitude)
        val deltaLonRad = Math.toRadians(other.longitude - longitude)

        val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val earthRadiusKm = 6371.0
        return earthRadiusKm * c
    }
}

data class BoundingBox(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double
) : Serializable {
    fun contains(location: GeoLocation): Boolean {
        return location.latitude in minLat..maxLat &&
                location.longitude in minLon..maxLon
    }
}
