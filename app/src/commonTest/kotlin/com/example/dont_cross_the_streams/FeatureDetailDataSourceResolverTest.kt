package com.example.dont_cross_the_streams

import com.example.dont_cross_the_streams.data.datasource.DataSourceCatalog
import com.example.dont_cross_the_streams.ui.main.MainTab
import com.example.dont_cross_the_streams.ui.main.MainViewModel
import com.example.dont_cross_the_streams.ui.map.resolveDataSourceUrl
import com.example.dont_cross_the_streams.ui.map.resolveTransparencyHubDataSourceId
import com.example.dont_cross_the_streams.ui.map.splitSourceNames
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureDetailDataSourceResolverTest {

    @Test
    fun resolveDataSourceUrl_mapsEachSourceToItsDocs() {
        assertEquals("https://api.inaturalist.org/v1/docs/", resolveDataSourceUrl("iNaturalist (Dead-animal annotations)"))
        assertEquals("https://techdocs.gbif.org/en/openapi/", resolveDataSourceUrl("GBIF / MDC Survey"))
        assertEquals("https://wiki.openstreetmap.org/wiki/Overpass_API", resolveDataSourceUrl("OpenStreetMap Overpass"))
        assertEquals("https://www.fhwa.dot.gov/bridge/nbi.cfm", resolveDataSourceUrl("FHWA National Bridge Inventory (BTS NTAD)"))
        assertEquals(
            "https://tigerweb.geo.census.gov/arcgis/rest/services/Census2020/Tracts_Blocks/MapServer",
            resolveDataSourceUrl("US Census Bureau 2020 (TIGERweb)")
        )
    }

    @Test
    fun resolveDataSourceUrl_returnsOriginalUrlIfHttpOrHttps() {
        val directUrl = "https://custom-data-portal.gov/api"
        assertEquals(directUrl, resolveDataSourceUrl(directUrl))
    }

    @Test
    fun resolveDataSourceUrl_hubSourcesUseTheHubsCuratedLink() {
        DataSourceCatalog.dataSources.forEach { info ->
            assertEquals(info.documentationUrl, resolveDataSourceUrl(info.name), "Link mismatch for ${info.id}")
            assertEquals(info.id, resolveTransparencyHubDataSourceId(info.name))
        }
    }

    @Test
    fun dataSources_areHttpsAndNeverStagingOrRawJson() {
        DataSourceCatalog.dataSources.forEach { info ->
            assertTrue(info.homepageUrl.startsWith("https://"), "Bad homepage for ${info.id}")
            assertTrue(info.documentationUrl.startsWith("https://"), "Bad docs link for ${info.id}")
            assertFalse(info.documentationUrl.contains("qa-"), "QA/staging link for ${info.id}")
            assertFalse(info.documentationUrl.endsWith(".json"), "Raw JSON link for ${info.id}")
        }
    }

    @Test
    fun resolveDataSourceUrl_unknownSourceIsUrlEncodedSearch() {
        assertEquals(
            "https://www.google.com/search?q=Some+Local+Survey+%26+Co",
            resolveDataSourceUrl("Some Local Survey & Co")
        )
    }

    @Test
    fun resolveTransparencyHubDataSourceId_unknownIsNull() {
        assertEquals(null, resolveTransparencyHubDataSourceId("Some Local Survey"))
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
