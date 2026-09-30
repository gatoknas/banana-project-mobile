package org.banana.project.utils

import android.os.Build
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@Serializable
enum class MobileLogLevel {
    DEBUG, INFO, WARN, ERROR, FATAL
}

@Serializable
data class DeviceContext(
    val brand: String = Build.BRAND ?: "unknown",
    val model: String = Build.MODEL ?: "unknown",
    val osVersion: String = Build.VERSION.RELEASE ?: "unknown",
    val sdkInt: Int = Build.VERSION.SDK_INT
)

@Serializable
data class MobileLogPayload(
    val timestamp: String,
    val level: String,
    val service: String = "banana-project-mobile",
    val env: String = "production",
    val version: String = "1.0",
    val trace_id: String,
    val event: String,
    val message: String,
    val device: DeviceContext = DeviceContext(),
    val error_type: String? = null,
    val error_stack: String? = null
)

object AppLogger {
    private const val APP_TAG = "BananaTelemetry"

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    fun generateTraceId(): String = UUID.randomUUID().toString()

    fun buildLogPayload(
        level: MobileLogLevel,
        event: String,
        message: String,
        throwable: Throwable? = null,
        traceId: String = generateTraceId()
    ): MobileLogPayload {
        return MobileLogPayload(
            timestamp = isoFormat.format(Date()),
            level = level.name.lowercase(),
            trace_id = traceId,
            event = event,
            message = message,
            error_type = throwable?.javaClass?.simpleName,
            error_stack = throwable?.stackTraceToString()
        )
    }

    fun serialize(payload: MobileLogPayload): String = json.encodeToString(payload)

    private fun safeLog(priority: Int, payload: MobileLogPayload, throwable: Throwable? = null) {
        val serialized = serialize(payload)
        try {
            Log.println(priority, APP_TAG, serialized)
            if (throwable != null) {
                Log.println(priority, APP_TAG, Log.getStackTraceString(throwable))
            }
        } catch (_: RuntimeException) {
            println(serialized)
            throwable?.printStackTrace()
        }
    }

    // Structured logging overloads (event + message)
    fun d(event: String, message: String, traceId: String = generateTraceId()) {
        safeLog(Log.DEBUG, buildLogPayload(MobileLogLevel.DEBUG, event, message, traceId = traceId))
    }

    fun i(event: String, message: String, traceId: String = generateTraceId()) {
        safeLog(Log.INFO, buildLogPayload(MobileLogLevel.INFO, event, message, traceId = traceId))
    }

    fun w(event: String, message: String, throwable: Throwable? = null, traceId: String = generateTraceId()) {
        safeLog(Log.WARN, buildLogPayload(MobileLogLevel.WARN, event, message, throwable, traceId), throwable)
    }

    fun e(event: String, message: String, throwable: Throwable? = null, traceId: String = generateTraceId()) {
        safeLog(Log.ERROR, buildLogPayload(MobileLogLevel.ERROR, event, message, throwable, traceId), throwable)
    }

    fun fatal(event: String, message: String, throwable: Throwable? = null, traceId: String = generateTraceId()) {
        safeLog(Log.ASSERT, buildLogPayload(MobileLogLevel.FATAL, event, message, throwable, traceId), throwable)
    }

    // Backward-compatible single-parameter overloads
    fun d(message: String) = d("general_debug", message)
    fun i(message: String) = i("general_info", message)
    fun w(message: String) = w("general_warning", message)
    fun e(message: String, throwable: Throwable? = null) = e("general_error", message, throwable)
    fun e(message: String, exception: Exception) = e("general_error", message, exception)
}
