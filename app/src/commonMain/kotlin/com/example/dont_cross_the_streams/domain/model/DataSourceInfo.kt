package com.example.dont_cross_the_streams.domain.model

enum class DataSourceCategory {
    WILDLIFE_OBSERVATION,
    TELEMETRY_MOVEMENT,
    ANIMAL_COLLISION,
    INFRASTRUCTURE_BARRIER,
    HUMAN_FOOTPRINT,
    ECOLOGICAL_SPECIES
}

enum class AuthType {
    NONE_PUBLIC,
    API_KEY,
    OAUTH2,
    DATASET_DOWNLOAD
}

data class DataSourceInfo(
    val id: String,
    val name: String,
    val category: DataSourceCategory,
    val description: String,
    val baseURL: String,
    val keyEndpoints: List<String>,
    val documentationUrl: String,
    val authType: AuthType,
    val keyUtility: String,
    val isLiveApiAvailable: Boolean = true
)
