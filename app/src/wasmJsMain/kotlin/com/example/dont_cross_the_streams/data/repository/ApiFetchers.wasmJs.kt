package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import kotlinx.coroutines.await
import kotlin.js.Promise

// Fetches and flattens iNaturalist results in JS so only one plain string crosses into Wasm.
// Resolves to null on any network/HTTP/parse failure.
@JsFun("""
(url) => fetch(url)
    .then(res => res.ok ? res.json() : null)
    .then(json => {
        if (!json || !Array.isArray(json.results)) return null;
        const clean = v => String(v == null ? '' : v).replace(/[\t\r\n]/g, ' ');
        return json.results
            .filter(o => o.geojson && Array.isArray(o.geojson.coordinates))
            .map(o => {
                const t = o.taxon || {};
                return [
                    o.id,
                    o.geojson.coordinates[1],
                    o.geojson.coordinates[0],
                    t.name,
                    t.preferred_common_name || o.species_guess,
                    t.iconic_taxon_name,
                    o.observed_on,
                    o.uri
                ].map(clean).join('\t');
            })
            .join('\n');
    })
    .catch(() => null)
""")
private external fun fetchRoadkillRowsJs(url: String): Promise<JsString?>

actual suspend fun fetchLiveOverpassBarriersApi(bboxQuery: String): List<BarrierFeature> = emptyList()

actual suspend fun fetchLiveOccurrencesFromGbifApi(
    scientificName: String?,
    latitude: Double?,
    longitude: Double?
): List<WildlifeOccurrence> = emptyList()

actual suspend fun fetchLiveObservationsFromINaturalistApi(
    latitude: Double?,
    longitude: Double?,
    radiusKm: Int
): List<WildlifeOccurrence> = emptyList()

actual suspend fun fetchLiveRoadkillReportsApi(bounds: BoundingBox, maxResults: Int): List<CollisionReport>? {
    val rows = fetchRoadkillRowsJs(buildINaturalistRoadkillUrl(bounds, maxResults)).await<JsString?>()
    return rows?.toString()?.let(::parseRoadkillRows)
}
