package com.example.dont_cross_the_streams.data.remote

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

const val INATURALIST_DEAD_SOURCE = "iNaturalist (Dead-animal annotations)"
const val INATURALIST_OBSERVATION_SOURCE = "iNaturalist (Research-grade observations)"

/**
 * iNaturalist API v1 (https://api.inaturalist.org/v1/docs/). No key needed; CORS enabled.
 * Dead-animal reports use the "Alive or Dead" annotation (term 17) = "Dead" (value 19).
 */
class INaturalistClient(private val http: HttpGet = ::httpGetText) {

    suspend fun deadAnimalReports(bounds: BoundingBox, pages: Int = 3): List<CollisionReport>? =
        paged(pages) { page -> deadReportsUrl(bounds, PER_PAGE, page) }?.mapNotNull(::toCollisionReport)

    suspend fun observations(bounds: BoundingBox, pages: Int = 2): List<WildlifeOccurrence>? =
        paged(pages) { page -> observationsUrl(bounds, PER_PAGE, page) }?.mapNotNull(::toOccurrence)

    suspend fun countDeadAnimalReports(bounds: BoundingBox): Int? = count(deadReportsUrl(bounds, 0, 1))

    suspend fun countObservations(bounds: BoundingBox): Int? = count(observationsUrl(bounds, 0, 1))

    private suspend fun count(url: String): Int? =
        parseJsonOrNull(http(url)).obj()?.num("total_results")?.toInt()

    /** Fetches up to [pages] pages; null only if the first page fails. */
    private suspend fun paged(pages: Int, url: (Int) -> String): List<kotlinx.serialization.json.JsonObject>? {
        val all = mutableListOf<kotlinx.serialization.json.JsonObject>()
        for (page in 1..pages) {
            val results = parseJsonOrNull(http(url(page))).obj()?.a("results")
            if (results == null) return if (page == 1) null else all
            results.mapNotNullTo(all) { it.obj() }
            if (results.size < PER_PAGE) break
        }
        return all
    }

    private fun toCollisionReport(o: kotlinx.serialization.json.JsonObject): CollisionReport? {
        val id = o.str("id") ?: return null
        val location = o.location() ?: return null
        val taxon = o.o("taxon")
        return CollisionReport(
            id = "inat_dead_$id",
            species = taxon?.str("name") ?: "Unidentified",
            commonName = taxon?.str("preferred_common_name") ?: o.str("species_guess"),
            taxonGroup = iconicTaxonToGroup(taxon?.str("iconic_taxon_name")),
            location = location,
            observedOn = o.str("observed_on"),
            source = INATURALIST_DEAD_SOURCE,
            observationUrl = o.str("uri") ?: "https://www.inaturalist.org/observations/$id"
        )
    }

    private fun toOccurrence(o: kotlinx.serialization.json.JsonObject): WildlifeOccurrence? {
        val id = o.str("id") ?: return null
        val location = o.location() ?: return null
        val taxon = o.o("taxon")
        return WildlifeOccurrence(
            id = "inat_$id",
            species = taxon?.str("name") ?: "Unidentified",
            commonName = taxon?.str("preferred_common_name") ?: o.str("species_guess"),
            location = location,
            taxonGroup = iconicTaxonToGroup(taxon?.str("iconic_taxon_name")),
            observationCount = 1,
            timestamp = 0L,
            source = INATURALIST_OBSERVATION_SOURCE,
            imageUrl = o.a("photos")?.firstOrNull().obj()?.str("url")?.replace("square", "medium"),
            observedOn = o.str("observed_on"),
            recordUrl = o.str("uri") ?: "https://www.inaturalist.org/observations/$id"
        )
    }

    private fun kotlinx.serialization.json.JsonObject.location(): GeoLocation? {
        val coords = o("geojson")?.a("coordinates") ?: return null
        val lon = coords.getOrNull(0)?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content?.toDoubleOrNull() }
        val lat = coords.getOrNull(1)?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content?.toDoubleOrNull() }
        return if (lat != null && lon != null) GeoLocation(lat, lon) else null
    }

    companion object {
        const val PER_PAGE = 200
        private const val VERTEBRATES = "Mammalia,Reptilia,Amphibia,Aves"

        fun deadReportsUrl(bounds: BoundingBox, perPage: Int, page: Int): String =
            "https://api.inaturalist.org/v1/observations?term_id=17&term_value_id=19" +
                "&iconic_taxa=$VERTEBRATES&geo=true&order_by=observed_on&order=desc" +
                bounds.inatParams() + "&per_page=${perPage.coerceIn(0, 200)}&page=$page"

        /** Research-grade sightings; the repository drops any that are also in the dead-animal set. */
        fun observationsUrl(bounds: BoundingBox, perPage: Int, page: Int): String =
            "https://api.inaturalist.org/v1/observations?quality_grade=research" +
                "&iconic_taxa=$VERTEBRATES&geo=true&order_by=observed_on&order=desc" +
                bounds.inatParams() + "&per_page=${perPage.coerceIn(0, 200)}&page=$page"

        private fun BoundingBox.inatParams() =
            "&swlat=$minLat&swlng=$minLon&nelat=$maxLat&nelng=$maxLon"
    }
}
