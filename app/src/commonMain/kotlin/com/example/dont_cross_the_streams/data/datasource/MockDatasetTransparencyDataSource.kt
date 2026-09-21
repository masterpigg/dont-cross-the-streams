package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.AuthType
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo

object MockDatasetTransparencyDataSource {
    val dataSources = listOf(
        DataSourceInfo(
            id = "usdot_wcpp",
            name = "USDOT FHWA Wildlife Crossings Pilot Program (WCPP)",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "Federal Highway Administration Wildlife Crossings Pilot Program tracking federally funded wildlife overpasses, underpasses, directional fencing, and eco-culverts under the Bipartisan Infrastructure Law.",
            baseURL = "https://data-usdot.opendata.arcgis.com/",
            keyEndpoints = listOf(
                "datasets/wcpp-grants",
                "features/wildlife-crossings",
                "rest/services/wcpp"
            ),
            documentationUrl = "https://data-usdot.opendata.arcgis.com/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Locations of federally funded wildlife overpasses, underpasses, directional fencing, and eco-culverts under the Bipartisan Infrastructure Law."
        ),
        DataSourceInfo(
            id = "caltrans_crossings",
            name = "Caltrans Wildlife Crossing & Mitigation GIS API",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "California Department of Transportation GIS database tracking state wildlife crossings, overpasses, underpasses, and animal exclusion fencing across California highways.",
            baseURL = "https://gisdata-caltrans.opendata.arcgis.com/",
            keyEndpoints = listOf(
                "datasets/wildlife-crossings",
                "features/mitigation-structures",
                "rest/services/caltrans_wildlife"
            ),
            documentationUrl = "https://gisdata-caltrans.opendata.arcgis.com/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "State wildlife crossings including Wallis Annenberg Wildlife Crossing at Liberty Canyon and CA-17 cougar underpasses."
        ),
        DataSourceInfo(
            id = "cdot_crossings",
            name = "CDOT Wildlife Mitigations & Overpasses API",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "Colorado Department of Transportation GIS layer recording wildlife overpass locations, underpasses, radar detection zones, and fencing corridors across Colorado Rocky Mountain passes.",
            baseURL = "https://data-cdot.opendata.arcgis.com/",
            keyEndpoints = listOf(
                "datasets/wildlife-mitigation",
                "features/overpasses",
                "rest/services/cdot_wildlife"
            ),
            documentationUrl = "https://data-cdot.opendata.arcgis.com/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Colorado highway overpasses (I-70 Vail Pass, SH-9 Kremmling) and wildlife collision reduction structures."
        ),
        DataSourceInfo(
            id = "wsdot_fish_wildlife",
            name = "WSDOT Fish Passage Barrier & Wildlife Crossing API",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "Washington State Department of Transportation spatial services for salmon fish passage culvert removals, fish ladders, and highway wildlife overpass structures.",
            baseURL = "https://gisdata-wsdot.opendata.arcgis.com/",
            keyEndpoints = listOf(
                "datasets/fish-passage-barriers",
                "features/snoqualmie-overpass",
                "rest/services/wsdot_fish_wildlife"
            ),
            documentationUrl = "https://gisdata-wsdot.opendata.arcgis.com/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Washington state fish passage culvert removals, salmon ladders, and I-90 Snoqualmie Pass overpass structures."
        ),
        DataSourceInfo(
            id = "mdc_wildlife",
            name = "MDC (Missouri Department of Conservation Wildlife Inventory)",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "Statewide wildlife census, elk restoration tracking, and aquatic biodiversity surveys managed by the Missouri Department of Conservation.",
            baseURL = "https://mdc.mo.gov/data-api/",
            keyEndpoints = listOf(
                "wildlife/sightings",
                "elk/telemetry",
                "aquatic/fish-count"
            ),
            documentationUrl = "https://mdc.mo.gov/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Authoritative Missouri state species occurrence, elk tracking, and black bear range monitoring."
        ),
        DataSourceInfo(
            id = "modot_wvc",
            name = "MoDOT Wildlife-Vehicle Crash Register",
            category = DataSourceCategory.ANIMAL_COLLISION,
            description = "Missouri Department of Transportation crash and carcass clearance database covering interstates I-70, I-44, and state highways.",
            baseURL = "https://www.modot.org/api/safety/",
            keyEndpoints = listOf(
                "crashes/wvc",
                "carcass/milepost",
                "corridors/hotspots"
            ),
            documentationUrl = "https://data-modot.opendata.arcgis.com/",
            authType = AuthType.DATASET_DOWNLOAD,
            keyUtility = "Pinpoint highway milepost collision hotspots across Missouri highway networks."
        ),
        DataSourceInfo(
            id = "gbif",
            name = "GBIF (Global Biodiversity Information Facility)",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "An international network and data infrastructure funded by the world's governments aimed at providing open access to data about all types of life on Earth.",
            baseURL = "https://api.gbif.org/v1/",
            keyEndpoints = listOf(
                "occurrence/search",
                "species/match",
                "occurrence/counts"
            ),
            documentationUrl = "https://techdocs.gbif.org/en/openapi/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Global geo-referenced species occurrence records and taxonomic verification across global research institutions."
        ),
        DataSourceInfo(
            id = "movebank",
            name = "Movebank Animal Tracking Database",
            category = DataSourceCategory.TELEMETRY_MOVEMENT,
            description = "A free, online database hosted by the Max Planck Institute of Animal Behavior to help animal tracking researchers manage, share, analyze, and archive movement data.",
            baseURL = "https://www.movebank.org/movebank/service/public/",
            keyEndpoints = listOf(
                "json?entity_type=study",
                "json?entity_type=individual",
                "json?entity_type=event"
            ),
            documentationUrl = "https://github.com/movebank/movebank-api-doc",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "High-precision GPS telemetry collar and sensor movement pathways for migratory mammals and birds."
        ),
        DataSourceInfo(
            id = "ebird",
            name = "eBird API (Cornell Lab of Ornithology)",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "The world's largest biodiversity-related citizen science project, gathering avian observation data to track bird migration corridors and population trends.",
            baseURL = "https://api.ebird.org/v2/",
            keyEndpoints = listOf(
                "data/obs/geo/recent",
                "data/obs/recent/historic",
                "ref/taxonomy/ebird"
            ),
            documentationUrl = "https://documenter.getpostman.com/view/664302/S1ENwy59",
            authType = AuthType.API_KEY,
            keyUtility = "Real-time avian migration density, recent bird sightings, and species distribution along migratory flyways."
        ),
        DataSourceInfo(
            id = "inaturalist",
            name = "iNaturalist Open API",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "A joint initiative of the California Academy of Sciences and National Geographic, hosting community-contributed research-grade wildlife observations.",
            baseURL = "https://api.inaturalist.org/v1/",
            keyEndpoints = listOf(
                "observations",
                "taxa",
                "observations/histogram"
            ),
            documentationUrl = "https://api.inaturalist.org/v1/docs/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Geo-tagged photo evidence and crowd-sourced research-grade observations for all animal and plant taxa."
        ),
        DataSourceInfo(
            id = "usfws_ipac",
            name = "USFWS IPaC (Information for Planning and Consultation)",
            category = DataSourceCategory.ECOLOGICAL_SPECIES,
            description = "U.S. Fish & Wildlife Service project planning tool providing critical habitat designations and threatened/endangered species listings.",
            baseURL = "https://ipac.ecosphere.fws.gov/location/api/",
            keyEndpoints = listOf(
                "species/list",
                "crithat/geojson",
                "project/assess"
            ),
            documentationUrl = "https://ecos.fws.gov/ecp/pullwebservices",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Identification of federally listed threatened and endangered species habitats intersecting infrastructure projects."
        ),
        DataSourceInfo(
            id = "usgs_gap",
            name = "USGS GAP (Core Science Analytics & Synthesis)",
            category = DataSourceCategory.ECOLOGICAL_SPECIES,
            description = "U.S. Geological Survey Gap Analysis Project mapping national land cover, species habitat distribution models, and protected areas.",
            baseURL = "https://www.usgs.gov/core-science-systems/science-analytics-and-synthesis/gap/",
            keyEndpoints = listOf(
                "padus/wms",
                "species/habitat/model",
                "landcover/wfs"
            ),
            documentationUrl = "https://www.sciencebase.gov/catalog/",
            authType = AuthType.DATASET_DOWNLOAD,
            keyUtility = "High-resolution habitat suitability models and protected land status for ecological connectivity analysis."
        ),
        DataSourceInfo(
            id = "natureserve",
            name = "NatureServe Explorer API",
            category = DataSourceCategory.ECOLOGICAL_SPECIES,
            description = "Authoritative biodiversity data network providing ecological conservation status ranks (G-ranks/S-ranks) for North American species.",
            baseURL = "https://explorer.natureserve.org/api/data/",
            keyEndpoints = listOf(
                "taxon/search",
                "taxon/guid/{guid}",
                "location/species"
            ),
            documentationUrl = "https://explorer.natureserve.org/api/docs/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Global and state conservation risk ranks (Imperiled, Endangered, Vulnerable) for risk matrix weighting."
        ),
        DataSourceInfo(
            id = "faa_nwsd",
            name = "FAA National Wildlife Strike Database",
            category = DataSourceCategory.ANIMAL_COLLISION,
            description = "Federal Aviation Administration database recording civil and military aircraft collisions with wildlife since 1990.",
            baseURL = "https://wildlife.faa.gov/home/",
            keyEndpoints = listOf(
                "api/strikes/search",
                "api/strikes/export/csv",
                "api/summary/species"
            ),
            documentationUrl = "https://qa-wildlife.faa.gov/api/swagger/v1/swagger.json",
            authType = AuthType.DATASET_DOWNLOAD,
            keyUtility = "Avian and bat strike hazard records surrounding airports and low-altitude flight corridors."
        ),
        DataSourceInfo(
            id = "state_dot_wvc",
            name = "State DOT Wildlife-Vehicle Collision Records",
            category = DataSourceCategory.ANIMAL_COLLISION,
            description = "Aggregated State Department of Transportation crash registers and carcass clearance reports along highway corridors.",
            baseURL = "https://transafety.dot.gov/wvc/api/",
            keyEndpoints = listOf(
                "reports/corridor",
                "hotspots/geojson",
                "carcass/summary"
            ),
            documentationUrl = "https://data-modot.opendata.arcgis.com/",
            authType = AuthType.DATASET_DOWNLOAD,
            keyUtility = "Highway wildlife casualty counts, localized milepost collision hotspots, and fencing prioritization metrics."
        ),
        DataSourceInfo(
            id = "nhtsa_fars",
            name = "NHTSA FARS (Fatality Analysis Reporting System)",
            category = DataSourceCategory.ANIMAL_COLLISION,
            description = "National Highway Traffic Safety Administration census of fatal motor vehicle crashes involving animal strikes.",
            baseURL = "https://crashviewer.nhtsa.dot.gov/CrashAPI/",
            keyEndpoints = listOf(
                "crashes/GetCrashesByLocation",
                "crashes/GetCaseDetails",
                "crashes/GetVehicleDetails"
            ),
            documentationUrl = "https://crashviewer.nhtsa.dot.gov/CrashAPI",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Severe and fatal animal-vehicle accident data for corridor safety matrix calculations."
        ),
        DataSourceInfo(
            id = "us_census",
            name = "US Census Bureau TIGER/Line & Demographic API",
            category = DataSourceCategory.HUMAN_FOOTPRINT,
            description = "U.S. Census Bureau demographic and geographic datasets defining urban clusters, population density, and housing expansion.",
            baseURL = "https://api.census.gov/data/",
            keyEndpoints = listOf(
                "2020/dec/dhc?get=NAME,P1_001N&for=blockgroup:*",
                "geo/tiger"
            ),
            documentationUrl = "https://www.census.gov/data/developers/guidance/api-user-guide.html",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Grid-level human population density calculation for wildlife displacement risk mapping."
        ),
        DataSourceInfo(
            id = "nasa_human_footprint",
            name = "NASA SEDAC Human Footprint & Influence Index",
            category = DataSourceCategory.HUMAN_FOOTPRINT,
            description = "NASA Socioeconomic Data and Applications Center dataset measuring human environmental impact based on built infrastructure, lighting, and access.",
            baseURL = "https://sedac.ciesin.columbia.edu/data/set/wildareas-v3-2009-human-footprint/",
            keyEndpoints = listOf(
                "wms/sedac",
                "api/grid/value"
            ),
            documentationUrl = "https://sedac.ciesin.columbia.edu/",
            authType = AuthType.DATASET_DOWNLOAD,
            keyUtility = "Global Human Footprint Index (0-100) determining habitat fragmentation and urban encroachment levels."
        ),
        DataSourceInfo(
            id = "usace_dams",
            name = "USACE National Inventory of Dams (NID)",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "U.S. Army Corps of Engineers comprehensive inventory of over 90,000 dams across the United States.",
            baseURL = "https://nid.sec.usace.army.mil/api/",
            keyEndpoints = listOf(
                "dams/geo",
                "dams/summary",
                "dams/{nidId}"
            ),
            documentationUrl = "https://nid.sec.usace.army.mil/api/developer",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Hydroelectric and irrigation dam barrier locations restricting aquatic species movement and river connectivity."
        ),
        DataSourceInfo(
            id = "osm_overpass",
            name = "OpenStreetMap Overpass API",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "A read-only API that serves up custom-selected parts of the OpenStreetMap data such as motorways, railways, canals, and fences.",
            baseURL = "https://overpass-api.de/api/",
            keyEndpoints = listOf(
                "interpreter?data=[out:json];way[highway=motorway];out geom;"
            ),
            documentationUrl = "https://overpass-turbo.eu/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Real-time vector geographic boundaries for linear barrier features including fences, highways, and railways."
        ),
        DataSourceInfo(
            id = "idnr_wildlife",
            name = "Illinois Department of Natural Resources (IDNR)",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "Statewide wildlife census, herpetological surveys, and Snake Road seasonal migration tracking managed by IDNR.",
            baseURL = "https://dnr.illinois.gov/data-api/",
            keyEndpoints = listOf(
                "herpetology/snake-road",
                "wildlife/census",
                "corridors/crossings"
            ),
            documentationUrl = "https://clearinghouse.isgs.illinois.gov/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Authoritative Illinois reptile, amphibian, and game species occurrence datasets."
        ),
        DataSourceInfo(
            id = "wdfw_salmon",
            name = "Washington Department of Fish and Wildlife (WDFW)",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "Salmonid escapement counts, fish passage barrier inventories, and wildlife collision tracking across Washington state.",
            baseURL = "https://wdfw.wa.gov/api/v1/",
            keyEndpoints = listOf(
                "salmon/counts",
                "barriers/fish-passage",
                "wildlife/collisions"
            ),
            documentationUrl = "https://geodataservices.wdfw.wa.gov/arcgis/rest/services",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Statewide Washington salmon escapement telemetry, fish ladders, and ungulate crossing data."
        ),
        DataSourceInfo(
            id = "usace_nwd_salmon",
            name = "USACE NWD Salmon Passage API",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "U.S. Army Corps of Engineers Northwestern Division daily fish ladder passage counts and dam operations.",
            baseURL = "https://www.nwd.usace.army.mil/api/salmon/",
            keyEndpoints = listOf(
                "fishcounts/daily",
                "ladders/status",
                "dams/telemetry"
            ),
            documentationUrl = "https://www.nwd.usace.army.mil/api/salmon/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Real-time daily counts for Chinook, Sockeye, and Steelhead migrating through Columbia River and Ballard Locks fish ladders."
        )
    )
}
