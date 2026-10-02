package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.repository.CollisionReportRepository
import kotlinx.coroutines.withTimeoutOrNull

class CollisionReportRepositoryImpl : CollisionReportRepository {
    override suspend fun getCollisionReports(bounds: BoundingBox): List<CollisionReport>? {
        return withTimeoutOrNull(8000L) {
            fetchLiveRoadkillReportsApi(bounds, maxResults = MAX_RESULTS)
        }
    }

    companion object {
        const val MAX_RESULTS = 200
    }
}

/**
 * iNaturalist observations annotated "Alive or Dead" (term 17) = "Dead" (value 19), restricted to
 * the vertebrate groups that dominate road mortality. Shared so every platform asks the same question.
 */
fun buildINaturalistRoadkillUrl(bounds: BoundingBox, perPage: Int): String {
    return "https://api.inaturalist.org/v1/observations" +
        "?term_id=17&term_value_id=19" +
        "&iconic_taxa=Mammalia,Reptilia,Amphibia,Aves" +
        "&geo=true&order_by=observed_on&order=desc" +
        "&swlat=${bounds.minLat}&swlng=${bounds.minLon}" +
        "&nelat=${bounds.maxLat}&nelng=${bounds.maxLon}" +
        "&per_page=${perPage.coerceIn(1, 200)}"
}

const val INATURALIST_ROADKILL_SOURCE = "iNaturalist (Dead-animal annotations)"

/** Parses the tab-separated rows produced by the platform fetchers: id, lat, lng, sci, common, group, date, url. */
fun parseRoadkillRows(rows: String): List<CollisionReport> {
    return rows.lineSequence().mapNotNull { line ->
        val f = line.split('\t')
        if (f.size < 8) return@mapNotNull null
        val lat = f[1].toDoubleOrNull() ?: return@mapNotNull null
        val lon = f[2].toDoubleOrNull() ?: return@mapNotNull null
        CollisionReport(
            id = "inat_dead_${f[0]}",
            species = f[3].ifBlank { "Unidentified" },
            commonName = f[4].ifBlank { null },
            taxonGroup = iconicTaxonToGroup(f[5]),
            location = GeoLocation(lat, lon),
            observedOn = f[6].ifBlank { null },
            source = INATURALIST_ROADKILL_SOURCE,
            observationUrl = f[7].ifBlank { null }
        )
    }.toList()
}

fun iconicTaxonToGroup(iconicTaxonName: String?): String = when (iconicTaxonName) {
    "Mammalia" -> "Mammals"
    "Aves" -> "Birds"
    "Reptilia" -> "Reptiles"
    "Amphibia" -> "Amphibians"
    "Actinopterygii" -> "Fish"
    else -> "Other"
}
