package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionSeverity
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

object MockWildlifeDataSource {

    val occurrences = listOf(
        // Missouri Wildlife Occurrences
        WildlifeOccurrence(
            id = "occ_mo_001",
            species = "Odocoileus virginianus",
            commonName = "White-tailed Deer",
            location = GeoLocation(38.9517, -92.3341), // Columbia, MO
            taxonGroup = "Mammals",
            observationCount = 48,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            source = "MDC Wildlife Census",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_002",
            species = "Cervus canadensis",
            commonName = "American Elk (Reintroduced)",
            location = GeoLocation(37.0250, -91.1350), // Peck Ranch Conservation Area / Ozarks
            taxonGroup = "Mammals",
            observationCount = 35,
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            source = "MDC Elk Restoration Telemetry",
            conservationStatus = "Protected"
        ),
        WildlifeOccurrence(
            id = "occ_mo_003",
            species = "Ursus americanus",
            commonName = "American Black Bear",
            location = GeoLocation(37.4500, -91.3200), // Mark Twain National Forest, MO
            taxonGroup = "Mammals",
            observationCount = 12,
            timestamp = System.currentTimeMillis() - 86400000L * 3,
            source = "MDC Bear Study / Movebank",
            conservationStatus = "Protected"
        ),
        WildlifeOccurrence(
            id = "occ_mo_004",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle",
            location = GeoLocation(38.5767, -92.1735), // Missouri River near Jefferson City, MO
            taxonGroup = "Birds",
            observationCount = 18,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            source = "eBird / Missouri Audubon",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_005",
            species = "Polyodon spathula",
            commonName = "Paddlefish",
            location = GeoLocation(38.2012, -92.6238), // Osage River below Bagnell Dam, MO
            taxonGroup = "Fish",
            observationCount = 64,
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            source = "MDC Fisheries Survey",
            conservationStatus = "Vulnerable"
        ),
        WildlifeOccurrence(
            id = "occ_mo_006",
            species = "Terrapene carolina triunguis",
            commonName = "Three-toed Box Turtle",
            location = GeoLocation(37.9514, -91.7713), // Rolla, MO
            taxonGroup = "Reptiles",
            observationCount = 9,
            timestamp = System.currentTimeMillis() - 86400000L * 4,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_007",
            species = "Canis latrans",
            commonName = "Coyote",
            location = GeoLocation(39.0997, -94.5786), // Kansas City Prairie Fringe, MO
            taxonGroup = "Mammals",
            observationCount = 22,
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_008",
            species = "Lynx rufus",
            commonName = "Bobcat",
            location = GeoLocation(37.2090, -93.2923), // Springfield Greenbelt, MO
            taxonGroup = "Mammals",
            observationCount = 7,
            timestamp = System.currentTimeMillis() - 86400000L * 5,
            source = "GBIF",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_009",
            species = "Strix varia",
            commonName = "Barred Owl",
            location = GeoLocation(39.7084, -91.3584), // Hannibal / Mississippi River Bluffs, MO
            taxonGroup = "Birds",
            observationCount = 14,
            timestamp = System.currentTimeMillis() - 86400000L * 3,
            source = "eBird",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_010",
            species = "Odocoileus virginianus",
            commonName = "White-tailed Deer",
            location = GeoLocation(38.2839, -90.3843), // Pevely / I-55 Corridor, MO
            taxonGroup = "Mammals",
            observationCount = 52,
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            source = "MoDOT WVC Record",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_011",
            species = "Haliaeetus leucocephalus",
            commonName = "Bald Eagle Nesting Pair",
            location = GeoLocation(38.8000, -90.1800), // St. Louis Confluence State Park, MO
            taxonGroup = "Birds",
            observationCount = 6,
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            source = "eBird",
            conservationStatus = "Least Concern"
        ),
        WildlifeOccurrence(
            id = "occ_mo_012",
            species = "Lithobates catesbeianus",
            commonName = "American Bullfrog",
            location = GeoLocation(38.1500, -92.7000), // Lake of the Ozarks Shoreline, MO
            taxonGroup = "Amphibians",
            observationCount = 28,
            timestamp = System.currentTimeMillis() - 86400000L * 3,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        ),

        // National Wildlife Occurrences
        WildlifeOccurrence(
            id = "occ_001",
            species = "Odocoileus virginianus",
            commonName = "White-tailed Deer",
            location = GeoLocation(39.638, -105.897),
            taxonGroup = "Mammals",
            observationCount = 14,
            timestamp = System.currentTimeMillis() - 86400000L * 2,
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
            timestamp = System.currentTimeMillis() - 86400000L * 1,
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
            timestamp = System.currentTimeMillis() - 86400000L * 3,
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
            timestamp = System.currentTimeMillis() - 86400000L * 5,
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
            timestamp = System.currentTimeMillis() - 86400000L * 4,
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
            timestamp = System.currentTimeMillis() - 86400000L * 2,
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
            timestamp = System.currentTimeMillis() - 86400000L * 7,
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
            timestamp = System.currentTimeMillis() - 86400000L * 1,
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
            timestamp = System.currentTimeMillis() - 86400000L * 2,
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
            timestamp = System.currentTimeMillis() - 86400000L * 6,
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
            timestamp = System.currentTimeMillis() - 86400000L * 1,
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
            timestamp = System.currentTimeMillis() - 86400000L * 3,
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
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            source = "iNaturalist",
            conservationStatus = "Least Concern"
        )
    )

    val collisionHotspots = listOf(
        // Missouri Collision Hotspots
        CollisionHotspot(
            id = "hotspot_mo_001",
            location = GeoLocation(38.9500, -92.1500), // Columbia to Wentzville I-70
            incidentCount = 340,
            primarySpeciesAffected = "White-tailed Deer",
            severity = CollisionSeverity.CRITICAL,
            source = "MoDOT WVC Database / MDC",
            highwayOrRouteName = "Interstate 70 Columbia-Wentzville Corridor",
            description = "Severe deer-vehicle collision cluster on 6-lane interstate bisecting prime forest agricultural transition land."
        ),
        CollisionHotspot(
            id = "hotspot_mo_002",
            location = GeoLocation(38.2200, -90.9800), // I-44 Eureka to Rolla & Springfield
            incidentCount = 280,
            primarySpeciesAffected = "White-tailed Deer & Black Bear",
            severity = CollisionSeverity.CRITICAL,
            source = "MoDOT / Missouri State Highway Patrol",
            highwayOrRouteName = "Interstate 44 Ozark Expressway",
            description = "High density vehicle strike corridor in Ozark foothills with jersey barriers trapping wildlife."
        ),
        CollisionHotspot(
            id = "hotspot_mo_003",
            location = GeoLocation(39.1200, -92.4200), // US-63 Moberly to Jefferson City
            incidentCount = 145,
            primarySpeciesAffected = "White-tailed Deer & Coyote",
            severity = CollisionSeverity.HIGH,
            source = "MoDOT Safety GIS",
            highwayOrRouteName = "US Highway 63 Central Arterial",
            description = "High velocity 4-lane arterial road cutting across agricultural draws and river tributaries."
        ),
        CollisionHotspot(
            id = "hotspot_mo_004",
            location = GeoLocation(36.9800, -91.2200), // US-60 Ozark Scenic Byway
            incidentCount = 88,
            primarySpeciesAffected = "Reintroduced Elk & Black Bear",
            severity = CollisionSeverity.HIGH,
            source = "MDC / National Park Service",
            highwayOrRouteName = "US Highway 60 Ozark Scenic Byway",
            description = "Frequent collisions with expanding Ozark elk herds and black bears traversing Mark Twain National Forest."
        ),
        CollisionHotspot(
            id = "hotspot_mo_005",
            location = GeoLocation(38.7000, -91.4400), // Missouri River Bridge Corridor
            incidentCount = 62,
            primarySpeciesAffected = "Bald Eagle & Waterfowl",
            severity = CollisionSeverity.MODERATE,
            source = "USFWS / eBird Aviation Hazard",
            highwayOrRouteName = "Missouri River Bridge Crossings (I-70 / US-54)",
            description = "Avian collision risk zone where river flyways intersect high-speed highway bridges and utility cables."
        ),

        // National Collision Hotspots
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
