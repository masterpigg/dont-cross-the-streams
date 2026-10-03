package com.example.dont_cross_the_streams.data.remote

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithURL
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual suspend fun httpGetText(url: String): String? = suspendCancellableCoroutine { continuation ->
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }
    val task = NSURLSession.sharedSession.dataTaskWithURL(nsUrl) { data, response, error ->
        val status = (response as? NSHTTPURLResponse)?.statusCode ?: 0L
        val body = if (error == null && data != null && status in 200L..299L) {
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        } else {
            null
        }
        continuation.resume(body)
    }
    continuation.invokeOnCancellation { task.cancel() }
    task.resume()
}
