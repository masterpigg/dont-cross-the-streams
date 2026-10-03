package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation

/** Camera shortcuts for the St. Louis study region. Presets only move the map; they add no data. */
enum class ConflictRegionPreset(
    val title: String,
    val subtitle: String,
    val center: GeoLocation,
    val zoomLevel: Float,
    val taxonGroups: Set<String> = setOf("Mammals", "Birds", "Reptiles", "Amphibians"),
    val barrierTypes: Set<BarrierType> = BarrierType.entries.toSet()
) {
    STL_REGION(
        title = "St. Louis Region",
        subtitle = "St. Louis City, St. Louis County and St. Charles County",
        center = GeoLocation(38.70, -90.45),
        zoomLevel = 10f
    ),
    STL_CITY(
        title = "St. Louis City",
        subtitle = "Forest Park, River Des Peres and the I-64/I-44/I-70 core",
        center = GeoLocation(38.63, -90.24),
        zoomLevel = 12f
    ),
    STL_COUNTY_WEST(
        title = "West St. Louis County",
        subtitle = "Creve Coeur Park, I-270 and the Missouri River bottoms",
        center = GeoLocation(38.70, -90.50),
        zoomLevel = 12f
    ),
    MERAMEC_I44(
        title = "Meramec River & I-44",
        subtitle = "Castlewood State Park, Valley Park and Eureka",
        center = GeoLocation(38.53, -90.56),
        zoomLevel = 12f
    ),
    ST_CHARLES_CITY(
        title = "St. Charles",
        subtitle = "I-70, I-370 and the Missouri River crossing",
        center = GeoLocation(38.79, -90.50),
        zoomLevel = 12f
    ),
    BUSCH_WELDON_SPRING(
        title = "Busch & Weldon Spring CAs",
        subtitle = "Conservation areas along MO-94 and US-40/I-64 in St. Charles County",
        center = GeoLocation(38.70, -90.72),
        zoomLevel = 12f
    ),
    CONFLUENCE(
        title = "Missouri–Mississippi Confluence",
        subtitle = "Floodplain between the rivers, US-67 and Route 367",
        center = GeoLocation(38.84, -90.17),
        zoomLevel = 12f
    )
}
