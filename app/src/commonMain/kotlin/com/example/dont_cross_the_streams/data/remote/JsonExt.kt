package com.example.dont_cross_the_streams.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal fun parseJsonOrNull(text: String?): JsonElement? =
    if (text.isNullOrBlank()) null else try {
        Json.parseToJsonElement(text)
    } catch (_: Exception) {
        null
    }

internal fun JsonElement?.obj(): JsonObject? = this as? JsonObject
internal fun JsonElement?.arr(): JsonArray? = this as? JsonArray
internal fun JsonObject.o(key: String): JsonObject? = this[key] as? JsonObject
internal fun JsonObject.a(key: String): JsonArray? = this[key] as? JsonArray

internal fun JsonObject.str(key: String): String? {
    val p = this[key] as? JsonPrimitive ?: return null
    if (p is JsonNull) return null
    return p.content.takeIf { it.isNotBlank() }
}

internal fun JsonObject.num(key: String): Double? = (this[key] as? JsonPrimitive)?.let {
    if (it is JsonNull) null else it.content.trim().trim('\'').toDoubleOrNull()
}

/** ArcGIS servers may change the case of statistic/field names; look keys up case-insensitively. */
internal fun JsonObject.numIgnoreCase(key: String): Double? =
    entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.let { (it.value as? JsonPrimitive)?.content?.toDoubleOrNull() }

internal fun JsonObject.strIgnoreCase(key: String): String? =
    entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.let { entry ->
        (entry.value as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content?.trim()?.trim('\'')?.trim()?.takeIf { it.isNotBlank() }
    }

internal fun JsonElement.doubleAt(index: Int): Double? = (this as? JsonArray)?.getOrNull(index)?.let {
    (it as? JsonPrimitive)?.content?.toDoubleOrNull()
}

internal fun iconicTaxonToGroup(iconicTaxonName: String?): String = when (iconicTaxonName) {
    "Mammalia" -> "Mammals"
    "Aves" -> "Birds"
    "Reptilia" -> "Reptiles"
    "Amphibia" -> "Amphibians"
    "Actinopterygii" -> "Fish"
    else -> "Other"
}
