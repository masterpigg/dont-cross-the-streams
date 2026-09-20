package com.example.dont_cross_the_streams.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class GbifOccurrenceResponse(
    @Json(name = "offset") val offset: Int? = 0,
    @Json(name = "limit") val limit: Int? = 20,
    @Json(name = "endOfRecords") val endOfRecords: Boolean? = true,
    @Json(name = "results") val results: List<GbifOccurrenceResult>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GbifOccurrenceResult(
    @Json(name = "key") val key: Long?,
    @Json(name = "scientificName") val scientificName: String?,
    @Json(name = "vernacularName") val vernacularName: String?,
    @Json(name = "decimalLatitude") val decimalLatitude: Double?,
    @Json(name = "decimalLongitude") val decimalLongitude: Double?,
    @Json(name = "class") val classGroup: String?,
    @Json(name = "individualCount") val individualCount: Int? = 1,
    @Json(name = "eventDate") val eventDate: String?
)

interface GbifApiService {
    @GET("occurrence/search")
    suspend fun searchOccurrences(
        @Query("scientificName") scientificName: String? = null,
        @Query("decimalLatitude") latitude: Double? = null,
        @Query("decimalLongitude") longitude: Double? = null,
        @Query("limit") limit: Int = 30
    ): GbifOccurrenceResponse
}
