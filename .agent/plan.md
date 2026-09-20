# Project Plan

Create an Android app titled "Don't Cross the Streams" (or similar appropriate name) for mapping and visualizing how human infrastructure/closed loop systems affect wildlife. The app overlays wildlife migration/occurrence data, wildlife collision/roadkill data, human population density/urban development, and physical barriers (roads, dams, fences). It must be easy to use (interactive map layer overlays, species/infrastructure filter, conflict/hotspot analysis, detailed data source links & references to real public APIs like GBIF, Movebank, eBird, iNaturalist, FAA NWSD, State DOT, US Census, USACE Dams, OpenStreetMap Overpass).

User Context & Requirements:
- Kids project on human closed-loop systems impact on wildlife.
- Map layers: Wildlife occurrence & migration, Collision & roadkill hotspots, Population density & human footprint, Linear infrastructure & barriers (highways, dams, fences).
- Data Source references & clickable links for educational & research transparency.
- Educational conflict score / risk matrix overview.
- Easy to use, intuitive Android UI using Jetpack Compose and modern map rendering (Maps SDK / Osmdroid / MapLibre / Compose Canvas Map).

## Project Brief

# Project Brief: Don't Cross the Streams

## Features
1. **Interactive Multi-Layer Conflict Map**: Visualize how human closed-loop infrastructure impacts wildlife by toggling interactive map overlays for wildlife migrations/occurrences, roadkill/collision hotspots, population density, and physical barriers (highways, dams, fences).
2. **Species & Infrastructure Filter**: Filter map layers by target species groups (e.g., large mammals, migratory birds) and infrastructure barrier types to isolate and examine specific ecological conflict zones.
3. **Conflict Risk Scorecard & Matrix**: Access an intuitive educational risk assessment matrix that quantifies and explains how human infrastructure density impacts local wildlife connectivity in selected regions.
4. **Data Source Transparency Hub**: Interactive educational directory providing clickable references and links to real open public APIs and ecological datasets (including GBIF, iNaturalist, Movebank, eBird, OpenStreetMap, US Census, and USACE Dams).

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Navigation**: **Jetpack Navigation 3** (state-driven navigation)
- **Adaptive Strategy**: **Compose Material Adaptive** library (for adaptive multi-pane and responsive layouts across screen sizes)
- **Architecture**: MVVM (Model-View-ViewModel) with Jetpack ViewModel & StateFlow
- **Concurrency**: Kotlin Coroutines & Flow
- **Map Rendering**: Google Maps Compose SDK (or Osmdroid / MapLibre Compose for interactive vector & GeoJSON map layer overlays)
- **Networking**: Retrofit & OkHttp (for querying public REST APIs)

## Implementation Steps
**Total Duration:** 4h 32m 16s

### Task_1_DataAndDomainLayer: Set up domain models, data repositories, and API integration for wildlife occurrences, collision hotspots, barriers, population density, and public dataset directory (GBIF, iNaturalist, eBird, Movebank, OSM, US Census, USACE Dams).
- **Status:** COMPLETED
- **Updates:** Completed Task_1_DataAndDomainLayer: Defined domain models (WildlifeOccurrence, CollisionHotspot, BarrierFeature, PopulationDensityZone, RiskMatrixScore, DataSourceInfo), created network API services (GBIF, iNaturalist, OSM Overpass) and mock fallback data sources, implemented dataset transparency repository for 14 public APIs, and verified conflict matrix calculation logic with passing unit tests.
- **Acceptance Criteria:**
  - Domain models defined for wildlife data, barrier layers, risk matrix, and dataset directory
  - Repository and network/mock data sources implemented for map overlays and API transparency entries
  - project builds successfully
- **Duration:** 6m 16s

### Task_2_InteractiveMapAndFilteringUI: Implement the Interactive Multi-Layer Conflict Map UI using Jetpack Compose, supporting togglable overlays (wildlife migrations, roadkill hotspots, population density, physical barriers) and species/infrastructure filtering.
- **Status:** COMPLETED
- **Updates:** Completed Task_2_InteractiveMapAndFilteringUI: Implemented InteractiveConflictMap with pan/zoom/tap gestures, rendering color-coded wildlife occurrence markers, collision/roadkill pins with pulse halos, linear barrier vector paths, and population density heatmaps. Added floating LayerToggleBar, FilterBottomSheet for species/barrier filtering and regional presets, FeatureDetailBottomSheet for tapped map items, and MapViewModel. Provided Compose @Previews and verified build and tests pass.
- **Acceptance Criteria:**
  - Interactive map screen rendered with togglable map overlays
  - Filter controls for species groups and barrier types integrated with map layer state
  - UI builds successfully and displays overlay toggles and filter dialog/sheet
- **Duration:** 7m 27s

### Task_3_RiskMatrixAndTransparencyHubUI: Develop the Conflict Risk Scorecard & Matrix view and the Data Source Transparency Hub screen using Jetpack Navigation 3 and Compose Material Adaptive layout.
- **Status:** COMPLETED
- **Updates:** Completed Task_3_RiskMatrixAndTransparencyHubUI: Built Conflict Risk Scorecard & Matrix UI with regional selector, composite conflict score gauge, factor progress bars, and mitigation recommendations. Built Data Source Transparency Hub displaying all 14 public datasets with clickable web links and search. Integrated Jetpack Navigation 3 and Compose Material 3 Adaptive ListDetailPaneScaffold layout with BottomBar/NavigationRail for phone and tablet screens. Verified build and 28/28 unit tests pass.
- **Acceptance Criteria:**
  - Conflict Risk Scorecard & Matrix quantifies and displays regional connectivity risk
  - Data Source Transparency Hub presents clickable open public API references (GBIF, iNaturalist, Movebank, etc.)
  - Jetpack Navigation 3 connects map, risk matrix, and transparency hub seamlessly
  - Adaptive multi-pane layout supports various screen sizes
- **Duration:** 4h 1m 17s

### Task_4_RunAndVerify: Run and verify the complete application stability, confirm alignment with user requirements, verify no crashes, and instruct critic_agent to report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Completed Task_4_RunAndVerify: App deployed and tested by critic_agent. Verified zero crashes, complete functionality across Interactive Conflict Map with multi-layer overlays, FilterBottomSheet with species/barrier filters and preset corridors, Risk Matrix & Scorecard, Data Sources Hub with public API links, and adaptive multi-pane navigation for phone and tablet screens. All 28 unit tests pass.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - critic_agent verifies application stability, alignment with requirements, and reports critical UI issues
- **Duration:** 17m 16s

