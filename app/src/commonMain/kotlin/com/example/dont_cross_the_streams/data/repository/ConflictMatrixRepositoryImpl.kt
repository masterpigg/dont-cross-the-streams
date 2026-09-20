package com.example.dont_cross_the_streams.data.repository

import com.example.dont_cross_the_streams.data.datasource.MockBarrierDataSource
import com.example.dont_cross_the_streams.data.datasource.MockPopulationDataSource
import com.example.dont_cross_the_streams.data.datasource.MockWildlifeDataSource
import com.example.dont_cross_the_streams.domain.model.GeoLocation
import com.example.dont_cross_the_streams.domain.model.MitigationSolution
import com.example.dont_cross_the_streams.domain.model.PopulationDensityZone
import com.example.dont_cross_the_streams.domain.model.RiskLevel
import com.example.dont_cross_the_streams.domain.model.RiskMatrixScore
import com.example.dont_cross_the_streams.domain.model.SeveranceCause
import com.example.dont_cross_the_streams.domain.repository.ConflictMatrixRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.min
import kotlin.math.roundToInt

class ConflictMatrixRepositoryImpl : ConflictMatrixRepository {

    override fun getAllRiskMatrixScores(): Flow<List<RiskMatrixScore>> = flow {
        val scores = listOf(
            // Missouri Regional Risk Matrix Scores
            RiskMatrixScore(
                id = "rm_mo_001",
                region = "Central Missouri I-70 Corridor (Columbia - Boone Co.)",
                centerLocation = GeoLocation(38.9517, -92.3341),
                animalDensityScore = 84.0,
                barrierDensityScore = 92.0,
                collisionCount = 340,
                humanPressure = 78.0,
                compositeConflictScore = 88,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Severe ecological severance zone. I-70 concrete jersey barriers restrict deer and mesopredator crossing between Missouri River bottomlands and northern agricultural prairies.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "6-Lane Divided Interstate with Concrete Barriers",
                        description = "Continuous Jersey concrete walls prevent cross-freeway mammal movements along a 40-mile stretch.",
                        impactSeverity = "Critical"
                    ),
                    SeveranceCause(
                        title = "High Velocity Commuter Traffic (70+ mph)",
                        description = "Average Daily Traffic (ADT) exceeding 55,000 vehicles creates insurmountable mortality risk.",
                        impactSeverity = "High"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Boone County Wildlife Overpass Bridge",
                        type = "Wildlife Overpass",
                        description = "Construct vegetated 50m wide overpass near Perche Creek crossing to re-link fragmented timber corridors.",
                        estimatedCost = "$6.2M",
                        expectedRiskReductionPercent = 85,
                        implementationStatus = "Recommended"
                    ),
                    MitigationSolution(
                        title = "Directional Wildlife Fencing & Jump-outs",
                        type = "8-ft Ungulate Fencing",
                        description = "Install 12 miles of high-tensile wire fencing funneling animals to existing Perche Creek bridge underpass.",
                        estimatedCost = "$1.8M",
                        expectedRiskReductionPercent = 70,
                        implementationStatus = "In Planning"
                    )
                )
            ),
            RiskMatrixScore(
                id = "rm_mo_002",
                region = "Ozarks I-44 Foothills (Eureka - Rolla Corridor)",
                centerLocation = GeoLocation(38.2000, -91.0000),
                animalDensityScore = 91.0,
                barrierDensityScore = 88.0,
                collisionCount = 280,
                humanPressure = 65.0,
                compositeConflictScore = 86,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "High-velocity interstate cutting through core Ozark forest ecosystem. Threatens black bears, reintroduced elk, and white-tailed deer.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "High-Speed Divided Interstate",
                        description = "I-44 right-of-way fencing and steep rock cuts isolate northern and southern Ozark black bear populations.",
                        impactSeverity = "Critical"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Bourbeuse River Valley Eco-Culvert Expansion",
                        type = "Eco-Culvert / Underpass",
                        description = "Retrofit dry ledge footings under existing river bridges for large carnivore and ungulate passage.",
                        estimatedCost = "$2.4M",
                        expectedRiskReductionPercent = 75,
                        implementationStatus = "Recommended"
                    )
                )
            ),
            RiskMatrixScore(
                id = "rm_mo_003",
                region = "Osage Basin / Bagnell Hydroelectric Dam Interface",
                centerLocation = GeoLocation(38.2012, -92.6238),
                animalDensityScore = 76.0,
                barrierDensityScore = 95.0,
                collisionCount = 64,
                humanPressure = 82.0,
                compositeConflictScore = 82,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Hydroelectric dam aquatic severance. Blocks paddlefish and sturgeon upstream migration while shoreline tourism development fragments timber habitat.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "Hydroelectric Concrete Dam Wall",
                        description = "45m dam wall permanently halts native Osage River aquatic species spawning migrations.",
                        impactSeverity = "Critical"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Osage River Paddlefish Fish By-Pass Channel",
                        type = "Fish Ladder / By-Pass",
                        description = "Construct specialized bio-engineered fish ladder for large riverine species.",
                        estimatedCost = "$8.5M",
                        expectedRiskReductionPercent = 80,
                        implementationStatus = "Recommended"
                    )
                )
            ),

            // Illinois Regional Risk Matrix Score
            RiskMatrixScore(
                id = "rm_il_001",
                region = "LaRue-Pine Hills / Snake Road Migration Zone (Shawnee NF)",
                centerLocation = GeoLocation(37.5600, -89.4400),
                animalDensityScore = 98.0,
                barrierDensityScore = 62.0,
                collisionCount = 120,
                humanPressure = 45.0,
                compositeConflictScore = 85,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Critical seasonal Herpetological migration barrier. Limestone bluffs and LaRue Swamp bisected by Forest Road 345, requiring seasonal closure to prevent mass roadkill.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "Forest Road Bisecting Hibernacula and Wetland",
                        description = "2.5-mile gravel road creates impassable vehicular hazard during spring/autumn mass snake and amphibian migrations.",
                        impactSeverity = "Critical"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Seasonal Road Closure Management (Bi-annual)",
                        type = "Dynamic Road Closure",
                        description = "Maintain US Forest Service gates closing Snake Road for 2 months each spring and autumn.",
                        estimatedCost = "$15K",
                        expectedRiskReductionPercent = 95,
                        implementationStatus = "Constructed"
                    ),
                    MitigationSolution(
                        title = "Amphibian Ecoduct Tunnels & Barrier Walls",
                        type = "Eco-Culvert",
                        description = "Install low-profile polymer drift fences directing salamanders and turtles to micro-culverts under the roadbed.",
                        estimatedCost = "$450K",
                        expectedRiskReductionPercent = 88,
                        implementationStatus = "Recommended"
                    )
                )
            ),

            // Washington Regional Risk Matrix Score
            RiskMatrixScore(
                id = "rm_wa_001",
                region = "Puget Sound / Lake Washington Salmon Corridor (Ballard Locks)",
                centerLocation = GeoLocation(47.6655, -122.3972),
                animalDensityScore = 95.0,
                barrierDensityScore = 90.0,
                collisionCount = 95,
                humanPressure = 92.0,
                compositeConflictScore = 91,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Urban aquatic migration bottleneck. Salmonids traversing Puget Sound to freshwater spawning grounds face navigation locks and dense urban shoreline pressure.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "Navigation Lock & Dam Wall Complex",
                        description = "Commercial lock operations disrupt salinity gradient and impede adult salmon return runs.",
                        impactSeverity = "Critical"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Ballard Locks Fish Ladder Upgrade & Viewing Gallery",
                        type = "Fish Ladder",
                        description = "Maintain 21-weir fish ladder with acoustic attraction currents guiding Chinook and Sockeye salmon.",
                        estimatedCost = "$12.0M",
                        expectedRiskReductionPercent = 90,
                        implementationStatus = "Constructed"
                    )
                )
            ),

            // National Risk Matrix Scores
            RiskMatrixScore(
                id = "rm_001",
                region = "Colorado I-70 Mountain Corridor",
                centerLocation = GeoLocation(39.638, -105.897),
                animalDensityScore = 82.0,
                barrierDensityScore = 88.0,
                collisionCount = 184,
                humanPressure = 75.0,
                compositeConflictScore = 83,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Critical migration barrier for mule deer and elk herds across subalpine mountain passes.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "Interstate Highway Barrier",
                        description = "High-speed 4-lane divided highway with concrete jersey barriers.",
                        impactSeverity = "Critical"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Vail Pass Wildlife Overpass",
                        type = "Wildlife Overpass",
                        description = "Constructed vegetated overpass bridge spanning 4 lanes.",
                        estimatedCost = "$4.5M",
                        expectedRiskReductionPercent = 90,
                        implementationStatus = "Constructed"
                    )
                )
            ),
            RiskMatrixScore(
                id = "rm_002",
                region = "Greater Yellowstone Migration Bottleneck",
                centerLocation = GeoLocation(44.428, -110.588),
                animalDensityScore = 95.0,
                barrierDensityScore = 45.0,
                collisionCount = 126,
                humanPressure = 35.0,
                compositeConflictScore = 72,
                riskLevel = RiskLevel.MEDIUM,
                severityAnalysis = "High wildlife density seasonal corridor threatened by arterial state highways and fencing.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "Unfenced State Highway",
                        description = "High nighttime vehicle speeds along ungulate winter range movement paths.",
                        impactSeverity = "High"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Dynamic Animal Detection Speed Corridors",
                        type = "Dynamic Speed Corridor",
                        description = "Thermal sensors trigger reduced night speed limits when herds approach.",
                        estimatedCost = "$800K",
                        expectedRiskReductionPercent = 65,
                        implementationStatus = "In Planning"
                    )
                )
            ),
            RiskMatrixScore(
                id = "rm_003",
                region = "Upper Green River Basin Trappers Point",
                centerLocation = GeoLocation(42.851, -109.859),
                animalDensityScore = 88.0,
                barrierDensityScore = 70.0,
                collisionCount = 210,
                humanPressure = 50.0,
                compositeConflictScore = 79,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Historical 6,000-year-old pronghorn migration bottleneck compressed by natural rivers and highways.",
                severanceCauses = listOf(
                    SeveranceCause(
                        title = "Barbed Wire Range Fencing & Highway 191",
                        description = "Woven wire fences prevent pronghorn crawling underneath.",
                        impactSeverity = "High"
                    )
                ),
                mitigationSolutions = listOf(
                    MitigationSolution(
                        title = "Trappers Point Overpass Complex",
                        type = "Wildlife Overpass",
                        description = "Two overpasses and six underpasses with 12 miles of high fencing.",
                        estimatedCost = "$9.7M",
                        expectedRiskReductionPercent = 92,
                        implementationStatus = "Constructed"
                    )
                )
            )
        )
        emit(scores)
    }

    override fun getRiskMatrixForLocation(
        location: GeoLocation,
        regionName: String?
    ): Flow<RiskMatrixScore> = flow {
        // Calculate dynamic live score based on proximity to barriers, wildlife, and population
        val localBarriers = MockBarrierDataSource.barrierFeatures.filter {
            it.location.distanceToKm(location) <= 50.0
        }
        val localOccurrences = MockWildlifeDataSource.occurrences.filter {
            it.location.distanceToKm(location) <= 50.0
        }
        val localHotspots = MockWildlifeDataSource.collisionHotspots.filter {
            it.location.distanceToKm(location) <= 50.0
        }
        val localPop = MockPopulationDataSource.densityZones.find {
            it.boundingBox.contains(location)
        }

        val animalDensity = min(100.0, localOccurrences.sumOf { it.observationCount } * 1.5)
        val barrierDensity = min(100.0, localBarriers.size * 20.0 + localBarriers.count { it.impactLevel == com.example.dont_cross_the_streams.domain.model.ImpactLevel.SEVERE } * 25.0)
        val collisionCount = localHotspots.sumOf { it.incidentCount }
        val humanPressure = min(100.0, (localPop?.densityScore ?: 50.0) / 25.0)

        val compositeScore = calculateCompositeConflictScore(animalDensity, barrierDensity, collisionCount, humanPressure)
        val riskLevel = when {
            compositeScore >= 75 -> RiskLevel.HIGH
            compositeScore >= 45 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val generated = RiskMatrixScore(
            id = "dyn_${location.latitude}_${location.longitude}",
            region = regionName ?: "Custom Radius Matrix (${location.latitude.toString().take(6)}, ${location.longitude.toString().take(6)})",
            centerLocation = location,
            animalDensityScore = animalDensity,
            barrierDensityScore = barrierDensity,
            collisionCount = collisionCount,
            humanPressure = humanPressure,
            compositeConflictScore = compositeScore,
            riskLevel = riskLevel,
            severityAnalysis = "Calculated conflict score of $compositeScore/100 based on $collisionCount recorded crashes, ${localBarriers.size} linear infrastructure barriers, and ${localOccurrences.size} species occurrence points within 50km.",
            severanceCauses = listOf(
                SeveranceCause(
                    title = "Infrastructure Boundary Density",
                    description = "Detected ${localBarriers.size} linear transport/hydroelectric barriers restricting natural dispersal.",
                    impactSeverity = if (barrierDensity > 70) "Critical" else "Moderate"
                )
            ),
            mitigationSolutions = listOf(
                MitigationSolution(
                    title = "Targeted Eco-Corridor Enhancement",
                    type = "Multi-Modal Mitigation",
                    description = "Deploy directional wildlife fencing and retrofitted culvert underpasses along primary collision hot spots.",
                    estimatedCost = "$1.5M - $3.5M",
                    expectedRiskReductionPercent = 78,
                    implementationStatus = "Recommended"
                )
            )
        )
        emit(generated)
    }

    override fun getPopulationDensityZones(): Flow<List<PopulationDensityZone>> = flow {
        emit(MockPopulationDataSource.densityZones)
    }

    override fun calculateCompositeConflictScore(
        animalDensity: Double,
        barrierDensity: Double,
        collisionCount: Int,
        humanPressure: Double
    ): Int {
        val weightedAnimal = animalDensity * 0.30
        val weightedBarrier = barrierDensity * 0.30
        val normalizedCollision = min(100.0, collisionCount / 3.0) * 0.25
        val weightedHuman = humanPressure * 0.15

        val rawScore = weightedAnimal + weightedBarrier + normalizedCollision + weightedHuman
        return rawScore.roundToInt().coerceIn(0, 100)
    }
}
