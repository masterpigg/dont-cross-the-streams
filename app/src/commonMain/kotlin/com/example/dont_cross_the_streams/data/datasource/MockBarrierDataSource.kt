package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.BarrierType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.ImpactLevel

object MockBarrierDataSource {

    val barrierFeatures = listOf(
        BarrierFeature(
            id = "bar_mo_001",
            type = BarrierType.HIGHWAY,
            name = "Interstate 70 Missouri East-West Divide",
            location = GeoLocation(38.9500, -92.3300),
            geometryPath = listOf(
                GeoLocation(39.0500, -94.3000),
                GeoLocation(38.9500, -92.3300),
                GeoLocation(38.7000, -90.5000)
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
                GeoLocation(38.5000, -90.4000),
                GeoLocation(37.9514, -91.7713),
                GeoLocation(37.2090, -93.2923)
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
                GeoLocation(39.7084, -91.3584),
                GeoLocation(38.8000, -90.2000),
                GeoLocation(38.2839, -90.3843)
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "Federal Railroad Administration / OpenStreetMap",
            heightMeters = 0.6,
            lengthKm = 280.0,
            description = "High-traffic dual-track rail freight line creating acoustic noise barriers and steep ballast embankment hazards along river corridors."
        ),
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
        ),
        BarrierFeature(
            id = "bar_il_001",
            type = BarrierType.HIGHWAY,
            name = "LaRue Road 345 (Snake Road Seasonal Closure)",
            location = GeoLocation(37.5600, -89.4400),
            geometryPath = listOf(
                GeoLocation(37.5750, -89.4380),
                GeoLocation(37.5600, -89.4400),
                GeoLocation(37.5450, -89.4420)
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "US Forest Service Shawnee NF / IDNR",
            heightMeters = 0.2,
            lengthKm = 4.3,
            description = "2.5-mile forest gravel road bisecting limestone bluffs and LaRue Swamp. Closed for 2 months each spring and autumn by USFS for mass reptile and amphibian migration."
        ),
        BarrierFeature(
            id = "bar_il_002",
            type = BarrierType.HIGHWAY,
            name = "Interstate 55 Illinois Central Expressway",
            location = GeoLocation(38.8500, -89.9200),
            geometryPath = listOf(
                GeoLocation(38.6200, -90.1500),
                GeoLocation(38.8500, -89.9200),
                GeoLocation(39.8000, -89.6500)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "IDOT GIS / OpenStreetMap",
            heightMeters = 1.4,
            lengthKm = 150.0,
            description = "High-volume 6-lane interstate with continuous concrete jersey barriers dividing Metro East and central Illinois wildlife habitats."
        ),
        BarrierFeature(
            id = "bar_il_003",
            type = BarrierType.DAM,
            name = "Melvin Price Locks & Dam (Mississippi River Dam 26)",
            location = GeoLocation(38.8680, -90.1530),
            geometryPath = listOf(
                GeoLocation(38.8680, -90.1530)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "USACE National Inventory of Dams",
            heightMeters = 24.0,
            lengthKm = 0.35,
            description = "Major navigation lock and dam structure on the upper Mississippi River, altering river flow velocity and impeding aquatic species movement."
        ),
        BarrierFeature(
            id = "bar_wa_001",
            type = BarrierType.CANAL,
            name = "Hiram M. Chittenden Locks (Ballard Locks)",
            location = GeoLocation(47.6655, -122.3972),
            geometryPath = listOf(
                GeoLocation(47.6655, -122.3972)
            ),
            impactLevel = ImpactLevel.HIGH,
            source = "USACE Seattle District / WDFW",
            heightMeters = 8.0,
            lengthKm = 0.15,
            description = "Navigation lock complex connecting marine Puget Sound to freshwater Lake Washington. Salmon migration relies on a retrofitted 21-weir fish ladder."
        ),
        BarrierFeature(
            id = "bar_wa_002",
            type = BarrierType.DAM,
            name = "Grand Coulee Dam (Upper Columbia Blockage)",
            location = GeoLocation(47.9570, -118.9810),
            geometryPath = listOf(
                GeoLocation(47.9570, -118.9810)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "US Bureau of Reclamation / USACE NID",
            heightMeters = 168.0,
            lengthKm = 1.58,
            description = "Massive concrete gravity dam permanently blocking over 1,000 miles of historical upper Columbia River salmon spawning grounds with no fish passage."
        ),
        BarrierFeature(
            id = "bar_wa_003",
            type = BarrierType.HIGHWAY,
            name = "Interstate 90 Snoqualmie Pass Highway",
            location = GeoLocation(47.3800, -121.4000),
            geometryPath = listOf(
                GeoLocation(47.4800, -121.7800),
                GeoLocation(47.3800, -121.4000),
                GeoLocation(47.2000, -120.9800)
            ),
            impactLevel = ImpactLevel.SEVERE,
            source = "WSDOT GIS / OpenStreetMap",
            heightMeters = 1.2,
            lengthKm = 240.0,
            description = "High-volume 6-lane interstate dividing North and South Cascade mountain ecosystems; now mitigated by new wildlife overpass bridges."
        ),
        BarrierFeature(
            id = "bar_wcpp_001",
            type = BarrierType.HIGHWAY,
            name = "I-90 & US-93 Federal Wildlife Crossing Pilot Corridor (WCPP)",
            location = GeoLocation(46.8700, -114.0000),
            geometryPath = listOf(
                GeoLocation(46.9000, -114.1000),
                GeoLocation(46.8700, -114.0000),
                GeoLocation(46.8400, -113.9000)
            ),
            impactLevel = ImpactLevel.MODERATE,
            source = "USDOT FHWA Wildlife Crossings Pilot Program (WCPP)",
            heightMeters = 8.5,
            lengthKm = 12.0,
            description = "Federally funded wildlife overpass bridges, directional fencing, and eco-culverts constructed under the USDOT Bipartisan Infrastructure Law WCPP grant."
        ),
        BarrierFeature(
            id = "bar_caltrans_001",
            type = BarrierType.HIGHWAY,
            name = "Wallis Annenberg Wildlife Crossing at Liberty Canyon (US-101)",
            location = GeoLocation(34.1350, -118.7020),
            geometryPath = listOf(
                GeoLocation(34.1350, -118.7020)
            ),
            impactLevel = ImpactLevel.LOW,
            source = "Caltrans Wildlife Crossing & Mitigation GIS API",
            heightMeters = 12.0,
            lengthKm = 0.12,
            description = "World's largest vegetated wildlife overpass bridging 10 lanes of US-101 highway in Agoura Hills to reconnect cougar and bobcat habitat."
        ),
        BarrierFeature(
            id = "bar_cdot_001",
            type = BarrierType.HIGHWAY,
            name = "State Highway 9 Kremmling & I-70 Vail Pass Wildlife Overpasses",
            location = GeoLocation(39.9000, -106.3800),
            geometryPath = listOf(
                GeoLocation(39.9200, -106.4000),
                GeoLocation(39.9000, -106.3800)
            ),
            impactLevel = ImpactLevel.MODERATE,
            source = "CDOT Wildlife Mitigations & Overpasses API",
            heightMeters = 7.0,
            lengthKm = 18.0,
            description = "Dedicated highway overpass bridges and 8-foot directional wildlife fencing reducing deer and elk vehicle collisions by over 90% across SH-9 and I-70."
        ),
        BarrierFeature(
            id = "bar_wsdot_001",
            type = BarrierType.HIGHWAY,
            name = "I-90 Snoqualmie Pass Wildlife Overpass & Resort Creek Fish Ladder",
            location = GeoLocation(47.3100, -121.2800),
            geometryPath = listOf(
                GeoLocation(47.3100, -121.2800)
            ),
            impactLevel = ImpactLevel.LOW,
            source = "WSDOT Fish Passage Barrier & Wildlife Crossing API",
            heightMeters = 10.0,
            lengthKm = 0.08,
            description = "Landmark 66-foot wide vegetated overpass bridge over 6 lanes of I-90 in Snoqualmie Pass integrated with salmon culvert fish ladders."
        )
    )
}
