package com.example.dont_cross_the_streams.data.remote

import kotlinx.coroutines.await
import kotlin.js.Promise

// All of the APIs used here send CORS headers, so the browser can call them directly from
// GitHub Pages. Resolves to null on network errors and non-2xx responses.
@JsFun("""
(url) => fetch(url)
    .then(res => res.ok ? res.text() : null)
    .catch(() => null)
""")
private external fun fetchTextJs(url: String): Promise<JsString?>

actual suspend fun httpGetText(url: String): String? =
    fetchTextJs(url).await<JsString?>()?.toString()
