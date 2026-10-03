package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.AuthType
import com.example.dont_cross_the_streams.domain.model.DataSourceCategory
import com.example.dont_cross_the_streams.domain.model.DataSourceInfo

/**
 * The public APIs the app actually queries, with the request shapes it sends. Nothing on the map
 * comes from anywhere else.
 */
object DataSourceCatalog {
    val dataSources = listOf(
        DataSourceInfo(
            id = "inaturalist",
            name = "iNaturalist API",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "Community science observations with photos, verified by other naturalists. Used for live " +
                "wildlife sightings (research grade) and for dead-animal reports, which iNaturalist users mark " +
                "with the \"Alive or Dead: Dead\" annotation.",
            baseURL = "https://api.inaturalist.org/v1/",
            keyEndpoints = listOf(
                "observations?quality_grade=research&iconic_taxa=Mammalia,Reptilia,Amphibia,Aves&swlat=&swlng=&nelat=&nelng=",
                "observations?term_id=17&term_value_id=19 (Alive or Dead = Dead) for the same area",
                "per_page=0 returns only total_results (used for study-area counts)"
            ),
            documentationUrl = "https://api.inaturalist.org/v1/docs/",
            homepageUrl = "https://www.inaturalist.org/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Wildlife layer, Collision Reports layer, and the hotspots derived from clusters of collision reports."
        ),
        DataSourceInfo(
            id = "gbif",
            name = "GBIF Occurrence API",
            category = DataSourceCategory.WILDLIFE_OBSERVATION,
            description = "Global Biodiversity Information Facility: museum, agency and survey occurrence records " +
                "aggregated from thousands of datasets. Records GBIF republishes from iNaturalist are skipped " +
                "because iNaturalist is queried directly.",
            baseURL = "https://api.gbif.org/v1/",
            keyEndpoints = listOf(
                "occurrence/search?decimalLatitude=min,max&decimalLongitude=min,max&taxonKey=359&taxonKey=212&taxonKey=358&taxonKey=131&hasCoordinate=true&hasGeospatialIssue=false",
                "limit=0 returns only the record count"
            ),
            documentationUrl = "https://techdocs.gbif.org/en/openapi/",
            homepageUrl = "https://www.gbif.org/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Additional vertebrate occurrence records for the Wildlife layer and study-area counts."
        ),
        DataSourceInfo(
            id = "osm_overpass",
            name = "OpenStreetMap Overpass API",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "Query service for OpenStreetMap, the volunteer-built map of the world. Used for road, " +
                "rail and dam barriers, mapped wildlife crossings, and stream culverts under major roads.",
            baseURL = "https://overpass-api.de/api/",
            keyEndpoints = listOf(
                "interpreter: way[highway~motorway|trunk|primary], way[railway=rail], nwr[waterway~dam|weir], way[waterway=canal]",
                "interpreter: nwr[man_made=wildlife_crossing]",
                "interpreter: way[waterway][tunnel=culvert](around major roads:20 m)",
                "interpreter: make stats road_m=sum(length()) for study-area road length"
            ),
            documentationUrl = "https://wiki.openstreetmap.org/wiki/Overpass_API",
            homepageUrl = "https://www.openstreetmap.org/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Barriers layer, dedicated wildlife crossings, culverts, and major-road length per study area."
        ),
        DataSourceInfo(
            id = "fhwa_nbi",
            name = "FHWA National Bridge Inventory",
            category = DataSourceCategory.INFRASTRUCTURE_BARRIER,
            description = "Federal inventory of every public road bridge and large culvert (span 20 ft or more), " +
                "published through the BTS National Transportation Atlas Database. The app requests structures " +
                "over water (item 42B codes 5-9), which streams and wildlife already use to pass under roads.",
            baseURL = "https://geo.dot.gov/server/rest/services/Hosted/National_Bridge_Inventory_DS/FeatureServer/0/",
            keyEndpoints = listOf(
                "query?where=SERVICE_UND_042B IN ('5','6','7','8','9')&geometry=w,s,e,n&geometryType=esriGeometryEnvelope&outFields=*",
                "Fields used: FACILITY_CARRIED_007, FEATURES_DESC_006A, ADT_029 (daily traffic), YEAR_BUILT_027, STRUCTURE_TYPE_043B (19 = culvert), LATDD, LONGDD",
                "returnCountOnly=true for study-area counts"
            ),
            documentationUrl = "https://www.fhwa.dot.gov/bridge/nbi.cfm",
            homepageUrl = "https://www.fhwa.dot.gov/bridge/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Potential crossings: existing bridges and culverts over waterways, with the traffic volume above them."
        ),
        DataSourceInfo(
            id = "census_tigerweb",
            name = "US Census Bureau TIGERweb (2020 Census Tracts)",
            category = DataSourceCategory.HUMAN_FOOTPRINT,
            description = "Census tract boundaries with the 2020 Census population count (POP100) and land area " +
                "(AREALAND), served by the Census Bureau's TIGERweb ArcGIS service.",
            baseURL = "https://tigerweb.geo.census.gov/arcgis/rest/services/Census2020/Tracts_Blocks/MapServer/0/",
            keyEndpoints = listOf(
                "query?geometry=w,s,e,n&geometryType=esriGeometryEnvelope&outFields=*&returnGeometry=true&outSR=4326",
                "query?where=STATE='29' AND COUNTY='510'|'189'|'183'&outStatistics=sum(POP100), sum(AREALAND)"
            ),
            documentationUrl = "https://tigerweb.geo.census.gov/arcgis/rest/services/Census2020/Tracts_Blocks/MapServer",
            homepageUrl = "https://www.census.gov/",
            authType = AuthType.NONE_PUBLIC,
            keyUtility = "Population density layer (people per km² of land, per tract) and study-area population."
        )
    )
}
