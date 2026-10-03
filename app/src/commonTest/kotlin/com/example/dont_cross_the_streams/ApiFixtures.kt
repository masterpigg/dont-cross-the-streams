package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.remote.HttpGet

/**
 * Trimmed copies of real response shapes from each API, used to test parsing without network access.
 */
object ApiFixtures {
    const val INAT_DEAD = """{"total_results":3,"page":1,"per_page":200,"results":[
      {"id":101,"observed_on":"2024-10-28","uri":"https://www.inaturalist.org/observations/101",
       "geojson":{"type":"Point","coordinates":[-90.5012,38.7121]},
       "taxon":{"name":"Odocoileus virginianus","preferred_common_name":"White-tailed Deer","iconic_taxon_name":"Mammalia"}},
      {"id":102,"observed_on":"2024-11-02","uri":"https://www.inaturalist.org/observations/102",
       "geojson":{"type":"Point","coordinates":[-90.5020,38.7125]},
       "taxon":{"name":"Odocoileus virginianus","preferred_common_name":"White-tailed Deer","iconic_taxon_name":"Mammalia"}},
      {"id":103,"observed_on":"2024-05-14","geojson":null,"taxon":{"name":"Terrapene carolina","iconic_taxon_name":"Reptilia"}}
    ]}"""

    const val INAT_OBSERVATIONS = """{"total_results":2,"results":[
      {"id":101,"observed_on":"2024-10-28","geojson":{"coordinates":[-90.5012,38.7121]},
       "taxon":{"name":"Odocoileus virginianus","preferred_common_name":"White-tailed Deer","iconic_taxon_name":"Mammalia"}},
      {"id":201,"observed_on":"2025-04-03","uri":"https://www.inaturalist.org/observations/201",
       "geojson":{"coordinates":[-90.6100,38.6500]},"photos":[{"url":"https://static.inaturalist.org/photos/1/square.jpg"}],
       "taxon":{"name":"Ambystoma maculatum","preferred_common_name":"Spotted Salamander","iconic_taxon_name":"Amphibia"}}
    ]}"""

    const val GBIF = """{"offset":0,"limit":300,"endOfRecords":true,"count":2,"results":[
      {"key":4000000001,"species":"Procyon lotor","scientificName":"Procyon lotor (Linnaeus, 1758)","class":"Mammalia",
       "decimalLatitude":38.65,"decimalLongitude":-90.55,"eventDate":"2019-07-11T00:00:00","datasetName":"MDC Survey",
       "occurrenceID":"mdc-123"},
      {"key":4000000002,"species":"Odocoileus virginianus","class":"Mammalia","decimalLatitude":38.7,"decimalLongitude":-90.5,
       "occurrenceID":"https://www.inaturalist.org/observations/999"}
    ]}"""

    const val OVERPASS_INFRA = """{"version":0.6,"elements":[
      {"type":"way","id":11,"tags":{"highway":"motorway","ref":"I 64","name":"Avenue of the Saints","lanes":"3","maxspeed":"65 mph"},
       "geometry":[{"lat":38.700,"lon":-90.600},{"lat":38.705,"lon":-90.550},{"lat":38.712,"lon":-90.502}]},
      {"type":"way","id":12,"tags":{"railway":"rail","operator":"Norfolk Southern"},
       "geometry":[{"lat":38.60,"lon":-90.40},{"lat":38.61,"lon":-90.39}]},
      {"type":"node","id":13,"lat":38.58,"lon":-90.45,"tags":{"waterway":"dam","name":"Test Dam"}},
      {"type":"way","id":14,"center":{"lat":38.71,"lon":-90.52},"tags":{"man_made":"wildlife_crossing","bridge":"yes","name":"Test Overpass"}},
      {"type":"way","id":15,"center":{"lat":38.69,"lon":-90.51},"tags":{"waterway":"stream","tunnel":"culvert","name":"Dardenne Creek"}},
      {"type":"way","id":16,"tags":{"highway":"service"}}
    ]}"""

    const val NBI = """{"objectIdFieldName":"OBJECTID","features":[
      {"attributes":{"STRUCTURE_NUMBER_008":"'A1234'","FACILITY_CARRIED_007":"'I-64'","FEATURES_DESC_006A":"'DARDENNE CREEK'",
       "SERVICE_UND_042B":"5","STRUCTURE_TYPE_043B":"19","ADT_029":64000,"YEAR_BUILT_027":1972,"LATDD":38.7101,"LONGDD":-90.6402}},
      {"attributes":{"STRUCTURE_NUMBER_008":"B5678","FACILITY_CARRIED_007":"MO 94","FEATURES_DESC_006A":"FEMME OSAGE CREEK",
       "SERVICE_UND_042B":"5","STRUCTURE_TYPE_043B":"02","ADT_029":"5400","YEAR_BUILT_027":0,"LATDD":38.62,"LONGDD":-90.81}},
      {"attributes":{"STRUCTURE_NUMBER_008":"C0","LATDD":0,"LONGDD":0}}
    ]}"""

    const val TIGER_TRACTS = """{"features":[
      {"attributes":{"GEOID":"29189212100","NAME":"Census Tract 2121","POP100":4200,"AREALAND":3500000,"CENTLAT":"+38.7000000","CENTLON":"-090.5000000"},
       "geometry":{"rings":[[[-90.51,38.69],[-90.49,38.69],[-90.49,38.71],[-90.51,38.71],[-90.51,38.69]]]}},
      {"attributes":{"GEOID":"29183310100","NAME":"Census Tract 3101","POP100":90,"AREALAND":20000000},
       "geometry":{"rings":[[[-90.8,38.6],[-90.7,38.6],[-90.7,38.7],[-90.8,38.6]]]}}
    ]}"""

    const val TIGER_COUNTY_STATS = """{"features":[{"attributes":{"POP":1004125,"LAND":1314700000}}]}"""

    /** Routes requests to the fixture for each host; anything else fails like a network error. */
    fun http(overrides: Map<String, String?> = emptyMap()): HttpGet = { url ->
        val match = overrides.entries.firstOrNull { url.contains(it.key) }
        when {
            match != null -> match.value
            url.contains("api.inaturalist.org") && url.contains("term_value_id=19") ->
                if (url.contains("per_page=0")) """{"total_results":321,"results":[]}""" else INAT_DEAD
            url.contains("api.inaturalist.org") ->
                if (url.contains("per_page=0")) """{"total_results":4567,"results":[]}""" else INAT_OBSERVATIONS
            url.contains("api.gbif.org") -> if (url.contains("limit=0")) """{"count":8910,"results":[]}""" else GBIF
            url.contains("overpass-api.de") && url.contains("make%20stats") ->
                """{"elements":[{"type":"stats","tags":{"road_m":"812345.6"}}]}"""
            url.contains("overpass-api.de") && url.contains("out%20count") ->
                """{"elements":[{"type":"count","tags":{"nodes":"0","ways":"1","relations":"0","total":"1"}}]}"""
            url.contains("overpass-api.de") -> OVERPASS_INFRA
            url.contains("geo.dot.gov") -> if (url.contains("returnCountOnly=true")) """{"count":412}""" else NBI
            url.contains("tigerweb.geo.census.gov") -> if (url.contains("outStatistics")) TIGER_COUNTY_STATS else TIGER_TRACTS
            else -> null
        }
    }
}
