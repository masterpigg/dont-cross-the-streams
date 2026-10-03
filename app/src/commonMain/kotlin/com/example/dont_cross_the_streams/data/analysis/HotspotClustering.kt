package com.example.dont_cross_the_streams.data.analysis

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Derives collision hotspots from real point reports with DBSCAN: a hotspot is a group of at least
 * [minReports] reports, each within [radiusKm] of another in the group. Nothing here is hand-placed;
 * every hotspot lists the report ids it was built from.
 */
object HotspotClustering {

    const val DEFAULT_RADIUS_KM = 0.75
    const val DEFAULT_MIN_REPORTS = 3

    fun cluster(
        reports: List<CollisionReport>,
        barriers: List<BarrierFeature> = emptyList(),
        radiusKm: Double = DEFAULT_RADIUS_KM,
        minReports: Int = DEFAULT_MIN_REPORTS
    ): List<CollisionHotspot> {
        val n = reports.size
        if (n < minReports) return emptyList()
        val neighbors = Array(n) { i ->
            (0 until n).filter { j -> j != i && reports[i].location.distanceToKm(reports[j].location) <= radiusKm }
        }
        val clusterOf = IntArray(n) { -1 }
        var next = 0
        for (i in 0 until n) {
            if (clusterOf[i] != -1 || neighbors[i].size + 1 < minReports) continue
            val id = next++
            val queue = ArrayDeque<Int>().apply { add(i) }
            clusterOf[i] = id
            while (queue.isNotEmpty()) {
                val p = queue.removeFirst()
                if (neighbors[p].size + 1 < minReports) continue // border point: joins but doesn't expand
                for (q in neighbors[p]) {
                    if (clusterOf[q] == -1) {
                        clusterOf[q] = id
                        queue.add(q)
                    }
                }
            }
        }
        val roads = barriers.filter { it.type == BarrierType.HIGHWAY && it.geometryPath.size >= 2 }
        return (0 until next).map { id -> reports.filterIndexed { i, _ -> clusterOf[i] == id } }
            .map { members -> toHotspot(members, roads, radiusKm) }
            .sortedByDescending { it.incidentCount }
    }

    private fun toHotspot(members: List<CollisionReport>, roads: List<BarrierFeature>, radiusKm: Double): CollisionHotspot {
        val center = GeoLocation(members.map { it.location.latitude }.average(), members.map { it.location.longitude }.average())
        val topSpecies = members.groupingBy { it.commonName ?: it.species }.eachCount()
            .entries.sortedByDescending { it.value }.take(3)
            .joinToString(", ") { "${it.key} (${it.value})" }
        val dates = members.mapNotNull { it.observedOn?.take(10) }.sorted()
        val nearestRoad = roads.map { it to distanceToPathKm(center, it.geometryPath) }
            .filter { it.second <= 0.3 }
            .minByOrNull { it.second }?.first?.name
        val count = members.size
        return CollisionHotspot(
            id = "hotspot_" + members.map { it.id }.sorted().first(),
            location = center,
            incidentCount = count,
            primarySpeciesAffected = topSpecies,
            severity = when {
                count >= 15 -> CollisionSeverity.CRITICAL
                count >= 8 -> CollisionSeverity.HIGH
                count >= 5 -> CollisionSeverity.MODERATE
                else -> CollisionSeverity.LOW
            },
            source = "Derived from iNaturalist dead-animal reports",
            highwayOrRouteName = nearestRoad,
            description = "Cluster of $count dead-animal reports, each within ${(radiusKm * 1000).toInt()} m of another" +
                (if (dates.isNotEmpty()) ", observed ${dates.first()} to ${dates.last()}" else "") + ".",
            reportIds = members.map { it.id },
            firstObserved = dates.firstOrNull(),
            lastObserved = dates.lastOrNull()
        )
    }

    /** Distance from [p] to the nearest segment of [path], in km (local flat-earth approximation). */
    fun distanceToPathKm(p: GeoLocation, path: List<GeoLocation>): Double {
        val kmPerDegLat = 111.32
        val kmPerDegLon = 111.32 * cos(p.latitude * PI / 180.0)
        fun x(g: GeoLocation) = (g.longitude - p.longitude) * kmPerDegLon
        fun y(g: GeoLocation) = (g.latitude - p.latitude) * kmPerDegLat
        var best = Double.MAX_VALUE
        for ((a, b) in path.zipWithNext()) {
            val ax = x(a); val ay = y(a); val bx = x(b); val by = y(b)
            val dx = bx - ax; val dy = by - ay
            val len2 = dx * dx + dy * dy
            val t = if (len2 == 0.0) 0.0 else ((-ax) * dx + (-ay) * dy) / len2
            val tc = t.coerceIn(0.0, 1.0)
            val cx = ax + tc * dx; val cy = ay + tc * dy
            best = minOf(best, sqrt(cx * cx + cy * cy))
        }
        return best
    }
}
