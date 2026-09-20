package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel

object MockBarrierDataSource {

    val barrierFeatures = listOf(
        // Missouri Linear Barriers
        BarrierFeature(
            id = "bar_mo_001",
            type = BarrierType.HIGHWAY,
            name = "Interstate 70 Missouri East-West Divide",
            location = GeoLocation(38.9500, -92.3300), // Central Missouri I-70
            geometryPath = listOf(
                GeoLocation(39.0500, -94.3000), // KC
                GeoLocation(38.9500, -92.3300), // Columbia
                GeoLocation(38.7000, -90.5000)  // St. Louis outskirts
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "MoDOT GIS / OpenStreetMap",
            heightMeters = 1.4,
            lengthKm = 390.0,
            description = "East-West continuous 6-lane concrete jersey barrier dividing northern prairie and southern Ozark Missouri wildlife populations."
        ),
        BarrierFeature(
            id = "bar_mo_002",
            type = BarrierType.HIGHWAY,
            name = "Interstate 44 Ozark Expressway",
            location = GeoLocation(38.2000, -91.0000),
            geometryPath = listOf(
                GeoLocation(38.5000, -90.4000), // St. Louis County
                GeoLocation(37.9514, -91.7713), // Rolla
                GeoLocation(37.2090, -93.2923)  // Springfield
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "MoDOT Safety Data",
            heightMeters = 1.2,
            lengthKm = 460.0,
            description = "High-speed 4-lane divided highway with right-of-way chainlink fencing restricting Ozark mammal movements."
        ),
        BarrierFeature(
            id = "bar_mo_003",
            type = BarrierType.DAM,
            name = "Bagnell Dam (Lake of the Ozarks)",
            location = GeoLocation(38.2012, -92.6238),
            geometryPath = listOf(
                GeoLocation(38.2012, -92.6238)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "USACE National Inventory of Dams",
            heightMeters = 45.0,
            lengthKm = 0.77,
            description = "Osage River hydroelectric dam blocking aquatic organism migration and fragmenting paddlefish and sturgeon spawning routes."
        ),
        BarrierFeature(
            id = "bar_mo_004",
            type = BarrierType.DAM,
            name = "Harry S. Truman Dam & Reservoir",
            location = GeoLocation(38.2858, -93.4194),
            geometryPath = listOf(
                GeoLocation(38.2858, -93.4194)
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "USACE National Inventory of Dams",
            heightMeters = 38.0,
            lengthKm = 1.5,
            description = "Major Osage Basin flood control dam blocking upstream fish passage into Truman Lake."
        ),
        BarrierFeature(
            id = "bar_mo_005",
            type = BarrierType.RAILWAY,
            name = "Mississippi & Missouri River BNSF Freight Corridor",
            location = GeoLocation(38.6270, -90.1994),
            geometryPath = listOf(
                GeoLocation(39.7084, -91.3584), // Hannibal
                GeoLocation(38.8000, -90.2000), // St. Louis Confluence
                GeoLocation(38.2839, -90.3843)  // Pevely
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "Federal Railroad Administration / OpenStreetMap",
            heightMeters = 0.6,
            lengthKm = 280.0,
            description = "High-traffic dual-track rail freight line creating acoustic noise barriers and steep ballast embankment hazards along river corridors."
        ),

        // National Linear Barriers
        BarrierFeature(
            id = "bar_001",
            type = BarrierType.HIGHWAY,
            name = "Interstate 70 Mountain Corridor",
            location = GeoLocation(39.638, -105.897),
            geometryPath = listOf(
                GeoLocation(39.701, -105.500),
                GeoLocation(39.638, -105.897),
                GeoLocation(39.575, -106.100)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "OpenStreetMap Overpass",
            heightMeters = 1.2,
            lengthKm = 240.0,
            description = "High-speed 4-to-6 lane divided interstate with continuous concrete jersey barriers preventing cross-freeway ungulate movements."
        ),
        BarrierFeature(
            id = "bar_002",
            type = BarrierType.RAILWAY,
            name = "Union Pacific Transcontinental Mainline",
            location = GeoLocation(41.501, -109.200),
            geometryPath = listOf(
                GeoLocation(41.450, -109.500),
                GeoLocation(41.501, -109.200),
                GeoLocation(41.580, -108.900)
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "OpenStreetMap Overpass",
            heightMeters = 0.5,
            lengthKm = 480.0,
            description = "Dual-track high frequency freight rail line creating noise disruption and steep ballast berm hazards."
        ),
        BarrierFeature(
            id = "bar_003",
            type = BarrierType.DAM,
            name = "Glen Canyon Dam",
            location = GeoLocation(36.937, -111.484),
            geometryPath = listOf(
                GeoLocation(36.937, -111.484)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "USACE National Inventory of Dams",
            heightMeters = 216.0,
            lengthKm = 0.48,
            description = "Major concrete arch dam blocking Colorado River aquatic organism passage and altered sediment hydrological regimes."
        ),
        BarrierFeature(
            id = "bar_004",
            type = BarrierType.FENCE,
            name = "Pinedale Anticline High-Tensile Range Boundary Fence",
            location = GeoLocation(42.700, -109.750),
            geometryPath = listOf(
                GeoLocation(42.650, -109.800),
                GeoLocation(42.700, -109.750),
                GeoLocation(42.750, -109.700)
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "USGS GAP / BLM Range Data",
            heightMeters = 1.8,
            lengthKm = 65.0,
            description = "Woven wire 6-strand barbed fence exceeding pronghorn crawl height, forcing dangerous herd concentration along fence lines."
        ),
        BarrierFeature(
            id = "bar_005",
            type = BarrierType.DAM,
            name = "Bonneville Lock and Dam",
            location = GeoLocation(45.644, -121.941),
            geometryPath = listOf(
                GeoLocation(45.644, -121.941)
            ),
            impactLevel = ImpactLevel.MODERATE,
            source = "USACE National Inventory of Dams",
            heightMeters = 60.0,
            lengthKm = 0.8,
            description = "Columbia River dam structure equipped with fish ladders but still restricting juvenile salmonid downstream passage."
        ),
        BarrierFeature(
            id = "bar_006",
            type = BarrierType.URBAN_WALL,
            name = "Los Angeles Metropolitan Perimeter Soundwall Infrastructure",
            location = GeoLocation(34.134, -118.321),
            geometryPath = listOf(
                GeoLocation(34.120, -118.340),
                GeoLocation(34.134, -118.321),
                GeoLocation(34.150, -118.300)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "Caltrans GIS",
            heightMeters = 4.5,
            lengthKm = 35.0,
            description = "Solid concrete soundwall perimeter completely blocking terrestrial mammal passage between urban parklands."
        )
    )
}
