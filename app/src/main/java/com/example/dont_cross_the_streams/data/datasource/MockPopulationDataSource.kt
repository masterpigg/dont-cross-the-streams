package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.BoundingBox
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.UrbanLevel

object MockPopulationDataSource {

    val densityZones = listOf(
        // Missouri Population & Urban Growth Zones
        PopulationDensityZone(
            id = "pop_mo_001",
            regionName = "Greater St. Louis Metropolitan Area",
            boundingBox = BoundingBox(38.350, -90.650, 38.900, -89.850),
            centerLocation = GeoLocation(38.6270, -90.1994),
            densityScore = 2100.0,
            urbanLevel = UrbanLevel.METROPOLITAN,
            source = "US Census Bureau 2020 / East-West Gateway Council"
        ),
        PopulationDensityZone(
            id = "pop_mo_002",
            regionName = "Kansas City Metropolitan Sprawl Zone",
            boundingBox = BoundingBox(38.800, -94.850, 39.350, -94.150),
            centerLocation = GeoLocation(39.0997, -94.5786),
            densityScore = 1680.0,
            urbanLevel = UrbanLevel.METROPOLITAN,
            source = "US Census Bureau 2020 / MARC"
        ),
        PopulationDensityZone(
            id = "pop_mo_003",
            regionName = "Springfield-Branson Development Corridor",
            boundingBox = BoundingBox(36.500, -93.500, 37.350, -93.100),
            centerLocation = GeoLocation(37.2090, -93.2923),
            densityScore = 620.0,
            urbanLevel = UrbanLevel.SUBURBAN,
            source = "US Census Bureau 2020 / OTO"
        ),
        PopulationDensityZone(
            id = "pop_mo_004",
            regionName = "Lake of the Ozarks Resort Growth Zone",
            boundingBox = BoundingBox(38.000, -92.850, 38.300, -92.400),
            centerLocation = GeoLocation(38.2012, -92.6238),
            densityScore = 210.0,
            urbanLevel = UrbanLevel.SUBURBAN,
            source = "Missouri Spatial Data Information Service (MSDIS)"
        ),
        PopulationDensityZone(
            id = "pop_mo_005",
            regionName = "Mark Twain National Forest Wilderness Buffer",
            boundingBox = BoundingBox(36.600, -92.000, 37.800, -90.500),
            centerLocation = GeoLocation(37.2000, -91.2000),
            densityScore = 8.4,
            urbanLevel = UrbanLevel.WILDERNESS,
            source = "NASA Human Footprint Index v3 / US Forest Service"
        ),

        // National Density Zones
        PopulationDensityZone(
            id = "pop_001",
            regionName = "Front Range Urban Corridor",
            boundingBox = BoundingBox(39.500, -105.200, 40.200, -104.700),
            centerLocation = GeoLocation(39.739, -104.990),
            densityScore = 1450.0, // High density
            urbanLevel = UrbanLevel.METROPOLITAN,
            source = "US Census Bureau TIGER/Line 2020"
        ),
        PopulationDensityZone(
            id = "pop_002",
            regionName = "Greater Yellowstone Ecosystem Buffer",
            boundingBox = BoundingBox(43.500, -111.000, 45.200, -109.500),
            centerLocation = GeoLocation(44.428, -110.588),
            densityScore = 4.2, // Very low density
            urbanLevel = UrbanLevel.WILDERNESS,
            source = "NASA Human Footprint Index v3"
        ),
        PopulationDensityZone(
            id = "pop_003",
            regionName = "Upper Green River Basin / Pinedale",
            boundingBox = BoundingBox(42.300, -110.200, 43.100, -109.300),
            centerLocation = GeoLocation(42.851, -109.859),
            densityScore = 12.8, // Rural oil-gas interface
            urbanLevel = UrbanLevel.RURAL,
            source = "US Census Bureau 2020"
        ),
        PopulationDensityZone(
            id = "pop_004",
            regionName = "Southern California Coastal Range",
            boundingBox = BoundingBox(33.800, -118.600, 34.300, -118.000),
            centerLocation = GeoLocation(34.134, -118.321),
            densityScore = 3200.0, // Highly urbanized
            urbanLevel = UrbanLevel.URBAN,
            source = "US Census / NASA Human Footprint"
        ),
        PopulationDensityZone(
            id = "pop_005",
            regionName = "Great Smoky Mountains Interface",
            boundingBox = BoundingBox(35.200, -84.000, 36.000, -83.000),
            centerLocation = GeoLocation(35.611, -83.489),
            densityScore = 48.5, // Suburban / Rural mix
            urbanLevel = UrbanLevel.SUBURBAN,
            source = "US Census Bureau 2020"
        ),

        // Illinois Population Density Zones
        PopulationDensityZone(
            id = "pop_il_001",
            regionName = "Metro East St. Louis Suburban Sprawl (St. Clair & Madison)",
            boundingBox = BoundingBox(38.450, -90.200, 38.900, -89.700),
            centerLocation = GeoLocation(38.6500, -89.9800),
            densityScore = 1850.0,
            urbanLevel = UrbanLevel.METROPOLITAN,
            source = "US Census Bureau 2020 / East-West Gateway Council"
        ),

        // Washington Population Density Zones
        PopulationDensityZone(
            id = "pop_wa_001",
            regionName = "Puget Sound / Seattle-Tacoma Metropolitan Encroachment",
            boundingBox = BoundingBox(47.150, -122.550, 47.850, -122.100),
            centerLocation = GeoLocation(47.6062, -122.3321),
            densityScore = 2350.0,
            urbanLevel = UrbanLevel.METROPOLITAN,
            source = "US Census Bureau 2020 / Puget Sound Regional Council"
        )
    )
}
