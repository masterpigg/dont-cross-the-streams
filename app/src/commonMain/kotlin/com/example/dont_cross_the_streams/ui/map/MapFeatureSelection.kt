package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

sealed interface MapFeatureSelection {
    val id: String
    val title: String
    val subtitle: String

    data class Wildlife(val occurrence: WildlifeOccurrence) : MapFeatureSelection {
        override val id: String = occurrence.id
        override val title: String = occurrence.commonName ?: occurrence.species
        override val subtitle: String = "${occurrence.taxonGroup} • ${occurrence.observationCount} observations"
    }

    data class Hotspot(val hotspot: CollisionHotspot) : MapFeatureSelection {
        override val id: String = hotspot.id
        override val title: String = hotspot.highwayOrRouteName ?: "Collision Hotspot"
        override val subtitle: String = "${hotspot.incidentCount} incidents • ${hotspot.primarySpeciesAffected}"
    }

    data class Barrier(val barrier: BarrierFeature) : MapFeatureSelection {
        override val id: String = barrier.id
        override val title: String = barrier.name
        override val subtitle: String = "${barrier.type.name} • Impact: ${barrier.impactLevel.name}"
    }

    data class Population(val zone: PopulationDensityZone) : MapFeatureSelection {
        override val id: String = zone.regionName
        override val title: String = zone.regionName
        override val subtitle: String = "${zone.urbanLevel.name} • ${zone.densityScore.toInt()} people/km²"
    }
}
