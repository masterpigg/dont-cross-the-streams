package com.example.dont_cross_the_streams.data.remote

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CrossingKind
import com.example.dont_cross_the_streams.domain.model.CrossingStructureType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel
import com.example.dont_cross_the_streams.domain.model.Infrastructure
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import kotlinx.serialization.json.JsonObject

const val OSM_SOURCE = "OpenStreetMap Overpass"

/**
 * OpenStreetMap via the Overpass API (https://wiki.openstreetmap.org/wiki/Overpass_API).
 * Barriers: major roads, mainline railways, dams/weirs and canals. Crossings: structures tagged
 * man_made=wildlife_crossing, plus (when [includeCulverts]) waterway culverts under major roads.
 */
class OverpassClient(private val http: HttpGet = ::httpGetText) {

    suspend fun infrastructure(bounds: BoundingBox, includeCulverts: Boolean): Infrastructure? {
        val elements = parseJsonOrNull(http(interpreterUrl(infrastructureQuery(bounds, includeCulverts))))
            .obj()?.a("elements") ?: return null
        val barriers = mutableListOf<BarrierFeature>()
        val crossings = mutableListOf<WildlifeCrossing>()
        for (element in elements) {
            val o = element.obj() ?: continue
            val tags = o.o("tags") ?: continue
            when {
                tags.str("man_made") == "wildlife_crossing" -> toDedicatedCrossing(o, tags)?.let(crossings::add)
                tags.str("tunnel") == "culvert" && tags.str("waterway") != null -> toCulvert(o, tags)?.let(crossings::add)
                else -> toBarrier(o, tags)?.let(barriers::add)
            }
        }
        return Infrastructure(barriers, crossings)
    }

    /** Total length of motorway/trunk/primary roads in [bounds], in km (computed by Overpass). */
    suspend fun majorRoadKm(bounds: BoundingBox): Double? {
        val q = "[out:json][timeout:60];way[\"highway\"~\"^(motorway|trunk|primary)$\"](${bounds.overpassBox()});" +
            "make stats road_m=sum(length());out;"
        val stats = parseJsonOrNull(http(interpreterUrl(q))).obj()?.a("elements")?.firstOrNull().obj()?.o("tags")
        return stats?.num("road_m")?.div(1000.0)
    }

    /** Number of OSM-mapped dedicated wildlife crossings in [bounds]. */
    suspend fun countWildlifeCrossings(bounds: BoundingBox): Int? {
        val q = "[out:json][timeout:30];nwr[\"man_made\"=\"wildlife_crossing\"](${bounds.overpassBox()});out count;"
        val tags = parseJsonOrNull(http(interpreterUrl(q))).obj()?.a("elements")?.firstOrNull().obj()?.o("tags")
        return tags?.num("total")?.toInt()
    }

    private fun toBarrier(o: JsonObject, tags: JsonObject): BarrierFeature? {
        val highway = tags.str("highway")
        val waterway = tags.str("waterway")
        val classification = classify(highway, tags.str("railway"), waterway) ?: return null
        val type = classification.first
        val impact = classification.second
        val path = o.a("geometry")?.mapNotNull { point ->
            val p = point.obj() ?: return@mapNotNull null
            val lat = p.num("lat") ?: return@mapNotNull null
            val lon = p.num("lon") ?: return@mapNotNull null
            GeoLocation(lat, lon)
        }.orEmpty()
        val location = o.point() ?: path.getOrNull(path.size / 2) ?: return null
        val osmType = o.str("type") ?: "way"
        val id = o.str("id") ?: return null
        val ref = tags.str("ref")
        val name = listOfNotNull(ref, tags.str("name")).distinct().joinToString(" · ").ifBlank {
            when (type) {
                BarrierType.HIGHWAY -> "Unnamed ${highway ?: "road"}"
                BarrierType.RAILWAY -> tags.str("operator") ?: "Railway"
                BarrierType.DAM -> "Unnamed ${waterway ?: "dam"}"
                else -> "Unnamed ${waterway ?: "barrier"}"
            }
        }
        return BarrierFeature(
            id = "osm_${osmType}_$id",
            type = type,
            name = name,
            location = location,
            geometryPath = path,
            impactLevel = impact,
            source = OSM_SOURCE,
            lengthKm = if (path.size >= 2) path.zipWithNext { a, b -> a.distanceToKm(b) }.sum() else null,
            description = listOfNotNull(
                highway?.let { "OSM highway=$it" },
                tags.str("lanes")?.let { "$it lanes" },
                tags.str("maxspeed")?.let { "speed limit $it" },
                tags.str("railway")?.let { "railway=$it" },
                waterway?.let { "waterway=$it" }
            ).joinToString(", ").ifBlank { null }
        )
    }

    /** Barrier type and impact class by OSM tag: motorway severe, trunk high, primary moderate. */
    // One `when` per tag on purpose: Kotlin/Wasm 2.1.0 miscompiles a subject-less `when` whose first
    // branches compare one variable to string constants into a switch on that variable alone, so
    // later branches testing other variables (railway, waterway) were never reached.
    private fun classify(highway: String?, railway: String?, waterway: String?): Pair<BarrierType, ImpactLevel>? {
        when (highway) {
            "motorway" -> return BarrierType.HIGHWAY to ImpactLevel.SEVERE
            "trunk" -> return BarrierType.HIGHWAY to ImpactLevel.HIGH
            "primary" -> return BarrierType.HIGHWAY to ImpactLevel.MODERATE
        }
        if (railway == "rail") return BarrierType.RAILWAY to ImpactLevel.MODERATE
        return when (waterway) {
            "dam" -> BarrierType.DAM to ImpactLevel.SEVERE
            "weir" -> BarrierType.DAM to ImpactLevel.HIGH
            "canal" -> BarrierType.CANAL to ImpactLevel.MODERATE
            else -> null
        }
    }

    private fun toDedicatedCrossing(o: JsonObject, tags: JsonObject): WildlifeCrossing? {
        val location = o.point() ?: return null
        val type = when {
            tags.str("bridge") != null && tags.str("bridge") != "no" -> CrossingStructureType.OVERPASS
            tags.str("tunnel") != null && tags.str("tunnel") != "no" -> CrossingStructureType.UNDERPASS
            else -> CrossingStructureType.UNDERPASS
        }
        return WildlifeCrossing(
            id = "osm_${o.str("type")}_${o.str("id")}",
            name = tags.str("name") ?: "Wildlife crossing",
            structureType = type,
            location = location,
            targetSpecies = tags.str("animal") ?: tags.str("wildlife_crossing") ?: "Not specified in OSM",
            source = OSM_SOURCE,
            kind = CrossingKind.DEDICATED,
            description = tags.str("description"),
            recordUrl = osmUrl(o)
        )
    }

    private fun toCulvert(o: JsonObject, tags: JsonObject): WildlifeCrossing? {
        val location = o.point() ?: return null
        val waterwayName = tags.str("name")
        return WildlifeCrossing(
            id = "osm_culvert_${o.str("id")}",
            name = waterwayName?.let { "Culvert: $it" } ?: "Culvert (${tags.str("waterway")})",
            structureType = CrossingStructureType.CULVERT,
            location = location,
            targetSpecies = "Any (existing drainage structure)",
            source = OSM_SOURCE,
            kind = CrossingKind.WATERWAY_STRUCTURE,
            crosses = waterwayName ?: tags.str("waterway"),
            description = "Waterway culvert within 20 m of a major road, mapped in OpenStreetMap.",
            recordUrl = osmUrl(o)
        )
    }

    private fun JsonObject.point(): GeoLocation? {
        num("lat")?.let { lat -> num("lon")?.let { lon -> return GeoLocation(lat, lon) } }
        val c = o("center") ?: return null
        val lat = c.num("lat") ?: return null
        val lon = c.num("lon") ?: return null
        return GeoLocation(lat, lon)
    }

    private fun osmUrl(o: JsonObject): String? {
        val type = o.str("type") ?: return null
        val id = o.str("id") ?: return null
        return "https://www.openstreetmap.org/$type/$id"
    }

    companion object {
        const val ENDPOINT = "https://overpass-api.de/api/interpreter"

        fun interpreterUrl(query: String) = "$ENDPOINT?data=${encodeUrlParam(query)}"

        /** Overpass bbox order is south,west,north,east. */
        fun BoundingBox.overpassBox() = "$minLat,$minLon,$maxLat,$maxLon"

        fun infrastructureQuery(bounds: BoundingBox, includeCulverts: Boolean): String {
            val b = bounds.overpassBox()
            val culverts = if (includeCulverts) {
                "way[\"highway\"~\"^(motorway|trunk|primary|secondary)$\"]($b)->.roads;" +
                    "way[\"waterway\"][\"tunnel\"=\"culvert\"](around.roads:20);out center;"
            } else {
                ""
            }
            return "[out:json][timeout:25];(" +
                "way[\"highway\"~\"^(motorway|trunk|primary)$\"]($b);" +
                "way[\"railway\"=\"rail\"][!\"service\"]($b);" +
                "nwr[\"waterway\"~\"^(dam|weir)$\"]($b);" +
                "way[\"waterway\"=\"canal\"]($b);" +
                ");out body geom;" +
                "nwr[\"man_made\"=\"wildlife_crossing\"]($b);out center;" +
                culverts
        }
    }
}
