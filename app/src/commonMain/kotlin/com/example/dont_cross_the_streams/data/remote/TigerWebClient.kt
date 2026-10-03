package com.example.dont_cross_the_streams.data.remote

import com.example.dont_cross_the_streams.domain.model.AreaPopulation
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.urbanLevelForDensity

const val CENSUS_SOURCE = "US Census Bureau 2020 (TIGERweb)"

/**
 * 2020 Census tracts from the Census Bureau's TIGERweb ArcGIS service: boundary polygons with the
 * 2020 population count (POP100) and land area in m² (AREALAND).
 */
class TigerWebClient(private val http: HttpGet = ::httpGetText) {

    suspend fun tracts(bounds: BoundingBox): List<PopulationDensityZone>? {
        val root = parseJsonOrNull(http(tractsUrl(bounds))).obj() ?: return null
        if (root["error"] != null) return null
        val features = root.a("features") ?: return null
        return features.mapNotNull { feature ->
            val f = feature.obj() ?: return@mapNotNull null
            val a = f.o("attributes") ?: return@mapNotNull null
            val geoid = a.strIgnoreCase("GEOID") ?: return@mapNotNull null
            val population = a.numIgnoreCase("POP100")?.toInt() ?: return@mapNotNull null
            val landKm2 = (a.numIgnoreCase("AREALAND") ?: 0.0) / 1_000_000.0
            val rings = f.o("geometry")?.a("rings")?.mapNotNull { ring ->
                ring.arr()?.mapNotNull { pt ->
                    val lon = pt.doubleAt(0) ?: return@mapNotNull null
                    val lat = pt.doubleAt(1) ?: return@mapNotNull null
                    GeoLocation(lat, lon)
                }?.takeIf { it.size >= 3 }
            }.orEmpty()
            if (rings.isEmpty()) return@mapNotNull null
            val all = rings.flatten()
            val box = BoundingBox(all.minOf { it.latitude }, all.minOf { it.longitude }, all.maxOf { it.latitude }, all.maxOf { it.longitude })
            val center = a.strIgnoreCase("CENTLAT")?.toDoubleOrNull()?.let { lat ->
                a.strIgnoreCase("CENTLON")?.toDoubleOrNull()?.let { lon -> GeoLocation(lat, lon) }
            } ?: GeoLocation((box.minLat + box.maxLat) / 2, (box.minLon + box.maxLon) / 2)
            val density = if (landKm2 > 0) population / landKm2 else 0.0
            PopulationDensityZone(
                id = "tract_$geoid",
                regionName = a.strIgnoreCase("NAME") ?: "Census Tract $geoid",
                boundingBox = box,
                centerLocation = center,
                densityScore = density,
                urbanLevel = urbanLevelForDensity(density),
                source = CENSUS_SOURCE,
                population = population,
                landAreaKm2 = landKm2,
                polygon = rings
            )
        }
    }

    /** Exact 2020 population and land area for a county, summed over its census tracts. */
    suspend fun countyPopulation(stateFips: String, countyFips: String): AreaPopulation? {
        val root = parseJsonOrNull(http(countyStatsUrl(stateFips, countyFips))).obj() ?: return null
        val a = root.a("features")?.firstOrNull().obj()?.o("attributes") ?: return null
        val pop = a.numIgnoreCase("pop") ?: return null
        val land = a.numIgnoreCase("land") ?: return null
        return AreaPopulation(pop.toInt(), land / 1_000_000.0)
    }

    companion object {
        const val TRACTS_LAYER =
            "https://tigerweb.geo.census.gov/arcgis/rest/services/Census2020/Tracts_Blocks/MapServer/0"

        fun tractsUrl(bounds: BoundingBox): String =
            "$TRACTS_LAYER/query?where=1%3D1" +
                "&geometry=${bounds.minLon},${bounds.minLat},${bounds.maxLon},${bounds.maxLat}" +
                "&geometryType=esriGeometryEnvelope&inSR=4326&spatialRel=esriSpatialRelIntersects" +
                "&outFields=*&returnGeometry=true&outSR=4326&maxAllowableOffset=0.0003&f=json"

        fun countyStatsUrl(stateFips: String, countyFips: String): String {
            val stats = "[{\"statisticType\":\"sum\",\"onStatisticField\":\"POP100\",\"outStatisticFieldName\":\"pop\"}," +
                "{\"statisticType\":\"sum\",\"onStatisticField\":\"AREALAND\",\"outStatisticFieldName\":\"land\"}]"
            return "$TRACTS_LAYER/query?where=${encodeUrlParam("STATE='$stateFips' AND COUNTY='$countyFips'")}" +
                "&outStatistics=${encodeUrlParam(stats)}&f=json"
        }
    }
}
