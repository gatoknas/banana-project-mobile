package org.banana.project.data.network

import okhttp3.Interceptor
import okhttp3.Response
import org.banana.project.utils.AppLogger
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceAndTelemetryInterceptor @Inject constructor() : Interceptor {

    companion object {
        const val TRACE_ID_HEADER = "X-Trace-ID"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Extract existing trace header or generate a new UUID
        val traceId = originalRequest.header(TRACE_ID_HEADER) ?: AppLogger.generateTraceId()
        
        val newRequest = originalRequest.newBuilder()
            .header(TRACE_ID_HEADER, traceId)
            .build()

        val startTime = System.currentTimeMillis()
        try {
            val response = chain.proceed(newRequest)
            val duration = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                AppLogger.w(
                    event = "http_request_unsuccessful",
                    message = "${newRequest.method} ${newRequest.url.encodedPath} returned status ${response.code} (${duration}ms)",
                    traceId = traceId
                )
            }
            return response
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            AppLogger.e(
                event = "http_request_network_failure",
                message = "${newRequest.method} ${newRequest.url.encodedPath} failed after ${duration}ms: ${e.message}",
                throwable = e,
                traceId = traceId
            )
            throw e
        }
    }
}
