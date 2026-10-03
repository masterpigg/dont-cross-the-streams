package com.example.dont_cross_the_streams.ui.map

import com.example.dont_cross_the_streams.domain.model.BarrierFeature
import com.example.dont_cross_the_streams.domain.model.CollisionHotspot
import com.example.dont_cross_the_streams.domain.model.CollisionReport
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.WildlifeCrossing
import com.example.dont_cross_the_streams.domain.model.WildlifeOccurrence

sealed interface MapFeatureSelection {
    val id: String
    val title: String
    val subtitle: String
    val location: GeoLocation

    data class Wildlife(val occurrence: WildlifeOccurrence) : MapFeatureSelection {
        override val id: String = occurrence.id
        override val title: String = occurrence.commonName ?: occurrence.species
        override val subtitle: String = "${occurrence.taxonGroup} • ${occurrence.observationCount} observations"
        override val location: GeoLocation = occurrence.location
    }

    data class Hotspot(val hotspot: CollisionHotspot) : MapFeatureSelection {
        override val id: String = hotspot.id
        override val title: String = hotspot.highwayOrRouteName ?: "Collision Hotspot"
        override val subtitle: String = "${hotspot.incidentCount} incidents • ${hotspot.primarySpeciesAffected}"
        override val location: GeoLocation = hotspot.location
    }

    data class Barrier(val barrier: BarrierFeature) : MapFeatureSelection {
        override val id: String = barrier.id
        override val title: String = barrier.name
        override val subtitle: String = "${barrier.type.name} • Impact: ${barrier.impactLevel.name}"
        override val location: GeoLocation = barrier.location
    }

    data class Population(val zone: PopulationDensityZone) : MapFeatureSelection {
        override val id: String = zone.id
        override val title: String = zone.regionName
        override val subtitle: String = "${zone.urbanLevel.name} • ${zone.densityScore.toInt()} people/km²"
        override val location: GeoLocation = zone.centerLocation
    }

    data class Crossing(val crossing: WildlifeCrossing) : MapFeatureSelection {
        override val id: String = crossing.id
        override val title: String = crossing.name
        override val subtitle: String = "Wildlife ${crossing.structureType.name.lowercase().replace('_', ' ')} • ${crossing.targetSpecies}"
        override val location: GeoLocation = crossing.location
    }

    data class Collision(val report: CollisionReport) : MapFeatureSelection {
        override val id: String = report.id
        override val title: String = report.commonName ?: report.species
        override val subtitle: String = "Reported dead${report.observedOn?.let { " on $it" } ?: ""} • ${report.taxonGroup}"
        override val location: GeoLocation = report.location
    }
}
