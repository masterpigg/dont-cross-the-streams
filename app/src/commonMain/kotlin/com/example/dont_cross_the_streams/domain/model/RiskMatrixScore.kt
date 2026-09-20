package com.example.dont_cross_the_streams.domain.model

enum class RiskLevel {
    HIGH,
    MEDIUM,
    LOW
}

data class SeveranceCause(
    val title: String,
    val description: String,
    val impactSeverity: String // e.g., "Critical", "High", "Moderate"
)

data class MitigationSolution(
    val title: String,
    val type: String, // e.g., "Wildlife Overpass", "Eco-Culvert", "Fish Ladder", "Dynamic Speed Corridor"
    val description: String,
    val estimatedCost: String,
    val expectedRiskReductionPercent: Int, // 0-100%
    val implementationStatus: String // "Recommended", "In Planning", "Constructed"
)

data class RiskMatrixScore(
    val id: String,
    val region: String,
    val centerLocation: GeoLocation,
    val animalDensityScore: Double, // 0 - 100
    val barrierDensityScore: Double, // 0 - 100
    val collisionCount: Int,
    val humanPressure: Double, // 0 - 100
    val compositeConflictScore: Int, // 0 - 100
    val riskLevel: RiskLevel,
    val severityAnalysis: String,
    val severanceCauses: List<SeveranceCause> = emptyList(),
    val mitigationSolutions: List<MitigationSolution> = emptyList()
)
