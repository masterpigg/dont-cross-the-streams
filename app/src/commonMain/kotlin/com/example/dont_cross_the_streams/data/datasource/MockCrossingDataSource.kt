package com.example.dont_cross_the_streams.data.datasource

import com.example.dont_cross_the_streams.domain.model.CrossingStructureType
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing

// Coordinates are approximate (structure-level precision, not survey grade).
object MockCrossingDataSource {

    val crossings = listOf(
        WildlifeCrossing(
            id = "xing_wcpp_001",
            name = "I-90 & US-93 Federal Wildlife Crossing Pilot Corridor (WCPP)",
            structureType = CrossingStructureType.OVERPASS,
            location = GeoLocation(46.8700, -114.0000),
            targetSpecies = "Elk, Mule Deer & Grizzly Bear",
            source = "USDOT FHWA Wildlife Crossings Pilot Program (WCPP)",
            structureCount = 3,
            description = "Federally funded wildlife overpass bridges, directional fencing, and eco-culverts constructed under the USDOT Bipartisan Infrastructure Law WCPP grant."
        ),
        WildlifeCrossing(
            id = "xing_caltrans_001",
            name = "Wallis Annenberg Wildlife Crossing at Liberty Canyon (US-101)",
            structureType = CrossingStructureType.OVERPASS,
            location = GeoLocation(34.1395, -118.7360),
            targetSpecies = "Mountain Lion, Bobcat & Mule Deer",
            source = "Caltrans Wildlife Crossing & Mitigation GIS API",
            description = "World's largest vegetated wildlife overpass, bridging 10 lanes of US-101 in Agoura Hills to reconnect Santa Monica Mountains cougar and bobcat habitat."
        ),
        WildlifeCrossing(
            id = "xing_cdot_001",
            name = "State Highway 9 Kremmling–Silverthorne Wildlife Crossings",
            structureType = CrossingStructureType.OVERPASS,
            location = GeoLocation(39.9000, -106.3800),
            targetSpecies = "Mule Deer & Elk",
            source = "CDOT Wildlife Mitigations & Overpasses API",
            structureCount = 7,
            description = "Two overpasses, five underpasses and 8-foot directional fencing along SH-9, credited with cutting deer and elk vehicle collisions by roughly 90%."
        ),
        WildlifeCrossing(
            id = "xing_wsdot_001",
            name = "I-90 Snoqualmie Pass East Wildlife Overcrossing",
            structureType = CrossingStructureType.OVERPASS,
            location = GeoLocation(47.3100, -121.2800),
            targetSpecies = "Elk, Black Bear & Salmon (culverts)",
            source = "WSDOT Fish Passage Barrier & Wildlife Crossing API",
            description = "66-foot wide vegetated overpass over 6 lanes of I-90, part of a corridor of over- and undercrossings paired with fish-passable culverts."
        ),
        WildlifeCrossing(
            id = "xing_wydot_001",
            name = "Trappers Point Pronghorn Crossings (US-191)",
            structureType = CrossingStructureType.OVERPASS,
            location = GeoLocation(42.8800, -109.9900),
            targetSpecies = "Pronghorn & Mule Deer",
            source = "Wyoming DOT",
            structureCount = 8,
            description = "Two overpasses and six underpasses with fencing on US-191 near Pinedale, built across the bottleneck of the Path of the Pronghorn migration."
        ),
        WildlifeCrossing(
            id = "xing_fl_001",
            name = "SR-29 & I-75 Florida Panther Underpasses",
            structureType = CrossingStructureType.UNDERPASS,
            location = GeoLocation(26.2000, -81.3500),
            targetSpecies = "Florida Panther & Black Bear",
            source = "Florida Fish and Wildlife Conservation Commission",
            structureCount = 6,
            description = "Fenced wildlife underpasses beneath SR-29 and I-75 (Alligator Alley), the primary mitigation for vehicle strikes, the leading known cause of panther deaths."
        )
    )
}
