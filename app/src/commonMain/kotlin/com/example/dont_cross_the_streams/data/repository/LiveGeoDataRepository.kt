package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.remote.GbifClient
import com.example.dont_cross_the_streams.data.remote.HttpGet
import com.example.dont_cross_the_streams.data.remote.INaturalistClient
import com.example.dont_cross_the_streams.data.remote.NbiClient
import com.example.dont_cross_the_streams.data.remote.OverpassClient
import com.example.dont_cross_the_streams.data.remote.TigerWebClient
import com.example.dont_cross_the_streams.data.remote.httpGetText
import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.Infrastructure
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.StudyArea
import com.example.dont_cross_the_streams.domain.model.StudyAreaStats
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence
import com.example.dont_cross_the_streams.domain.repository.GeoDataRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

class LiveGeoDataRepository(http: HttpGet = ::httpGetText) : GeoDataRepository {
    private val inat = INaturalistClient(http)
    private val gbif = GbifClient(http)
    private val overpass = OverpassClient(http)
    private val nbi = NbiClient(http)
    private val tiger = TigerWebClient(http)

    override suspend fun wildlifeObservations(bounds: BoundingBox): List<WildlifeOccurrence>? = coroutineScope {
        val fromInat = async { timed { inat.observations(bounds) } }
        val fromGbif = async { timed { gbif.occurrences(bounds) } }
        val dead = async { timed { inat.deadAnimalReports(bounds, pages = 1) } }
        val a = fromInat.await()
        val b = fromGbif.await()
        if (a == null && b == null) return@coroutineScope null
        // Keep the "wildlife" layer to live sightings: drop anything iNaturalist marks as dead.
        val deadIds = dead.await().orEmpty().map { it.id.removePrefix("inat_dead_") }.toSet()
        (a.orEmpty().filter { it.id.removePrefix("inat_") !in deadIds } + b.orEmpty()).distinctBy { it.id }
    }

    override suspend fun collisionReports(bounds: BoundingBox): List<CollisionReport>? =
        timed { inat.deadAnimalReports(bounds) }

    override suspend fun infrastructure(bounds: BoundingBox, includeCulverts: Boolean): Infrastructure? =
        timed(30_000) { overpass.infrastructure(bounds, includeCulverts) }

    override suspend fun waterwayStructures(bounds: BoundingBox): List<WildlifeCrossing>? =
        timed { nbi.waterwayStructures(bounds) }

    override suspend fun censusTracts(bounds: BoundingBox): List<PopulationDensityZone>? =
        timed { tiger.tracts(bounds) }

    override suspend fun studyAreaStats(area: StudyArea): StudyAreaStats = coroutineScope {
        val b = area.bounds
        val dead = async { timed { inat.countDeadAnimalReports(b) } }
        val obs = async { timed { inat.countObservations(b) } }
        val gbifCount = async { timed { gbif.count(b) } }
        val structures = async { timed { nbi.countWaterwayStructures(b) } }
        val crossings = async { timed(60_000) { overpass.countWildlifeCrossings(b) } }
        val roadKm = async { timed(60_000) { overpass.majorRoadKm(b) } }
        val population = async {
            val state = area.stateFips
            val county = area.countyFips
            if (state != null && county != null) timed { tiger.countyPopulation(state, county) } else null
        }
        StudyAreaStats(
            area = area,
            deadAnimalReports = dead.await(),
            wildlifeObservations = obs.await(),
            gbifOccurrences = gbifCount.await(),
            waterwayStructures = structures.await(),
            dedicatedCrossings = crossings.await(),
            majorRoadKm = roadKm.await(),
            population = population.await()
        )
    }

    private suspend fun <T> timed(ms: Long = 20_000, block: suspend () -> T?): T? = withTimeoutOrNull(ms) { block() }
}
