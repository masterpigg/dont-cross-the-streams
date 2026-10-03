package com.example.dont_cross_the_streams.data.remote

/**
 * GETs [url] and returns the body, or null on any network/HTTP failure. Each platform uses its own
 * client (browser fetch, OkHttp, NSURLSession) so all parsing can live in shared code.
 */
expect suspend fun httpGetText(url: String): String?

/** Injectable form of [httpGetText] so the API clients can be tested with recorded responses. */
typealias HttpGet = suspend (url: String) -> String?

/** Percent-encodes a query parameter value (RFC 3986 unreserved characters are kept). */
fun encodeUrlParam(value: String): String = buildString {
    for (byte in value.encodeToByteArray()) {
        val v = byte.toInt() and 0xFF
        val c = v.toChar()
        if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '-' || c == '_' || c == '.' || c == '~') {
            append(c)
        } else {
            append('%')
            append("0123456789ABCDEF"[v shr 4])
            append("0123456789ABCDEF"[v and 0x0F])
        }
    }
}
