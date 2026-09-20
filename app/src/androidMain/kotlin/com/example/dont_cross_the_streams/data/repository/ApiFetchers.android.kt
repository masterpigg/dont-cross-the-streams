package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.remote.NetworkClient
import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

actual suspend fun fetchLiveOverpassBarriersApi(bboxQuery: String): List<BarrierFeature> {
    return try {
        val defaultQuery = if (bboxQuery.isBlank()) {
            "[out:json][timeout:25];(way[\"highway\"=\"motorway\"](39.5,-106.0,39.7,-105.5););out body geom;"
        } else bboxQuery

        val response = NetworkClient.overpassApiService.queryOverpass(defaultQuery)
        response.elements?.mapNotNull { elem ->
            val geom = elem.geometry?.map { GeoLocation(it.lat, it.lon) } ?: emptyList()
            val center = if (geom.isNotEmpty()) {
                GeoLocation(
                    latitude = geom.map { it.latitude }.average(),
                    longitude = geom.map { it.longitude }.average()
                )
            } else if (elem.lat != null && elem.lon != null) {
                GeoLocation(elem.lat, elem.lon)
            } else null

            if (center != null) {
                val highwayType = elem.tags?.get("highway")
                val railwayType = elem.tags?.get("railway")
                val waterwayType = elem.tags?.get("waterway")
                val barrierType = elem.tags?.get("barrier")

                val type = when {
                    highwayType != null -> BarrierType.HIGHWAY
                    railwayType != null -> BarrierType.RAILWAY
                    waterwayType == "dam" -> BarrierType.DAM
                    barrierType != null -> BarrierType.FENCE
                    else -> BarrierType.HIGHWAY
                }

                BarrierFeature(
                    id = "osm_${elem.id ?: System.nanoTime()}",
                    type = type,
                    name = elem.tags?.get("name") ?: elem.tags?.get("ref") ?: "OSM Linear Barrier",
                    location = center,
                    geometryPath = geom,
                    impactLevel = ImpactLevel.HIGH,
                    source = "OpenStreetMap Overpass API"
                )
            } else null
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

actual suspend fun fetchLiveOccurrencesFromGbifApi(
    scientificName: String?,
    latitude: Double?,
    longitude: Double?
): List<WildlifeOccurrence> {
    return try {
        val response = NetworkClient.gbifApiService.searchOccurrences(
            scientificName = scientificName,
            latitude = latitude,
            longitude = longitude,
            limit = 30
        )
        response.results?.mapNotNull { item ->
            val lat = item.decimalLatitude
            val lon = item.decimalLongitude
            if (lat != null && lon != null) {
                WildlifeOccurrence(
                    id = "gbif_${item.key ?: System.nanoTime()}",
                    species = item.scientificName ?: scientificName ?: "Unknown Species",
                    commonName = item.vernacularName,
                    location = GeoLocation(lat, lon),
                    taxonGroup = item.classGroup ?: "Fauna",
                    observationCount = item.individualCount ?: 1,
                    timestamp = System.currentTimeMillis(),
                    source = "GBIF API"
                )
            } else null
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

actual suspend fun fetchLiveObservationsFromINaturalistApi(
    latitude: Double?,
    longitude: Double?,
    radiusKm: Int
): List<WildlifeOccurrence> {
    return try {
        val response = NetworkClient.iNaturalistApiService.getObservations(
            lat = latitude,
            lng = longitude,
            radiusKm = radiusKm,
            perPage = 30
        )
        response.results?.mapNotNull { obs ->
            val locParts = obs.locationStr?.split(",")
            val lat = locParts?.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lon = locParts?.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (lat != null && lon != null) {
                val imageUrl = obs.photos?.firstOrNull()?.url?.replace("square", "medium")
                WildlifeOccurrence(
                    id = "inat_${obs.id ?: System.nanoTime()}",
                    species = obs.taxon?.name ?: obs.speciesGuess ?: "Unidentified Species",
                    commonName = obs.taxon?.preferredCommonName ?: obs.speciesGuess,
                    location = GeoLocation(lat, lon),
                    taxonGroup = obs.taxon?.iconicTaxonName ?: "Fauna",
                    observationCount = 1,
                    timestamp = System.currentTimeMillis(),
                    source = "iNaturalist API",
                    imageUrl = imageUrl
                )
            } else null
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
