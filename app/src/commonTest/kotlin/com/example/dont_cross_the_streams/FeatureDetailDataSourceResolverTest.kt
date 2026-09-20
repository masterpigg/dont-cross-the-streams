package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.ui.main.MainTab
import com.example.dont_cross_the_streams.ui.main.MainViewModel
import com.example.dont_cross_the_streams.ui.map.resolveDataSourceUrl
import com.example.dont_cross_the_streams.ui.map.resolveTransparencyHubDataSourceId
import com.example.dont_cross_the_streams.ui.map.splitSourceNames
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureDetailDataSourceResolverTest {

    @Test
    fun resolveDataSourceUrl_mapsKnownSourcesToUrls() {
        assertEquals("https://www.gbif.org/", resolveDataSourceUrl("GBIF"))
        assertEquals("https://www.inaturalist.org/", resolveDataSourceUrl("iNaturalist"))
        assertEquals("https://www.movebank.org/", resolveDataSourceUrl("Movebank GPS Tracking"))
        assertEquals("https://ebird.org/", resolveDataSourceUrl("eBird"))
        assertEquals("https://overpass-api.de/", resolveDataSourceUrl("OpenStreetMap Overpass"))
        assertEquals("https://nid.sec.usace.army.mil/", resolveDataSourceUrl("USACE NID"))
        assertEquals("https://www.census.gov/", resolveDataSourceUrl("US Census Bureau 2020"))
        assertEquals("https://www.modot.org/", resolveDataSourceUrl("MoDOT Safety GIS"))
        assertEquals("https://mdc.mo.gov/", resolveDataSourceUrl("MDC Wildlife Census"))
        assertEquals("https://dnr.illinois.gov/", resolveDataSourceUrl("Illinois DNR"))
        assertEquals("https://wdfw.wa.gov/", resolveDataSourceUrl("WDFW Salmon Passage API"))
        assertEquals("https://wildlife.faa.gov/", resolveDataSourceUrl("FAA NWSD"))
        assertEquals("https://crashviewer.nhtsa.dot.gov/CrashAPI", resolveDataSourceUrl("NHTSA FARS"))
    }

    @Test
    fun resolveDataSourceUrl_returnsOriginalUrlIfHttpOrHttps() {
        val directUrl = "https://custom-data-portal.gov/api"
        assertEquals(directUrl, resolveDataSourceUrl(directUrl))
    }

    @Test
    fun resolveTransparencyHubDataSourceId_mapsToValidHubIds() {
        assertEquals("gbif", resolveTransparencyHubDataSourceId("GBIF Sighting"))
        assertEquals("inaturalist", resolveTransparencyHubDataSourceId("iNaturalist Observations"))
        assertEquals("movebank", resolveTransparencyHubDataSourceId("Movebank"))
        assertEquals("ebird", resolveTransparencyHubDataSourceId("eBird"))
        assertEquals("osm_overpass", resolveTransparencyHubDataSourceId("OpenStreetMap Overpass"))
        assertEquals("usace_dams", resolveTransparencyHubDataSourceId("USACE National Inventory of Dams"))
        assertEquals("us_census", resolveTransparencyHubDataSourceId("US Census Bureau 2020"))
        assertEquals("modot_wvc", resolveTransparencyHubDataSourceId("MoDOT WVC Database"))
        assertEquals("mdc_wildlife", resolveTransparencyHubDataSourceId("MDC Wildlife Inventory"))
        assertEquals("idnr_wildlife", resolveTransparencyHubDataSourceId("Illinois DNR Herpetological Survey"))
        assertEquals("wdfw_salmon", resolveTransparencyHubDataSourceId("WDFW Salmon Passage"))
        assertEquals("faa_nwsd", resolveTransparencyHubDataSourceId("FAA NWSD"))
        assertEquals("nhtsa_fars", resolveTransparencyHubDataSourceId("NHTSA FARS"))
    }

    @Test
    fun splitSourceNames_handlesCompoundSources() {
        val split1 = splitSourceNames("MoDOT GIS / OpenStreetMap")
        assertEquals(2, split1.size)
        assertEquals("MoDOT GIS", split1[0])
        assertEquals("OpenStreetMap", split1[1])

        val split2 = splitSourceNames("WDFW, USACE NWD")
        assertEquals(2, split2.size)
        assertEquals("WDFW", split2[0])
        assertEquals("USACE NWD", split2[1])

        val single = splitSourceNames("Movebank GPS Tracking")
        assertEquals(1, single.size)
        assertEquals("Movebank GPS Tracking", single[0])
    }

    @Test
    fun mainViewModel_navigateToTransparencyHub_switchesTabAndSetsId() {
        val viewModel = MainViewModel()
        viewModel.navigateToTransparencyHub("gbif")
        val state = viewModel.uiState.value

        assertEquals(MainTab.TRANSPARENCY_HUB, state.currentTab)
        assertEquals("gbif", state.selectedDataSourceIdForHub)
    }
}
