package com.example.dont_cross_the_streams.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class INatResponse(
    @Json(name = "total_results") val totalResults: Int? = 0,
    @Json(name = "results") val results: List<INatObservation>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class INatObservation(
    @Json(name = "id") val id: Long?,
    @Json(name = "species_guess") val speciesGuess: String?,
    @Json(name = "location") val locationStr: String?, // e.g. "37.7749,-122.4194"
    @Json(name = "taxon") val taxon: INatTaxon?,
    @Json(name = "observed_on") val observedOn: String?,
    @Json(name = "photos") val photos: List<INatPhoto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class INatTaxon(
    @Json(name = "name") val name: String?,
    @Json(name = "preferred_common_name") val preferredCommonName: String?,
    @Json(name = "iconic_taxon_name") val iconicTaxonName: String?
)

@JsonClass(generateAdapter = true)
data class INatPhoto(
    @Json(name = "url") val url: String?
)

interface INaturalistApiService {
    @GET("observations")
    suspend fun getObservations(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radius") radiusKm: Int = 50,
        @Query("per_page") perPage: Int = 30
    ): INatResponse
}
