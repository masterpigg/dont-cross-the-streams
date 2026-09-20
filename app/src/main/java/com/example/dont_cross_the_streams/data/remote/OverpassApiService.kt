package com.example.dont_cross_the_streams.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OverpassResponse(
    @Json(name = "elements") val elements: List<OverpassElement>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class OverpassElement(
    @Json(name = "type") val type: String?,
    @Json(name = "id") val id: Long?,
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lon") val lon: Double? = null,
    @Json(name = "tags") val tags: Map<String, String>? = emptyMap(),
    @Json(name = "geometry") val geometry: List<OverpassGeometryPoint>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class OverpassGeometryPoint(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lon") val lon: Double
)

interface OverpassApiService {
    @GET("interpreter")
    suspend fun queryOverpass(
        @Query("data") queryData: String
    ): OverpassResponse
}
