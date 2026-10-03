package com.example.dont_cross_the_streams.data.remote

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CrossingKind
import com.example.dont_cross_the_streams.domain.model.CrossingStructureType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing

const val NBI_SOURCE = "FHWA National Bridge Inventory (BTS NTAD)"

/**
 * FHWA National Bridge Inventory, served by the BTS National Transportation Atlas Database ArcGIS
 * service. Only structures over water (NBI item 42B "type of service under" = 5-9) are requested:
 * the bridges and large culverts (span >= 20 ft) that streams, and the animals that follow them,
 * already use to get under roads.
 */
class NbiClient(private val http: HttpGet = ::httpGetText) {

    suspend fun waterwayStructures(bounds: BoundingBox, limit: Int = 1000): List<WildlifeCrossing>? {
        val root = parseJsonOrNull(http(queryUrl(bounds, countOnly = false, limit = limit))).obj() ?: return null
        if (root["error"] != null) return null
        val features = root.a("features") ?: return null
        return features.mapNotNull { feature ->
            val a = feature.obj()?.o("attributes") ?: return@mapNotNull null
            val lat = a.numIgnoreCase("LATDD") ?: return@mapNotNull null
            val lon = a.numIgnoreCase("LONGDD") ?: return@mapNotNull null
            if (lat == 0.0 || lon == 0.0) return@mapNotNull null
            val structureNumber = a.strIgnoreCase("STRUCTURE_NUMBER_008") ?: "${lat}_$lon"
            val isCulvert = a.strIgnoreCase("STRUCTURE_TYPE_043B")?.trimStart('0') == "19"
            val carries = a.strIgnoreCase("FACILITY_CARRIED_007")
            val crosses = a.strIgnoreCase("FEATURES_DESC_006A")
            WildlifeCrossing(
                id = "nbi_$structureNumber",
                name = listOfNotNull(carries, crosses?.let { "over $it" }).joinToString(" ")
                    .ifBlank { "NBI structure $structureNumber" },
                structureType = if (isCulvert) CrossingStructureType.CULVERT else CrossingStructureType.BRIDGE,
                location = GeoLocation(lat, lon),
                targetSpecies = "Any (existing waterway structure)",
                source = NBI_SOURCE,
                kind = CrossingKind.WATERWAY_STRUCTURE,
                carries = carries,
                crosses = crosses,
                averageDailyTraffic = a.numIgnoreCase("ADT_029")?.toInt(),
                yearBuilt = a.numIgnoreCase("YEAR_BUILT_027")?.toInt()?.takeIf { it > 1700 },
                description = "NBI structure $structureNumber: ${serviceUnderLabel(a.strIgnoreCase("SERVICE_UND_042B"))}."
            )
        }
    }

    suspend fun countWaterwayStructures(bounds: BoundingBox): Int? {
        val root = parseJsonOrNull(http(queryUrl(bounds, countOnly = true, limit = 0))).obj() ?: return null
        return root.num("count")?.toInt()
    }

    companion object {
        const val LAYER =
            "https://geo.dot.gov/server/rest/services/Hosted/National_Bridge_Inventory_DS/FeatureServer/0"

        // Item 42B codes 5-9: waterway, highway-waterway, railroad-waterway, all three, relief for waterway.
        const val WATERWAY_WHERE = "SERVICE_UND_042B IN ('5','6','7','8','9')"

        fun queryUrl(bounds: BoundingBox, countOnly: Boolean, limit: Int): String =
            "$LAYER/query?where=${encodeUrlParam(WATERWAY_WHERE)}" +
                "&geometry=${bounds.minLon},${bounds.minLat},${bounds.maxLon},${bounds.maxLat}" +
                "&geometryType=esriGeometryEnvelope&inSR=4326&spatialRel=esriSpatialRelIntersects" +
                (if (countOnly) "&returnCountOnly=true" else "&outFields=*&returnGeometry=false&resultRecordCount=$limit") +
                "&f=json"

        fun serviceUnderLabel(code: String?): String = when (code?.trim()?.trimStart('0')) {
            "5" -> "crosses a waterway"
            "6" -> "crosses a highway and a waterway"
            "7" -> "crosses a railroad and a waterway"
            "8" -> "crosses a highway, waterway and railroad"
            "9" -> "relief structure for a waterway"
            else -> "crosses a waterway"
        }
    }
}
