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
        val defaultScores = listOf(
            RiskMatrixScore(
                id = "rms_mo_001",
                region = "Missouri I-70 & Deer Corridor",
                centerLocation = GeoLocation(38.9517, -92.3341),
                animalDensityScore = 92.0,
                barrierDensityScore = 88.0,
                collisionCount = 340,
                humanPressure = 72.0,
                compositeConflictScore = 91,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Extreme white-tailed deer collision density across 6-lane interstate. Jersey barrier walls trap animals on high-speed travel lanes.",
                severanceCauses = listOf(
                    SeveranceCause("6-Lane Interstate Barrier", "Continuous high-volume traffic separates agricultural feed grounds from river bluff bedding cover.", "Critical"),
                    SeveranceCause("Concrete Median Walls", "Prevents trapped animals from crossing safely once entering right-of-way.", "Critical")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("I-70 Missouri Wildlife Overpass", "Wildlife Overpass", "Construct 120ft vegetated overpass bridge between Columbia and Kingdom City.", "$12M - $15M", 92, "In Planning"),
                    MitigationSolution("High 8ft Exclusion Fencing with Jump-Outs", "Directional Fencing", "Install continuous guide fencing forcing deer into major creek culverts.", "$1.8M", 85, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_mo_002",
                region = "Ozarks & Bagnell Dam Aquatic Barrier",
                centerLocation = GeoLocation(38.2012, -92.6238),
                animalDensityScore = 82.0,
                barrierDensityScore = 94.0,
                collisionCount = 88,
                humanPressure = 65.0,
                compositeConflictScore = 85,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Severe aquatic species fragmentation on Osage River. Hydroelectric dam blocks ancient paddlefish migration while I-44 cuts through black bear and elk territory.",
                severanceCauses = listOf(
                    SeveranceCause("Osage River Dam Impediment", "Hydroelectric dam completely severs upstream paddlefish spawning runs.", "Critical"),
                    SeveranceCause("High Speed Highway & Resort Sprawl", "Fencing and shoreline development obstruct terrestrial mammal movements.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Bagnell Dam Fish Passage & Lift Retrofit", "Fish Ladder", "Install automated high-capacity fish elevator for paddlefish and sturgeon.", "$18M", 80, "In Planning"),
                    MitigationSolution("US-60 Ozark Scenic Underpass Eco-Culvert", "Eco-Culvert", "Arched highway underpasses with directional guiding fences for black bear and elk.", "$4.2M", 88, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_mo_003",
                region = "St. Louis Metro Encroachment",
                centerLocation = GeoLocation(38.6270, -90.1994),
                animalDensityScore = 55.0,
                barrierDensityScore = 96.0,
                collisionCount = 145,
                humanPressure = 92.0,
                compositeConflictScore = 82,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Dense urban sprawl along Mississippi/Missouri confluence. High rail and highway noise blocks avian and amphibian movement along key flyway.",
                severanceCauses = listOf(
                    SeveranceCause("Urban Soundwalls & Multilane Freeways", "Isolates suburban park patches and prevents terrestrial mammal dispersal.", "High"),
                    SeveranceCause("High Density Freight Rail Corridors", "Disturbs nesting birds and creates steep ballast hazards.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Confluence River Corridor Greenways", "Directional Fencing", "Preserve contiguous riparian green belts beneath river bridge approach spans.", "$6.5M", 84, "Recommended"),
                    MitigationSolution("Smart Acoustic Noise Mitigation Barriers", "Sound Wall", "Vegetated acoustic sound buffers reducing highway noise spillover.", "$2.1M", 72, "Implemented")
                )
            ),
            RiskMatrixScore(
                id = "rms_001",
                region = "I-70 Vail Pass Mountain Corridor",
                centerLocation = GeoLocation(39.638, -105.897),
                animalDensityScore = 85.0,
                barrierDensityScore = 92.0,
                collisionCount = 184,
                humanPressure = 78.0,
                compositeConflictScore = 89,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Severe habitat fragmentation caused by 6-lane interstate bisecting elk and mule deer migration routes with high vehicle velocity and continuous barrier walls.",
                severanceCauses = listOf(
                    SeveranceCause("6-Lane Mountain Interstate", "High-speed multi-lane traffic creates a lethal physical barrier.", "Critical"),
                    SeveranceCause("Continuous Concrete Jersey Barriers", "Blocks smaller wildlife and prevents escape once animals step onto roadway.", "High"),
                    SeveranceCause("High Noise & Light Pollution", "Disrupts nocturnal animal movements along riparian draws.", "Moderate")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Large Wildlife Overpass", "Wildlife Overpass", "Construct 150ft wide vegetated arch bridge over I-70 with natural soil and fencing.", "$15M - $18M", 90, "In Planning"),
                    MitigationSolution("Retrofitted Eco-Culverts", "Eco-Culvert", "Enlarge existing stream culverts with dry bench ledges for small-to-medium mammals.", "$2.5M", 75, "Recommended"),
                    MitigationSolution("Variable Dynamic Speed Corridors", "Dynamic Speed Corridor", "Deploy electronic warning signs reducing speed limit to 45 mph during peak migration nights.", "$800K", 60, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_002",
                region = "US Highway 191 Trappers Point",
                centerLocation = GeoLocation(42.851, -109.859),
                animalDensityScore = 95.0,
                barrierDensityScore = 75.0,
                collisionCount = 210,
                humanPressure = 65.0,
                compositeConflictScore = 86,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Critical migration choke point where 5,000+ pronghorn and mule deer cross highway and woven wire fence barriers, causing recurring mass strikes.",
                severanceCauses = listOf(
                    SeveranceCause("Bottleneck Topography & Fencing", "Natural pinch point flanked by woven wire livestock fencing that funnels animals onto roadway.", "Critical"),
                    SeveranceCause("High Speed Rural Highway", "Truck transport corridor with speeds exceeding 70 mph.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Trappers Point Wildlife Overpasses", "Wildlife Overpass", "Dual dedicated pronghorn overpass bridges with directional funnel fencing.", "$12M", 92, "Constructed"),
                    MitigationSolution("Wildlife-Friendly Fence Conversions", "Directional Fencing", "Replace top and bottom fence wires with smooth wire at 18 inches clearance.", "$400K", 70, "Implemented")
                )
            ),
            RiskMatrixScore(
                id = "rms_003",
                region = "Yellowstone Highway Corridor",
                centerLocation = GeoLocation(44.428, -110.588),
                animalDensityScore = 90.0,
                barrierDensityScore = 40.0,
                collisionCount = 126,
                humanPressure = 35.0,
                compositeConflictScore = 68,
                riskLevel = RiskLevel.MEDIUM,
                severityAnalysis = "Moderate-to-high conflict driven by seasonal ungulate migration movements across 2-lane park access highways intersecting grizzly bear and bison ranges.",
                severanceCauses = listOf(
                    SeveranceCause("Seasonal Tourist Congestion", "Intermittent heavy vehicle traffic during herd migration periods.", "High"),
                    SeveranceCause("Unfenced Highway Segments", "Animals cross unpredictably at night along blind forest curves.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Thermal Animal Detection Warning Systems", "Dynamic Speed Corridor", "Infrared roadside sensors triggering flashing warning lights when bison/elk approach road.", "$1.2M", 80, "In Planning"),
                    MitigationSolution("Underpass Eco-Culverts with Funnel Fencing", "Eco-Culvert", "Expanded bottom culverts for grizzly and ungulate crossing.", "$3.8M", 85, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_004",
                region = "US 101 Liberty Canyon Corridor",
                centerLocation = GeoLocation(34.134, -118.321),
                animalDensityScore = 45.0,
                barrierDensityScore = 98.0,
                collisionCount = 12,
                humanPressure = 95.0,
                compositeConflictScore = 81,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Extreme genetic bottleneck for mountain lions and bobcats caused by 10-lane freeway barrier in high human density suburban interface.",
                severanceCauses = listOf(
                    SeveranceCause("10-Lane Freeway Barrier", "Impassable urban freeway cutting off Santa Monica Mountains wildlife population.", "Critical"),
                    SeveranceCause("Suburban Sprawl & Ambient Light", "Dense residential development restricting movement corridors.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Wallis Annenberg Wildlife Crossing", "Wildlife Overpass", "World's largest landscaped 210ft green bridge connecting fragmented ecosystem.", "$90M", 95, "Constructed"),
                    MitigationSolution("Sound and Light Attenuation Walls", "Directional Fencing", "Vegetated sound barriers blocking freeway glare and noise.", "$4M", 75, "Constructed")
                )
            ),
            RiskMatrixScore(
                id = "rms_005",
                region = "California Highway 17 Crossing",
                centerLocation = GeoLocation(37.121, -121.985),
                animalDensityScore = 65.0,
                barrierDensityScore = 88.0,
                collisionCount = 142,
                humanPressure = 82.0,
                compositeConflictScore = 79,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Winding mountain highway dividing Santa Cruz Mountains. High deer and puma strikes due to narrow shoulders and concrete center dividers.",
                severanceCauses = listOf(
                    SeveranceCause("Center Concrete Median Barriers", "Traps animals in high-speed lanes when crossing mountain pass.", "Critical"),
                    SeveranceCause("Blind Mountain Curves", "Reduced driver visibility at 55+ mph speeds.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Laurel Curve Wildlife Underpass", "Eco-Culvert", "Large arched concrete underpass with guide fencing through redwood forest.", "$12.5M", 88, "In Planning"),
                    MitigationSolution("Directional Escape Ramps & Jump-Outs", "Directional Fencing", "One-way earthen ramps allowing trapped animals to exit highway right-of-way.", "$600K", 80, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_006",
                region = "Snake River Dams & Aquatic Corridor",
                centerLocation = GeoLocation(46.231, -118.882),
                animalDensityScore = 88.0,
                barrierDensityScore = 95.0,
                collisionCount = 5,
                humanPressure = 70.0,
                compositeConflictScore = 84,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Severe aquatic ecological fragmentation. Four hydroelectric dams restrict wild salmon, steelhead, and sturgeon spawning migrations.",
                severanceCauses = listOf(
                    SeveranceCause("Hydroelectric Dam Spillways & Turbines", "Blocks upstream adult fish passage and causes mortality in juvenile smolts.", "Critical"),
                    SeveranceCause("Reservoir Thermal Alteration", "Elevated water temperatures disrupt migratory timing and increase disease.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Advanced Fish Ladders & Fish Lifts", "Fish Ladder", "High-capacity fish passage ladders with automated counting sensors.", "$45M", 78, "Constructed"),
                    MitigationSolution("Surface Bypass Collectors & Spillway Flumes", "Fish Ladder", "Guides juvenile salmon around turbine intakes into safe bypass channels.", "$28M", 85, "Implemented")
                )
            ),
            RiskMatrixScore(
                id = "rms_007",
                region = "Florida Panther Crossing (SR 29 & I-75)",
                centerLocation = GeoLocation(26.152, -81.352),
                animalDensityScore = 78.0,
                barrierDensityScore = 82.0,
                collisionCount = 34,
                humanPressure = 68.0,
                compositeConflictScore = 77,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Vehicle strikes remain the leading cause of mortality for the endangered Florida Panther along rural highways cutting through Everglades reserves.",
                severanceCauses = listOf(
                    SeveranceCause("High-Speed Two-Lane Rural Arterials", "Nighttime vehicle strikes in unlit panther movement corridors.", "Critical"),
                    SeveranceCause("Missing Boundary Fencing Segments", "Panthers cross road where cattle fencing ends.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Panther Wildlife Underpasses with 10ft Fencing", "Eco-Culvert", "Wide bridge underpasses lined with high continuous fencing and barbed wire top.", "$8.5M", 92, "Constructed"),
                    MitigationSolution("Nighttime Slow-Speed Enforcement Corridors", "Dynamic Speed Corridor", "Automated speed enforcement zones (45 mph at night) in primary panther range.", "$500K", 70, "Implemented")
                )
            ),
            RiskMatrixScore(
                id = "rms_008",
                region = "Appalachian Pass Corridor (Pigeon River Gorge I-40)",
                centerLocation = GeoLocation(35.611, -83.489),
                animalDensityScore = 70.0,
                barrierDensityScore = 80.0,
                collisionCount = 95,
                humanPressure = 60.0,
                compositeConflictScore = 72,
                riskLevel = RiskLevel.MEDIUM,
                severityAnalysis = "Steep canyon terrain forces black bears, elk, and white-tailed deer onto roadway lanes enclosed by high highway safety barriers.",
                severanceCauses = listOf(
                    SeveranceCause("Steep Canyon Topography & Concrete Retaining Walls", "Animals cannot scale steep highway cuts and become trapped on lanes.", "High"),
                    SeveranceCause("High Heavy-Truck Freight Traffic", "Non-stop commercial transit along interstate mountain pass.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Pigeon River Gorge Wildlife Overpass Bridge", "Wildlife Overpass", "Proposed green overpass connecting Great Smoky Mountains with Pisgah National Forest.", "$14M", 88, "In Planning"),
                    MitigationSolution("Bridge Underpass Retrofits", "Eco-Culvert", "Adding dry shelf footpaths beneath river bridges for black bears and bobcats.", "$1.8M", 82, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_il_001",
                region = "Shawnee NF Snake Road Migration",
                centerLocation = GeoLocation(37.5600, -89.4400),
                animalDensityScore = 96.0,
                barrierDensityScore = 85.0,
                collisionCount = 120,
                humanPressure = 32.0,
                compositeConflictScore = 88,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Critical mass biannual reptile and amphibian migration between LaRue-Pine Hills limestone bluffs and LaRue Swamp. Road traffic poses extreme mortality risk during seasonal crossings.",
                severanceCauses = listOf(
                    SeveranceCause("Bi-Annual Road Crossing", "LaRue Road 345 cuts across the exact 2.5 mile migration vector between winter hibernation bluffs and summer wetland habitat.", "Critical"),
                    SeveranceCause("Vehicle Strike Mortality", "High mortality rate for cottonmouths, rattlesnakes, and salamanders when road is unclosed.", "Critical")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("USFS Seasonal Road Closure (Oct & Apr)", "Directional Fencing", "Bi-annual 2-month closure of LaRue Road 345 to all motor vehicles enforced by US Forest Service.", "$50K", 98, "Implemented"),
                    MitigationSolution("Eco-Passage Underpasses with Drift Fencing", "Eco-Culvert", "Construct permanent low-clearance reptile underpasses with continuous drift walls.", "$1.2M", 90, "Recommended")
                )
            ),
            RiskMatrixScore(
                id = "rms_wa_001",
                region = "Ballard Locks & Salmon Passage",
                centerLocation = GeoLocation(47.6700, -122.4000),
                animalDensityScore = 94.0,
                barrierDensityScore = 92.0,
                collisionCount = 95,
                humanPressure = 88.0,
                compositeConflictScore = 91,
                riskLevel = RiskLevel.HIGH,
                severityAnalysis = "Severe aquatic migration bottleneck at Hiram M. Chittenden Locks connecting Lake Washington to Puget Sound. Chinook and Sockeye salmon runs depend entirely on fish ladder passage.",
                severanceCauses = listOf(
                    SeveranceCause("Salinity & Elevation Hydraulic Barrier", "Double locks barrier interrupts natural salmon smolt and adult spawning movement between marine and freshwater.", "Critical"),
                    SeveranceCause("Pinniped Predation Chokepoint", "California sea lions and harbor seals exploit narrow lock entrance to prey on migrating salmon.", "High")
                ),
                mitigationSolutions = listOf(
                    MitigationSolution("Ballard Locks Fish Ladder Modernization", "Fish Ladder", "Upgraded 21-weir fish ladder with viewing chamber, attraction flow pumps, and temperature controls.", "$14M", 92, "Implemented"),
                    MitigationSolution("Acoustic Deterrent Pinniped System", "Dynamic Speed Corridor", "Non-harmful acoustic harassment devices to prevent sea lion predation at lock entrance.", "$800K", 75, "In Planning")
                )
            )
        )
        emit(defaultScores)
    }

    override fun getRiskMatrixForLocation(
        location: GeoLocation,
        regionName: String?
    ): Flow<RiskMatrixScore> = flow {
        // Calculate dynamic proximity scores within 25km radius
        val radius = 25.0
        val nearbyOccurrences = MockWildlifeDataSource.occurrences.filter { it.location.distanceToKm(location) <= radius }
        val nearbyHotspots = MockWildlifeDataSource.collisionHotspots.filter { it.location.distanceToKm(location) <= radius }
        val nearbyBarriers = MockBarrierDataSource.barrierFeatures.filter { it.location.distanceToKm(location) <= radius }
        val nearbyPopZones = MockPopulationDataSource.densityZones.filter { it.centerLocation.distanceToKm(location) <= radius * 2 }

        val animalDensityScore = min(100.0, nearbyOccurrences.sumOf { it.observationCount } * 2.5)
        val barrierDensityScore = min(100.0, nearbyBarriers.size * 22.0)
        val collisionCount = nearbyHotspots.sumOf { it.incidentCount }
        val humanPressure = min(100.0, (nearbyPopZones.maxOfOrNull { it.densityScore } ?: 50.0) / 15.0)

        val composite = calculateCompositeConflictScore(
            animalDensity = animalDensityScore,
            barrierDensity = barrierDensityScore,
            collisionCount = collisionCount,
            humanPressure = humanPressure
        )

        val level = when {
            composite >= 75 -> RiskLevel.HIGH
            composite >= 45 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val regionTitle = regionName ?: "Location Radius (${location.latitude.toString().take(6)}, ${location.longitude.toString().take(6)})"

        val analysis = when (level) {
            RiskLevel.HIGH -> "Critical conflict potential. High spatial overlap of wildlife movements, dense linear barriers, and past incident records requiring wildlife crossing infrastructure."
            RiskLevel.MEDIUM -> "Moderate conflict threat. Intersecting migratory pathways or localized barrier hazards requiring warning systems or speed mitigation."
            RiskLevel.LOW -> "Low immediate conflict risk. Low human footprint or minimal linear barrier intrusion detected in immediate area."
        }

        val causes = listOf(
            SeveranceCause("Spatial Barrier Density", "Local highway or dam infrastructure within 25km radius.", level.name),
            SeveranceCause("Wildlife Corridor Intersect", "Active wildlife sighting and movement records in proximity.", level.name)
        )

        val mitigations = listOf(
            MitigationSolution("Targeted Eco-Culvert / Crossing Structure", "Eco-Culvert", "Install local crossing structure based on primary species movement.", "$3M - $5M", 80, "Recommended"),
            MitigationSolution("Driver Speed & Wildlife Hazard Warning Signs", "Dynamic Speed Corridor", "Seasonal electronic warning system during high movement hours.", "$400K", 65, "Recommended")
        )

        emit(
            RiskMatrixScore(
                id = "rms_calc_${System.currentTimeMillis()}",
                region = regionTitle,
                centerLocation = location,
                animalDensityScore = animalDensityScore,
                barrierDensityScore = barrierDensityScore,
                collisionCount = collisionCount,
                humanPressure = humanPressure,
                compositeConflictScore = composite,
                riskLevel = level,
                severityAnalysis = analysis,
                severanceCauses = causes,
                mitigationSolutions = mitigations
            )
        )
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
        val normalizedCollisionScore = min(100.0, collisionCount * 0.5)
        // Weighted formula: 30% animal density, 30% barrier density, 25% collision history, 15% human pressure
        val rawScore = (animalDensity * 0.30) +
                (barrierDensity * 0.30) +
                (normalizedCollisionScore * 0.25) +
                (humanPressure * 0.15)

        return min(100, rawScore.roundToInt().coerceAtLeast(0))
    }
}
