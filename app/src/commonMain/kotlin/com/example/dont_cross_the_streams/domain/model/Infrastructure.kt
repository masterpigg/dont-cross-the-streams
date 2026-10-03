package com.example.dont_cross_the_streams.domain.model

/** Barriers and crossing structures returned together by one OpenStreetMap query. */
data class Infrastructure(
    val barriers: List<BarrierFeature>,
    val crossings: List<WildlifeCrossing>
)

/** 2020 Census population and land area for an area. */
data class AreaPopulation(val population: Int, val landAreaKm2: Double) {
    val densityPerKm2: Double get() = if (landAreaKm2 > 0) population / landAreaKm2 else 0.0
}
