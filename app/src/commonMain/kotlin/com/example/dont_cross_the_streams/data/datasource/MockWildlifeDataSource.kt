package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

object MockWildlifeDataSource {

    val occurrences = listOf(
        WildlifeOccurrence(
            id = "occ_mo_001",
            species = "Odocoileus virginianus",
            commonName = "White-tailed Deer",
            location = GeoLocation(38.9517, -92.3341),
            taxonGroup = "Mammals",
            observationCount = 48,
            timestamp = 1700000000000L,
            source = "MDC Wildlife Census",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_002",
            species = "Cervus canadensis",
            commonName = "American Elk (Reintroduced)",
            location = GeoLocation(37.0250, -91.1350),
            taxonGroup = "Mammals",
            observationCount = 35,
            timestamp = 1700000000000L,
            source = "MDC Elk Restoration Telemetry",
            conservationStatus = "Protected"
        ),
        WildlifeOccurrence(
            id = "occ_mo_003",
            species = "Ursus americanus",
            commonName = "American Black Bear",
            location = GeoLocation(37.4500, -91.3200),
            taxonGroup = "Mammals",
            observationCount = 12,
            timestamp = 1700000000000L,
            source = "MDC Bear Study / Movebank",
            conservationStatus = "Protected"
        ),
        WildlifeOccurrence(
            id = "occ_mo_004",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle",
            location = GeoLocation(38.5767, -92.1735),
            taxonGroup = "Birds",
            observationCount = 18,
            timestamp = 1700000000000L,
            source = "eBird / Missouri Audubon",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_005",
            species = "Polyodon spathula",
            commonName = "Paddlefish",
            location = GeoLocation(38.2012, -92.6238),
            taxonGroup = "Fish",
            observationCount = 64,
            timestamp = 1700000000000L,
            source = "MDC Fisheries Survey",
            conservationStatus = "Vulnerable"
        ),
        WildlifeOccurrence(
            id = "occ_mo_006",
            species = "Terrapene carolina triunguis",
            commonName = "Three-toed Box Turtle",
            location = GeoLocation(37.9514, -91.7713),
            taxonGroup = "Reptiles",
            observationCount = 9,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_007",
            species = "Canis latrans",
            commonName = "Coyote",
            location = GeoLocation(39.0997, -94.5786),
            taxonGroup = "Mammals",
            observationCount = 22,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_008",
            species = "Lynx rufus",
            commonName = "Bobcat",
            location = GeoLocation(37.2090, -93.2923),
            taxonGroup = "Mammals",
            observationCount = 7,
            timestamp = 1700000000000L,
            source = "GBIF",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_009",
            species = "Strix varia",
            commonName = "Barred Owl",
            location = GeoLocation(39.7084, -91.3584),
            taxonGroup = "Birds",
            observationCount = 14,
            timestamp = 1700000000000L,
            source = "eBird",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_010",
            species = "Odocoileus virginianus",
            commonName = "White-tailed Deer",
            location = GeoLocation(38.2839, -90.3843),
            taxonGroup = "Mammals",
            observationCount = 52,
            timestamp = 1700000000000L,
            source = "MoDOT WVC Record",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_011",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle Nesting Pair",
            location = GeoLocation(38.8000, -90.1800),
            taxonGroup = "Birds",
            observationCount = 6,
            timestamp = 1700000000000L,
            source = "eBird",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_012",
            species = "Lithobates catesbeianus",
            commonName = "American Bullfrog",
            location = GeoLocation(38.1500, -92.7000),
            taxonGroup = "Amphibians",
            observationCount = 28,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_001",
            species = "Odocoileus virginianus",
            commonName = "White-tailed Deer",
            location = GeoLocation(39.638, -105.897),
            taxonGroup = "Mammals",
            observationCount = 14,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_002",
            species = "Cervus canadensis",
            commonName = "Elk / Wapiti",
            location = GeoLocation(44.428, -110.588),
            taxonGroup = "Mammals",
            observationCount = 42,
            timestamp = 1700000000000L,
            source = "Movebank",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_003",
            species = "Antilocapra americana",
            commonName = "Pronghorn",
            location = GeoLocation(42.851, -109.859),
            taxonGroup = "Mammals",
            observationCount = 120,
            timestamp = 1700000000000L,
            source = "GBIF",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_004",
            species = "Puma concolor",
            commonName = "Cougar / Mountain Lion",
            location = GeoLocation(34.134, -118.321),
            taxonGroup = "Mammals",
            observationCount = 1,
            timestamp = 1700000000000L,
            source = "Movebank",
            conservationStatus = "Specially Protected"
        ),
        WildlifeOccurrence(
            id = "occ_005",
            species = "Ursus americanus",
            commonName = "American Black Bear",
            location = GeoLocation(35.611, -83.489),
            taxonGroup = "Mammals",
            observationCount = 3,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_006",
            species = "Ovis canadensis",
            commonName = "Bighorn Sheep",
            location = GeoLocation(40.342, -105.683),
            taxonGroup = "Mammals",
            observationCount = 18,
            timestamp = 1700000000000L,
            source = "GBIF",
            conservationStatus = "Vulnerable"
        ),
        WildlifeOccurrence(
            id = "occ_007",
            species = "Gopherus agassizii",
            commonName = "Mojave Desert Tortoise",
            location = GeoLocation(34.891, -115.542),
            taxonGroup = "Reptiles",
            observationCount = 2,
            timestamp = 1700000000000L,
            source = "USFWS IPaC",
            conservationStatus = "Threatened"
        ),
        WildlifeOccurrence(
            id = "occ_008",
            species = "Ursus arctos horribilis",
            commonName = "Grizzly Bear",
            location = GeoLocation(48.696, -113.718),
            taxonGroup = "Mammals",
            observationCount = 2,
            timestamp = 1700000000000L,
            source = "Movebank",
            conservationStatus = "Threatened"
        ),
        WildlifeOccurrence(
            id = "occ_009",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle",
            location = GeoLocation(46.872, -96.789),
            taxonGroup = "Birds",
            observationCount = 5,
            timestamp = 1700000000000L,
            source = "eBird",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_010",
            species = "Puma concolor coryi",
            commonName = "Florida Panther",
            location = GeoLocation(26.168, -81.352),
            taxonGroup = "Mammals",
            observationCount = 1,
            timestamp = 1700000000000L,
            source = "USFWS IPaC",
            conservationStatus = "Endangered"
        ),
        WildlifeOccurrence(
            id = "occ_011",
            species = "Oncorhynchus tshawytscha",
            commonName = "Chinook Salmon",
            location = GeoLocation(45.644, -121.941),
            taxonGroup = "Fish",
            observationCount = 85,
            timestamp = 1700000000000L,
            source = "NOAA Fisheries",
            conservationStatus = "Threatened"
        ),
        WildlifeOccurrence(
            id = "occ_012",
            species = "Salvelinus confluentus",
            commonName = "Bull Trout",
            location = GeoLocation(46.200, -117.100),
            taxonGroup = "Fish",
            observationCount = 12,
            timestamp = 1700000000000L,
            source = "USFWS",
            conservationStatus = "Vulnerable"
        ),
        WildlifeOccurrence(
            id = "occ_013",
            species = "Pseudacris regilla",
            commonName = "Pacific Tree Frog",
            location = GeoLocation(37.800, -119.500),
            taxonGroup = "Amphibians",
            observationCount = 30,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_il_001",
            species = "Agkistrodon piscivorus",
            commonName = "Western Cottonmouth",
            location = GeoLocation(37.5600, -89.4400),
            taxonGroup = "Reptiles",
            observationCount = 78,
            timestamp = 1700000000000L,
            source = "Illinois DNR / USFS Herpetology Census",
            conservationStatus = "Protected"
        ),
        WildlifeOccurrence(
            id = "occ_il_002",
            species = "Crotalus horridus",
            commonName = "Timber Rattlesnake",
            location = GeoLocation(37.5620, -89.4380),
            taxonGroup = "Reptiles",
            observationCount = 24,
            timestamp = 1700000000000L,
            source = "Illinois Natural History Survey",
            conservationStatus = "Threatened"
        ),
        WildlifeOccurrence(
            id = "occ_il_003",
            species = "Agkistrodon contortrix",
            commonName = "Eastern Copperhead",
            location = GeoLocation(37.5580, -89.4420),
            taxonGroup = "Reptiles",
            observationCount = 31,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_il_004",
            species = "Eurycea lucifuga",
            commonName = "Cave Salamander",
            location = GeoLocation(37.5650, -89.4350),
            taxonGroup = "Amphibians",
            observationCount = 45,
            timestamp = 1700000000000L,
            source = "Illinois DNR",
            conservationStatus = "Special Concern"
        ),
        WildlifeOccurrence(
            id = "occ_il_005",
            species = "Kinosternon subrubrum",
            commonName = "Mississippi Mud Turtle",
            location = GeoLocation(37.5550, -89.4450),
            taxonGroup = "Reptiles",
            observationCount = 19,
            timestamp = 1700000000000L,
            source = "iNaturalist",
            conservationStatus = "Endangered"
        ),
        WildlifeOccurrence(
            id = "occ_il_006",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle",
            location = GeoLocation(38.8680, -90.1530),
            taxonGroup = "Birds",
            observationCount = 38,
            timestamp = 1700000000000L,
            source = "eBird / Audubon Illinois",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_il_007",
            species = "Lontra canadensis",
            commonName = "North American River Otter",
            location = GeoLocation(38.6500, -89.9800),
            taxonGroup = "Mammals",
            observationCount = 14,
            timestamp = 1700000000000L,
            source = "Illinois DNR Wildlife Division",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_wa_001",
            species = "Oncorhynchus tshawytscha",
            commonName = "Chinook Salmon (King Salmon)",
            location = GeoLocation(47.6655, -122.3972),
            taxonGroup = "Fish",
            observationCount = 1420,
            timestamp = 1700000000000L,
            source = "WDFW / USACE NWD Salmon Passage API",
            conservationStatus = "Threatened"
        ),
        WildlifeOccurrence(
            id = "occ_wa_002",
            species = "Oncorhynchus nerka",
            commonName = "Sockeye Salmon",
            location = GeoLocation(47.6680, -122.4000),
            taxonGroup = "Fish",
            observationCount = 890,
            timestamp = 1700000000000L,
            source = "WDFW Salmon Passage API",
            conservationStatus = "Special Concern"
        ),
        WildlifeOccurrence(
            id = "occ_wa_003",
            species = "Oncorhynchus mykiss",
            commonName = "Steelhead Trout",
            location = GeoLocation(45.6440, -121.9410),
            taxonGroup = "Fish",
            observationCount = 620,
            timestamp = 1700000000000L,
            source = "NOAA Fisheries / WDFW",
            conservationStatus = "Threatened"
        ),
        WildlifeOccurrence(
            id = "occ_wa_004",
            species = "Orcinus orca",
            commonName = "Southern Resident Killer Whale",
            location = GeoLocation(47.6200, -122.4200),
            taxonGroup = "Mammals",
            observationCount = 18,
            timestamp = 1700000000000L,
            source = "NOAA Fisheries / Orca Network",
            conservationStatus = "Endangered"
        ),
        WildlifeOccurrence(
            id = "occ_wa_005",
            species = "Cervus canadensis roosevelti",
            commonName = "Roosevelt Elk",
            location = GeoLocation(47.3800, -121.4000),
            taxonGroup = "Mammals",
            observationCount = 64,
            timestamp = 1700000000000L,
            source = "WDFW Telemetry / Movebank",
            conservationStatus = "Protected"
        ),
        WildlifeOccurrence(
            id = "occ_wa_006",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle",
            location = GeoLocation(47.6700, -122.3900),
            taxonGroup = "Birds",
            observationCount = 29,
            timestamp = 1700000000000L,
            source = "eBird",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_wa_007",
            species = "Ursus arctos horribilis",
            commonName = "Grizzly Bear",
            location = GeoLocation(48.5000, -120.5000),
            taxonGroup = "Mammals",
            observationCount = 4,
            timestamp = 1700000000000L,
            source = "USFWS / WDFW Recovery Team",
            conservationStatus = "Threatened"
        )
    )

    val collisionHotspots = listOf(
        CollisionHotspot(
            id = "hotspot_mo_001",
            location = GeoLocation(38.9500, -92.1500),
            incidentCount = 340,
            primarySpeciesAffected = "White-tailed Deer",
            severity = CollisionSeverity.CRITICAL,
            source = "MoDOT WVC Database / MDC",
            highwayOrRouteName = "Interstate 70 Columbia-Wentzville Corridor",
            description = "Severe deer-vehicle collision cluster on 6-lane interstate bisecting prime forest agricultural transition land."
        ),
        CollisionHotspot(
            id = "hotspot_mo_002",
            location = GeoLocation(38.2200, -90.9800),
            incidentCount = 280,
            primarySpeciesAffected = "White-tailed Deer & Black Bear",
            severity = CollisionSeverity.CRITICAL,
            source = "MoDOT / Missouri State Highway Patrol",
            highwayOrRouteName = "Interstate 44 Ozark Expressway",
            description = "High density vehicle strike corridor in Ozark foothills with jersey barriers trapping wildlife."
        ),
        CollisionHotspot(
            id = "hotspot_mo_003",
            location = GeoLocation(39.1200, -92.4200),
            incidentCount = 145,
            primarySpeciesAffected = "White-tailed Deer & Coyote",
            severity = CollisionSeverity.HIGH,
            source = "MoDOT Safety GIS",
            highwayOrRouteName = "US Highway 63 Central Arterial",
            description = "High velocity 4-lane arterial road cutting across agricultural draws and river tributaries."
        ),
        CollisionHotspot(
            id = "hotspot_mo_004",
            location = GeoLocation(36.9800, -91.2200),
            incidentCount = 88,
            primarySpeciesAffected = "Reintroduced Elk & Black Bear",
            severity = CollisionSeverity.HIGH,
            source = "MDC / National Park Service",
            highwayOrRouteName = "US Highway 60 Ozark Scenic Byway",
            description = "Frequent collisions with expanding Ozark elk herds and black bears traversing Mark Twain National Forest."
        ),
        CollisionHotspot(
            id = "hotspot_mo_005",
            location = GeoLocation(38.7000, -91.4400),
            incidentCount = 62,
            primarySpeciesAffected = "Bald Eagle & Waterfowl",
            severity = CollisionSeverity.MODERATE,
            source = "USFWS / eBird Aviation Hazard",
            highwayOrRouteName = "Missouri River Bridge Crossings (I-70 / US-54)",
            description = "Avian collision risk zone where river flyways intersect high-speed highway bridges and utility cables."
        ),
        CollisionHotspot(
            id = "hotspot_il_001",
            location = GeoLocation(37.5600, -89.4400),
            incidentCount = 120,
            primarySpeciesAffected = "Cottonmouth, Rattlesnake & Salamanders",
            severity = CollisionSeverity.CRITICAL,
            source = "USFS / Illinois DNR Herpetological Survey",
            highwayOrRouteName = "LaRue Road 345 (Snake Road)",
            description = "Biannual mass reptile and amphibian road crossing between limestone bluffs and LaRue Swamp. Road closed seasonally by US Forest Service."
        ),
        CollisionHotspot(
            id = "hotspot_il_002",
            location = GeoLocation(38.8500, -89.9200),
            incidentCount = 310,
            primarySpeciesAffected = "White-tailed Deer & Small Mammals",
            severity = CollisionSeverity.CRITICAL,
            source = "IDOT Safety Data / Illinois DNR",
            highwayOrRouteName = "Interstate 55 Corridor",
            description = "High density vehicle collision corridor across agricultural fields and river basin woodlands."
        ),
        CollisionHotspot(
            id = "hotspot_il_003",
            location = GeoLocation(38.8680, -90.1530),
            incidentCount = 75,
            primarySpeciesAffected = "Bald Eagle & Migratory Waterfowl",
            severity = CollisionSeverity.MODERATE,
            source = "USACE / eBird Flying Hazard Data",
            highwayOrRouteName = "Melvin Price Locks & Dam Mississippi Crossing",
            description = "Avian flyway intersection with river locks, high-voltage powerlines, and bridge traffic."
        ),
        CollisionHotspot(
            id = "hotspot_wa_001",
            location = GeoLocation(47.6655, -122.3972),
            incidentCount = 95,
            primarySpeciesAffected = "Chinook & Sockeye Salmon",
            severity = CollisionSeverity.CRITICAL,
            source = "WDFW / USACE NWD",
            highwayOrRouteName = "Hiram M. Chittenden Locks (Ballard Locks)",
            description = "Critical aquatic passage bottleneck connecting Puget Sound to Lake Washington. Salmon runs traverse retrofitted fish ladder."
        ),
        CollisionHotspot(
            id = "hotspot_wa_002",
            location = GeoLocation(45.6440, -121.9410),
            incidentCount = 240,
            primarySpeciesAffected = "Chinook, Sockeye & Steelhead",
            severity = CollisionSeverity.CRITICAL,
            source = "USACE Northwestern Division / NOAA",
            highwayOrRouteName = "Bonneville Dam Columbia River Corridor",
            description = "Major hydroelectric barrier on Columbia River mainstem restricting upstream adult salmon and downstream smolt passage."
        ),
        CollisionHotspot(
            id = "hotspot_wa_003",
            location = GeoLocation(47.3800, -121.4000),
            incidentCount = 190,
            primarySpeciesAffected = "Roosevelt Elk & Black Bear",
            severity = CollisionSeverity.HIGH,
            source = "WSDOT Wildlife Incident Database",
            highwayOrRouteName = "Interstate 90 Snoqualmie Pass Corridor",
            description = "High-speed 6-lane interstate cutting through Cascade wildlife migration routes, mitigated by landmark overpass bridge."
        ),
        CollisionHotspot(
            id = "hotspot_wa_004",
            location = GeoLocation(48.0800, -123.5500),
            incidentCount = 15,
            primarySpeciesAffected = "Chinook & Coho Salmon",
            severity = CollisionSeverity.LOW,
            source = "National Park Service / Lower Elwha Klallam Tribe",
            highwayOrRouteName = "Elwha River Restored Migration Zone",
            description = "Former dam barrier zone fully restored after historical dam removal, restoring free salmon migration."
        ),
        CollisionHotspot(
            id = "hotspot_001",
            location = GeoLocation(39.638, -105.897),
            incidentCount = 184,
            primarySpeciesAffected = "Mule Deer & Elk",
            severity = CollisionSeverity.CRITICAL,
            source = "Colorado DOT WVC",
            highwayOrRouteName = "I-70 Vail Pass Corridor",
            description = "High density 4-lane interstate bisecting subalpine winter migration corridor resulting in severe animal strikes."
        ),
        CollisionHotspot(
            id = "hotspot_002",
            location = GeoLocation(44.381, -110.822),
            incidentCount = 126,
            primarySpeciesAffected = "Elk & Bison",
            severity = CollisionSeverity.HIGH,
            source = "Wyoming DOT WVC",
            highwayOrRouteName = "US Highway 191 / West Yellowstone",
            description = "Two-lane high-speed arterial roadway prone to nighttime collisions during winter range migrations."
        ),
        CollisionHotspot(
            id = "hotspot_003",
            location = GeoLocation(42.851, -109.859),
            incidentCount = 210,
            primarySpeciesAffected = "Pronghorn",
            severity = CollisionSeverity.CRITICAL,
            source = "Wyoming Game & Fish / WVC Database",
            highwayOrRouteName = "US Highway 191 Trappers Point",
            description = "Historical bottleneck pinch point crossing over 5,000 migrating pronghorn annually."
        ),
        CollisionHotspot(
            id = "hotspot_004",
            location = GeoLocation(26.168, -81.352),
            incidentCount = 38,
            primarySpeciesAffected = "Florida Panther",
            severity = CollisionSeverity.CRITICAL,
            source = "Florida Fish and Wildlife Conservation Commission",
            highwayOrRouteName = "State Road 29 / Alligator Alley",
            description = "High mortality stretch bisecting Big Cypress National Preserve panther territory."
        ),
        CollisionHotspot(
            id = "hotspot_005",
            location = GeoLocation(34.134, -118.321),
            incidentCount = 12,
            primarySpeciesAffected = "Mountain Lion & Mule Deer",
            severity = CollisionSeverity.MODERATE,
            source = "Caltrans / NHTSA FARS",
            highwayOrRouteName = "US 101 Liberty Canyon Corridor",
            description = "10-lane freeway separating Santa Monica Mountains population causing severe genetic isolation."
        ),
        CollisionHotspot(
            id = "hotspot_006",
            location = GeoLocation(35.611, -83.489),
            incidentCount = 95,
            primarySpeciesAffected = "Black Bear & White-tailed Deer",
            severity = CollisionSeverity.HIGH,
            source = "North Carolina DOT / FAA NWSD",
            highwayOrRouteName = "I-40 Pigeon River Gorge",
            description = "Steep terrain highway causing wildlife trapped inside concrete jersey barriers."
        )
    )
}
