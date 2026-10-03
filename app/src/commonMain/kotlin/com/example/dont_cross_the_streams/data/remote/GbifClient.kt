package com.example.dont_cross_the_streams.data.remote

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

const val GBIF_SOURCE = "GBIF"

/**
 * GBIF occurrence search (https://techdocs.gbif.org/en/openapi/v1/occurrence). No key needed.
 * Records that GBIF republishes from iNaturalist are dropped because iNaturalist is queried directly.
 */
class GbifClient(private val http: HttpGet = ::httpGetText) {

    suspend fun occurrences(bounds: BoundingBox, limit: Int = 300): List<WildlifeOccurrence>? {
        val results = parseJsonOrNull(http(searchUrl(bounds, limit))).obj()?.a("results") ?: return null
        return results.mapNotNull { element ->
            val o = element.obj() ?: return@mapNotNull null
            if (o.str("occurrenceID")?.contains("inaturalist.org") == true) return@mapNotNull null
            val key = o.str("key") ?: return@mapNotNull null
            val lat = o.num("decimalLatitude") ?: return@mapNotNull null
            val lon = o.num("decimalLongitude") ?: return@mapNotNull null
            WildlifeOccurrence(
                id = "gbif_$key",
                species = o.str("species") ?: o.str("scientificName") ?: "Unidentified",
                commonName = o.str("vernacularName"),
                location = GeoLocation(lat, lon),
                taxonGroup = iconicTaxonToGroup(o.str("class")),
                observationCount = o.num("individualCount")?.toInt()?.coerceAtLeast(1) ?: 1,
                timestamp = 0L,
                source = listOfNotNull(GBIF_SOURCE, o.str("datasetName")).joinToString(" / "),
                observedOn = o.str("eventDate"),
                recordUrl = "https://www.gbif.org/occurrence/$key"
            )
        }
    }

    suspend fun count(bounds: BoundingBox): Int? =
        parseJsonOrNull(http(searchUrl(bounds, 0))).obj()?.num("count")?.toInt()

    companion object {
        // GBIF backbone class keys: Mammalia 359, Aves 212, Reptilia 358, Amphibia 131.
        private const val VERTEBRATE_TAXA = "&taxonKey=359&taxonKey=212&taxonKey=358&taxonKey=131"

        fun searchUrl(bounds: BoundingBox, limit: Int): String =
            "https://api.gbif.org/v1/occurrence/search?hasCoordinate=true&hasGeospatialIssue=false" +
                "&occurrenceStatus=PRESENT$VERTEBRATE_TAXA" +
                "&decimalLatitude=${bounds.minLat},${bounds.maxLat}" +
                "&decimalLongitude=${bounds.minLon},${bounds.maxLon}" +
                "&limit=${limit.coerceIn(0, 300)}"
    }
}
