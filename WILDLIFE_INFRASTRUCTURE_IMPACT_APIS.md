# US Wildlife, Migration & Anthropogenic Impact API Reference Guide

> **Purpose:** A comprehensive technical reference of public APIs, geospatial web services, and federal databases confined to the United States. Designed to enable an agent or developer to build spatial overlays showing where man-made closed systems (highways, railways, dams, airports, urban sprawl) conflict with native wildlife, disrupt migration corridors, and cause population decline.

---

## Architecture Overview: The Conflict Matrix

To map where man-made closed systems create friction with natural species, four distinct data layers must be intersected:

```
+-------------------------------------------------------------------------+
| Layer 1: Biological Baseline & Movement                                 |
| - Species ranges, GPS telemetry, migration corridors, seasonal abundance|
| (GBIF, Movebank, eBird, USGS GAP, USFWS IPaC)                           |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
| Layer 2: Anthropogenic Barriers & Linear Infrastructure                 |
| - Limited-access highways, fences, rail corridors, dams, power lines    |
| (OpenStreetMap Overpass API, USACE NID, USGS NHD)                       |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
| Layer 3: Direct Mortality & Conflict Hotspots                           |
| - Wildlife-vehicle collisions, roadkill, FAA airport strikes, train hits|
| (FAA NWSD, iNaturalist Dead/Roadkill, State DOT WVC, NHTSA FARS)        |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
| Layer 4: Human Encroachment & Longitudinal Decline                      |
| - Population density, urban growth/impervious surface, species decline  |
| (US Census API, NASA Human Footprint, USGS Breeding Bird Survey, NLCD)  |
+-------------------------------------------------------------------------+
```

---

## 1. Native Animals, Migration Patterns & Population Density

### 1.1. GBIF (Global Biodiversity Information Facility)
* **What it provides:** Aggregated occurrence data (over 2.5 billion records globally, dense US coverage) sourced from museums, citizen science, and research institutes. Contains native status, coordinates, taxon taxonomy, and date/time.
* **Base URL:** `https://api.gbif.org/v1/`
* **Key Endpoints:**
  * `GET /occurrence/search`: Query occurrences by bounding box, polygon, scientific name, taxon key, or country.
* **Query Parameters:**
  * `country=US`
  * `geometry=POLYGON((...))` (WKT bounding area)
  * `hasCoordinate=true`
  * `year=2010,2025`
  * `basisOfRecord=HUMAN_OBSERVATION` or `PRESERVED_SPECIMEN`
* **Auth:** Free, no API key required for standard queries.
* **Documentation:** https://www.gbif.org/developer/summary

### 1.2. Movebank REST API (Animal GPS Telemetry & Tracking)
* **What it provides:** Real GPS tracking and telemetry datasets for individual tagged animals (mammals, raptors, migratory birds, reptiles). Directly demonstrates actual animal movement pathways and barrier crossings across roads and waterways.
* **Base URL:** `https://www.movebank.org/movebank/service/direct-read`
* **Key Query Format:**
  * Study list: `entity_type=study`
  * Sensor events (coordinates + timestamps): `entity_type=event&study_id={STUDY_ID}&sensor_type_id=653` (GPS)
* **Auth:** Free account. Many public studies can be queried directly; licensed studies require passing basic HTTP auth credentials or `access_token`.
* **Documentation / Repo:** https://github.com/movebank/movebank-api-doc
* **Agent Utility:** Movebank tracks reveal where GPS-collared deer, bears, elk, or cougars approach an interstate highway or dam and either turn around (barrier effect) or get struck.

### 1.3. Cornell Lab of Ornithology — eBird API v2
* **What it provides:** Bird observations, seasonal migration arrival/departure dates, relative abundance, and hotspot density across the US.
* **Base URL:** `https://api.ebird.org/v2/`
* **Key Endpoints:**
  * `GET /data/obs/geo/recent`: Recent bird observations near lat/long coordinates.
  * `GET /product/lists/{regionCode}`: Checklist submissions by US county or state.
  * `GET /data/obs/{regionCode}/historic/{y}/{m}/{d}`: Historical sightings for time-series analysis.
* **Auth:** Requires free API key (header `X-eBirdApiToken: <API_KEY>`).
* **Documentation:** https://documenter.getpostman.com/view/664302/S1ENwy59

### 1.4. iNaturalist API v1
* **What it provides:** Rich crowd-sourced biodiversity observations with high-resolution photographic evidence and coordinate validation.
* **Base URL:** `https://api.inaturalist.org/v1/`
* **Key Endpoints:**
  * `GET /observations`: Filter by taxon, bounding box, quality grade (`research`), and life stage.
* **Auth:** Free, no auth needed for read endpoints; requests rate-limited to 60 req/min.
* **Documentation:** https://api.inaturalist.org/v1/docs/

### 1.5. USFWS IPaC (Information for Planning and Consultation) Location API
* **What it provides:** Official U.S. Fish and Wildlife Service regulatory data: lists of endangered and threatened species, candidate species, and designated critical habitats intersecting a custom geographic polygon.
* **Base URL:** `https://ecos.fws.gov/ipac/` / `https://ecos.fws.gov/ecp/services`
* **Input:** Accepts GeoJSON or WKT (`projectLocationWKT`) to return intersecting species and critical habitats.
* **Auth:** Open access / moving toward registration API keys via `fwhq_ipac@fws.gov`.
* **Agent Utility:** Instantly identifies if a proposed or existing infrastructure corridor cuts through federally protected critical habitat.

### 1.6. USGS Gap Analysis Project (GAP) Species Habitat Models
* **What it provides:** Predicted habitat distribution maps and species ranges for over 2,000 terrestrial vertebrate species across the contiguous US.
* **Access Method:** USGS ArcGIS MapServer & OGC Web Map Services (WMS):
  * `https://www.sciencebase.gov/catalog/`
  * Raster GeoTIFF download via USGS GAP Species portal (`https://gapanalysis.usgs.gov/`)
* **Auth:** Public domain, no key.

### 1.7. NatureServe Explorer REST API
* **What it provides:** Conservation status metrics (Global G-Rank, State S-Rank), native/introduced classifications, species taxonomy, and environmental threat assessments for US and Canadian biodiversity.
* **Base URL:** `https://explorer.natureserve.org/api/data/`
* **Key Endpoints:**
  * `POST /speciesSearch`: Complex multi-criteria search (jurisdiction, status, taxon).
  * `GET /taxon/{taxonId}`: Comprehensive species profile, threats, and decline status.
* **Auth:** Free developer registration.
* **Documentation:** https://explorer.natureserve.org/api/docs/

### 1.8. USGS North American Breeding Bird Survey (BBS) via ScienceBase API
* **What it provides:** 50+ year standardized ecological survey (1966–present) measuring population changes, density, and longitudinal declines along thousands of fixed roadside survey routes across the US.
* **Base URL:** `https://www.sciencebase.gov/catalog/items`
* **Access Methods:**
  * ScienceBase REST API: Query BBS datasets by catalog item tag.
  * Python Client: `sciencebasepy` (`pip install sciencebasepy`).
  * R Package: `bbsAssistant`.
* **Agent Utility:** The definitive historical source to measure species decline in areas that transformed from woodland/grassland to suburbs over the last 40 years.

---

## 2. Animal Collisions & Wildlife-Infrastructure Conflict

### 2.1. FAA National Wildlife Strike Database (NWSD) API
* **What it provides:** Official database of over 250,000 strikes between civil/military aircraft and wildlife (birds, bats, terrestrial mammals like deer/coyotes on runways) across US airports from 1990 to present. Details species, airport FAA code, height AGL, phase of flight, damage cost, and animal carcass remains.
* **Base URL:** `https://wildlife.faa.gov/` (OpenAPI Swagger endpoint: `https://qa-wildlife.faa.gov/api/swagger/v1/swagger.json`)
* **Key Endpoints:**
  * `GET /api/reports`: Search strikes by date range, airport ID, state, species ID, and damage level.
  * `GET /api/airports`: Airport lookup and strike frequency summaries.
  * `GET /api/species`: Taxonomy of struck birds and mammals.
* **Bulk Download:** Complete Microsoft Access and CSV databases available at `https://wildlife.faa.gov/`.
* **Auth:** Public API.

### 2.2. iNaturalist Deceased & Roadkill Observation Filters
* **What it provides:** Precise, geo-tagged, community-verified carcasses and roadkill observations across North America.
* **Base URL:** `https://api.inaturalist.org/v1/observations`
* **Implementation Tricks for Collisions:**
  * **Annotation Filter (Dead):** Add `term_id=17&term_value_id=19` (Controlled term 17 = "Alive or Dead", Value 19 = "Dead").
  * **Observation Field (Roadkill):** Add `field:Roadkill=yes` or `field:Road%20Mortality=yes`.
  * **Specialized Projects:** Query specific project IDs, e.g., California Roadkill Observation System (CROS), Global Roadkill Observations: `project_id={PROJECT_ID}`.
* **Example Query:**
  ```http
  GET https://api.inaturalist.org/v1/observations?term_id=17&term_value_id=19&place_id=1&taxon_id=40151&quality_grade=research
  ```
  *(Returns research-grade dead mammal records in the United States)*

### 2.3. Federal & State Wildlife-Vehicle Collision (WVC) Services
* **National Park Service & USFWS ROaDS (Roadkill Observation and Data System):**
  * Integrated GIS framework using Esri ArcGIS Online Feature Services for tracking roadkill across public lands.
* **State DOT ArcGIS Feature Services:**
  * Many state Departments of Transportation (e.g., California Caltrans, Colorado CDOT, Washington WSDOT, Utah UDOT) publish REST API endpoints for animal collisions and carcass removal locations:
  * Example: CDOT Wildlife Collisions ArcGIS REST Service:
    `https://services.arcgis.com/.../FeatureServer/0/query?where=1=1&outFields=*&f=geojson`
* **Academic Aggregators:**
  * UC Davis Road Ecology Center / California Roadkill Observation System (CROS) provides downloadable and API-queryable historical datasets (thousands of entries mapped with mile markers and species).

### 2.4. NHTSA Crash API (Fatality Analysis Reporting System — FARS)
* **What it provides:** Programmatic access to all fatal motor vehicle crashes on public roads in the United States.
* **Base URL:** `https://crashviewer.nhtsa.dot.gov/CrashAPI`
* **Key Endpoints:**
  * `GET /CrashAPI/crashes/GetCrashesByLocation`: Filter by state, county, year.
  * Filter criteria: `First Harmful Event` = `Collision with Animal` (Code value 8 or state specific).
* **Limitations:** Captures human fatalities resulting from collisions with deer/elk/moose; under-reports minor property-damage crashes. Use in conjunction with State DOT carcass removal data.

### 2.5. Federal Railroad Administration (FRA) Safety Data API
* **What it provides:** Rail accident and incident reports across US rail lines (freight and passenger).
* **Base URL:** `https://safetydata.fra.dot.gov/` (Developer portal and APIs).
* **Usage:** Query Form FRA F 6180.54 reports for obstruction collisions on tracks where wildlife strikes caused equipment damage or delays.

---

## 3. Human Population Density & Urban Growth

### 3.1. US Census Bureau API
* **What it provides:** Gold standard for population density, demographics, and housing unit density down to the Census Tract, Block Group, and Census Block levels across all 50 states.
* **Base URL:** `https://api.census.gov/data/`
* **Key Endpoints:**
  * `GET /data/{year}/acs/acs5`: American Community Survey 5-Year estimates (variable `B01003_001E` for total population).
  * `GET /data/2020/dec/dhc`: 2020 Decennial Census down to the block level.
* **Spatial Geography / Boundaries:**
  * **TIGERweb REST API:** `https://tigerweb.geo.census.gov/arcgis/rest/services/TIGERweb/`
  * Provides GeoJSON boundaries and `ALAND` (land area in m²) to compute exact population density:
    $$\text{Density} = \frac{\text{B01003\_001E}}{\text{ALAND} / 1{,}000{,}000} \quad (\text{people per km}^2)$$
* **Auth:** Free Census API key (optional for small testing batches, recommended for production).
* **Documentation:** https://www.census.gov/data/developers/guidance/api-user-guide.html

### 3.2. NASA SEDAC / CIESIN — Global Human Footprint & Population Grids
* **What it provides:**
  * **Human Footprint Index (HFI):** A 1km² resolution gridded index quantifying cumulative anthropogenic pressure (built environments, population density, electric infrastructure, highways, railways, and navigable waterways). Directly answers where closed human systems sever ecosystems.
  * **Gridded Population of the World (GPW v4):** Raster data showing human population distribution independent of administrative boundaries.
* **Access Method:** OGC WMS/WCS and GeoTIFF downloads via NASA Earthdata:
  * `https://sedac.ciesin.columbia.edu/data/collection/wildareas-v3`
  * `https://earthdata.nasa.gov/`
* **Auth:** Free NASA Earthdata Login account.

### 3.3. WorldPop & Oak Ridge National Laboratory (LandScan)
* **What it provides:** High-resolution (100m to 1km) ambient population density surfaces mapping where people work and travel during the day versus nighttime residence.
* **WorldPop REST API:** `https://www.worldpop.org/sdi/intro/api`

---

## 4. Man-Made Closed Systems, Barriers & Fragmentation

### 4.1. USACE National Inventory of Dams (NID) API
* **What it provides:** Database of over 91,000 dams across the US. Critical for demonstrating river fragmentation, fish migration barriers (e.g. salmon/shad), water flow disruption, and reservoir flooding.
* **Base URL:** `https://nid.sec.usace.army.mil/api`
* **Interactive Documentation (Swagger):** `https://nid.sec.usace.army.mil/api/developer`
* **Key Endpoints:**
  * `GET /api/dams`: Filter by state, river name, hazard potential, structural height, and fish ladder status.
  * `GET /api/dams/{id}`: Detailed specifications (impoundment capacity, construction year, primary purpose).
* **ArcGIS GeoPlatform:** USACE also exposes these layers via ArcGIS OGC Features (`https://geospatial-usace.opendata.arcgis.com/`).
* **Auth:** Public REST API.

### 4.2. USGS National Hydrography Dataset (NHDPlus HR)
* **What it provides:** Detailed vector flowlines and waterbody networks showing natural stream connectivity and where culverts or diversions cut off aquatic species.
* **Endpoint:** USGS Hydrography REST API:
  `https://hydro.nationalmap.gov/arcgis/rest/services/nhd/MapServer`

### 4.3. USGS National Land Cover Database (NLCD) & MRLC Consortium
* **What it provides:** 30-meter resolution national land cover classification updated in multi-year epochs (2001, 2006, 2011, 2016, 2019, 2021).
* **Key Classes for Anthropogenic Encroachment:**
  * Developed, High Intensity (Class 24 - commercial/industrial/dense urban)
  * Developed, Medium Intensity (Class 23 - suburban single-family)
  * Developed, Low Intensity (Class 22)
  * Developed, Open Space (Class 21 - roadsides, golf courses)
  * Deciduous/Evergreen/Mixed Forest (Classes 41, 42, 43)
  * Emergent Herbaceous Wetlands (Class 95)
* **Access Method:** Web Coverage Service (WCS) / WMS via MRLC:
  * `https://www.mrlc.gov/geoserver/wms`
  * Raster GeoTIFF tiles from USGS EarthExplorer / ScienceBase.
* **Agent Utility:** Calculate percentage of woodland or wetland lost within a 10km buffer around a growing city over a 20-year window.

### 4.4. OpenStreetMap (OSM) Overpass API (Linear Anthropogenic Barriers)
* **What it provides:** The most up-to-date, hyper-detailed vector dataset of physical man-made linear barriers in any bounding box in the US.
* **Base URL:** `https://overpass-api.de/api/interpreter`
* **Key Barrier Queries:**
  * **Limited Access Expressways & Interstates:** `way["highway"~"motorway|trunk"](bbox);`
  * **Railroads:** `way["railway"~"rail"](bbox);`
  * **Fences & Walls:** `way["barrier"~"fence|wall|retaining_wall"](bbox);`
  * **High-Voltage Transmission Corridors:** `way["power"="line"](bbox);`
  * **Pipelines:** `way["man_made"="pipeline"](bbox);`
* **Example Overpass QL Query for Highway Barriers:**
  ```text
  [out:json][timeout:25];
  (
    way["highway"="motorway"](37.7,-122.5,37.8,-122.3);
    way["highway"="trunk"](37.7,-122.5,37.8,-122.3);
  );
  out body;
  >;
  out skel qt;
  ```

### 4.5. USGS Protected Areas Database of the United States (PAD-US)
* **What it provides:** Comprehensive inventory of public lands and private conservation easements (National Parks, BLM, US Forest Service, State Parks, Land Trusts).
* **ArcGIS REST Endpoint:**
  `https://services.arcgis.com/.../PADUS_Protected_Areas/FeatureServer`
* **Agent Utility:** Demonstrates where protected corridors abruptly end at private agricultural fences, housing developments, or major tollways (habitat islands).

---

## 5. Quick-Reference API Comparison Cheat-Sheet

| Dataset / API | Primary Data Type | Geographic Scope | Auth Required | Data Format | Best For |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **GBIF Occurrence API** | Biodiversity Occurrences | Global / US filter | No | JSON | Baseline species presence in an area |
| **Movebank Direct-Read API** | Animal GPS Tracks / Telemetry | Global / US studies | Free User / Key | CSV / JSON | Animal movement tracks crossing roads |
| **eBird API v2** | Bird Sightings & Abundance | Global / US filter | Free API Key | JSON | Bird migration timing and seasonal density |
| **iNaturalist API v1** | Observations & Dead Annotations | Global / US filter | No | JSON | Crowd-sourced roadkill and carcass points |
| **USFWS IPaC Location API** | Threatened/Endangered & Habitats| United States | Open / Email Key | JSON / WKT | Regulatory environmental conflict analysis |
| **USGS BBS (ScienceBase)** | Time-series population trends | North America | No | JSON / CSV | 50-year bird population decline rates |
| **FAA NWSD API** | Aircraft-Wildlife Strikes | United States | No | JSON / CSV / DB | Airport & low-altitude birdstrike hazards |
| **State DOT GIS Services** | Wildlife-Vehicle Collisions | State-specific | No | GeoJSON / REST | Highway carcass removal & crash hotspots |
| **NHTSA Crash API (FARS)** | Fatal Vehicle Crashes | United States | No | JSON / CSV | Human fatality crashes with animals |
| **US Census Bureau API** | Human Population & Demographics| United States | Free Key | JSON | Exact population counts per tract/block |
| **US Census TIGERweb** | Administrative Boundaries | United States | No | GeoJSON / REST | Land area (`ALAND`) to compute density |
| **USACE NID API** | Dam Locations & Specifications | United States | No | JSON / REST | Aquatic migration & river barriers |
| **NASA SEDAC Human Footprint**| Cumulative Human Encroachment | Global / US | Free Earthdata | GeoTIFF / WCS | Macro anthropogenic pressure index |
| **USGS NLCD (MRLC)** | Land Cover & Urban Development | United States | No | WMS / GeoTIFF | Woodland loss & urban expansion over time |
| **OpenStreetMap Overpass** | Linear Barriers (Roads/Fences) | Global / US | No | JSON / GeoJSON | Vector lines of physical migration obstacles |

---

## 6. Implementation Strategy for an Autonomous Agent

When an agent is tasked with building this spatial conflict application, it should execute the following pipeline:

### Step 1: Spatial Grid Partitioning
* Partition the region of interest into equal-area hexagonal cells (e.g. **Uber H3** resolution 7 or 8, roughly 1km–5km per cell) or standard GeoJSON bounding boxes.

### Step 2: Layer Ingestion & Feature Engineering
For each cell:
1. **Animal Density Index ($A$):** Query GBIF / eBird occurrences and USGS GAP habitat suitability.
2. **Barrier Density ($B$):** Query OSM Overpass for total linear meters of `highway=motorway`, `railway=rail`, and USACE NID for dam counts.
3. **Collision Incident Count ($C$):** Query iNaturalist (`term_id=17&term_value_id=19`), State DOT WVC, and FAA NWSD.
4. **Human Pressure ($H$):** Query US Census population density + NASA Human Footprint raster value.
5. **Historical Habitat Loss ($\Delta L$):** Compute delta of NLCD Developed land (Classes 22–24) vs. Forest/Wetland (Classes 41–43, 95) between 2001 and 2021.

### Step 3: Severance & Conflict Index Calculation
Define a normalized composite score representing the severity of the man-made barrier:
$$\text{Conflict Score} = w_1 \cdot A \times B + w_2 \cdot C + w_3 \cdot H \times \Delta L$$
* High score indicates a high biological presence directly intersected by high-speed barriers or dense human settlement with high recorded mortality.

---

## 7. Useful Libraries & SDKs

* **Python:**
  * `geopandas` / `shapely` / `pyproj`: Spatial overlays and intersection calculations.
  * `h3-py`: Hexagonal hierarchical spatial indexing.
  * `requests` / `httpx`: Fetching REST APIs with backoff/retry.
  * `sciencebasepy`: Direct interface to USGS ScienceBase.
  * `overpy` or `osmnx`: Python wrappers for OpenStreetMap Overpass queries.
* **JavaScript / Node / Browser:**
  * `turf.js`: Client-side geospatial analysis (buffers, intersections, points-in-polygon).
  * `leaflet` or `maplibre-gl-js`: High-performance vector tile and heatmap rendering.
  * `h3-js`: Hexagonal indexing in Javascript.
