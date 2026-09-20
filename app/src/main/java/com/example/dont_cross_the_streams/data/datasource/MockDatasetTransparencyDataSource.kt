package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.AuthType
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo

object MockDatasetTransparencyDataSource {
    val dataSources = listOf(
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
            documentationUrl = "https://www.modot.org/",
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
            documentationUrl = "https://documenter.getpostman.com/view/664302/S1ENwyLg",
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
            documentationUrl = "https://ipac.ecosphere.fws.gov/",
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
            documentationUrl = "https://gapanalysis.usgs.gov/apps/species-data-download/",
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
            documentationUrl = "https://explorer.natureserve.org/api-docs/",
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
            documentationUrl = "https://wildlife.faa.gov/home",
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
            documentationUrl = "https://transportation.gov/",
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
            documentationUrl = "https://www.census.gov/data/developers/guidance.html",
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
            documentationUrl = "https://nid.sec.usace.army.mil/",
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
            documentationUrl = "https://wiki.openstreetmap.org/wiki/Overpass_API",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Real-time vector geographic boundaries for linear barrier features including fences, highways, and railways."
        )
    )
}
