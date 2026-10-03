package com.example.dont_cross_the_streams.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private val client: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}

actual suspend fun httpGetText(url: String): String? = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url(url)
            // OpenStreetMap's Overpass API asks clients to identify themselves.
            .header("User-Agent", "DontCrossTheStreams/1.0 (wildlife research app)")
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    } catch (e: java.io.IOException) {
        null
    }
}
