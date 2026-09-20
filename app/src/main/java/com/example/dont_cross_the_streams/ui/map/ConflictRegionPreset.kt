package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation

enum class ConflictRegionPreset(
    val title: String,
    val subtitle: String,
    val center: GeoLocation,
    val zoomLevel: Float,
    val taxonGroups: Set<String>,
    val barrierTypes: Set<BarrierType>
) {
    MISSOURI_I70_CORRIDOR(
        title = "Missouri I-70 & Deer Corridor",
        subtitle = "Severe deer-vehicle collision zone & interstate divide",
        center = GeoLocation(38.95, -92.33),
        zoomLevel = 9.0f,
        taxonGroups = setOf("Mammals", "Birds"),
        barrierTypes = setOf(BarrierType.HIGHWAY, BarrierType.RAILWAY)
    ),
    OZARK_BAGNELL_DAM(
        title = "Ozarks & Bagnell Dam Barrier",
        subtitle = "Osage river aquatic barrier, elk & black bear corridors",
        center = GeoLocation(38.20, -92.62),
        zoomLevel = 10.0f,
        taxonGroups = setOf("Fish", "Mammals", "Reptiles", "Amphibians"),
        barrierTypes = setOf(BarrierType.DAM, BarrierType.HIGHWAY, BarrierType.FENCE)
    ),
    ST_LOUIS_SPRAWL(
        title = "St. Louis Metro Encroachment",
        subtitle = "Urban expansion & Mississippi river flyway corridor",
        center = GeoLocation(38.62, -90.20),
        zoomLevel = 10.0f,
        taxonGroups = setOf("Birds", "Mammals", "Amphibians"),
        barrierTypes = setOf(BarrierType.HIGHWAY, BarrierType.URBAN_WALL, BarrierType.RAILWAY)
    ),
    YELLOWSTONE_CORRIDOR(
        title = "Yellowstone Highway Corridor",
        subtitle = "Ungulate migrations & highway collisions",
        center = GeoLocation(44.428, -110.588),
        zoomLevel = 8.5f,
        taxonGroups = setOf("Mammals", "Birds"),
        barrierTypes = setOf(BarrierType.HIGHWAY, BarrierType.FENCE)
    ),
    SNAKE_RIVER_DAMS(
        title = "Snake River Dams",
        subtitle = "Aquatic migration barriers & fish passage",
        center = GeoLocation(45.644, -121.941),
        zoomLevel = 7.5f,
        taxonGroups = setOf("Fish", "Amphibians"),
        barrierTypes = setOf(BarrierType.DAM, BarrierType.CANAL)
    ),
    CA17_COUGAR(
        title = "CA-17 Cougar Corridor",
        subtitle = "Suburban freeway & soundwall isolation",
        center = GeoLocation(34.134, -118.321),
        zoomLevel = 9.5f,
        taxonGroups = setOf("Mammals"),
        barrierTypes = setOf(BarrierType.HIGHWAY, BarrierType.URBAN_WALL, BarrierType.RAILWAY)
    ),
    FLORIDA_PANTHER(
        title = "Florida Panther Wildlife Crossing",
        subtitle = "Critical endangered feline habitat fragmentation",
        center = GeoLocation(26.168, -81.352),
        zoomLevel = 9.0f,
        taxonGroups = setOf("Mammals", "Reptiles"),
        barrierTypes = setOf(BarrierType.HIGHWAY, BarrierType.FENCE)
    )
}

