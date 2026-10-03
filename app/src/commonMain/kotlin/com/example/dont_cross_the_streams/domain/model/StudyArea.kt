package com.example.dont_cross_the_streams.domain.model

/**
 * A jurisdiction the research compares. [bounds] is the rectangle used for the point-based APIs
 * (it slightly overlaps neighbours); population uses the exact county FIPS code instead.
 */
data class StudyArea(
    val id: String,
    val name: String,
    val description: String,
    val bounds: BoundingBox,
    val stateFips: String? = null,
    val countyFips: String? = null
) {
    val center: GeoLocation
        get() = GeoLocation((bounds.minLat + bounds.maxLat) / 2, (bounds.minLon + bounds.maxLon) / 2)
}

object StudyAreas {
    // Missouri FIPS 29. St. Louis City (510) is an independent city, separate from St. Louis County (189).
    val ST_LOUIS_CITY = StudyArea(
        id = "stl_city",
        name = "St. Louis City",
        description = "Independent city on the Mississippi River (FIPS 29-510).",
        bounds = BoundingBox(38.532, -90.321, 38.774, -90.166),
        stateFips = "29",
        countyFips = "510"
    )
    val ST_LOUIS_COUNTY = StudyArea(
        id = "stl_county",
        name = "St. Louis County",
        description = "Suburban county surrounding the city, bounded by the Missouri and Meramec rivers (FIPS 29-189).",
        bounds = BoundingBox(38.392, -90.736, 38.893, -90.117),
        stateFips = "29",
        countyFips = "189"
    )
    val ST_CHARLES_COUNTY = StudyArea(
        id = "st_charles_county",
        name = "St. Charles County",
        description = "Between the Missouri and Mississippi rivers; includes Busch and Weldon Spring conservation areas (FIPS 29-183).",
        bounds = BoundingBox(38.530, -91.080, 39.000, -90.117),
        stateFips = "29",
        countyFips = "183"
    )

    val all = listOf(ST_LOUIS_CITY, ST_LOUIS_COUNTY, ST_CHARLES_COUNTY)
}

/** Measured values for one study area. Null means that source could not be reached. */
data class StudyAreaStats(
    val area: StudyArea,
    val deadAnimalReports: Int? = null,
    val wildlifeObservations: Int? = null,
    val gbifOccurrences: Int? = null,
    val waterwayStructures: Int? = null,
    val dedicatedCrossings: Int? = null,
    val majorRoadKm: Double? = null,
    val population: AreaPopulation? = null
) {
    /** Dead-animal reports per 100 km of motorway/trunk/primary road. */
    val deadReportsPer100RoadKm: Double?
        get() = ratePer100(deadAnimalReports, majorRoadKm)

    /** Bridges and large culverts over water per 100 km of major road. */
    val waterwayStructuresPer100RoadKm: Double?
        get() = ratePer100(waterwayStructures, majorRoadKm)

    private fun ratePer100(count: Int?, km: Double?): Double? =
        if (count == null || km == null || km <= 0.0) null else count / km * 100.0
}
