package org.banana.project.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLoggerTest {

    data class LogTestCase(
        val name: String,
        val level: MobileLogLevel,
        val event: String,
        val message: String,
        val throwable: Throwable?,
        val customTraceId: String?,
        val expectedLevelString: String
    )

    @Test
    fun `buildLogPayload formats fields according to Grafana specification`() {
        val testCases = listOf(
            LogTestCase(
                name = "Standard info log without error",
                level = MobileLogLevel.INFO,
                event = "user_login_success",
                message = "User authenticated successfully",
                throwable = null,
                customTraceId = "fixed-trace-1234",
                expectedLevelString = "info"
            ),
            LogTestCase(
                name = "Warning log with retry exception",
                level = MobileLogLevel.WARN,
                event = "network_retry",
                message = "Temporary failure, retrying",
                throwable = IllegalStateException("Temporary timeout"),
                customTraceId = "fixed-trace-5678",
                expectedLevelString = "warn"
            ),
            LogTestCase(
                name = "Fatal crash log with null pointer",
                level = MobileLogLevel.FATAL,
                event = "uncaught_crash",
                message = "Critical unhandled crash",
                throwable = NullPointerException("Unexpected null reference"),
                customTraceId = null,
                expectedLevelString = "fatal"
            )
        )

        testCases.forEach { tc ->
            val payload = if (tc.customTraceId != null) {
                AppLogger.buildLogPayload(
                    level = tc.level,
                    event = tc.event,
                    message = tc.message,
                    throwable = tc.throwable,
                    traceId = tc.customTraceId
                )
            } else {
                AppLogger.buildLogPayload(
                    level = tc.level,
                    event = tc.event,
                    message = tc.message,
                    throwable = tc.throwable
                )
            }

            assertEquals("Scenario: ${tc.name} level mismatch", tc.expectedLevelString, payload.level)
            assertEquals("Scenario: ${tc.name} event mismatch", tc.event, payload.event)
            assertEquals("Scenario: ${tc.name} message mismatch", tc.message, payload.message)
            assertEquals("Scenario: ${tc.name} service mismatch", "banana-project-mobile", payload.service)
            assertNotNull("Scenario: ${tc.name} timestamp should not be null", payload.timestamp)

            if (tc.customTraceId != null) {
                assertEquals("Scenario: ${tc.name} trace ID mismatch", tc.customTraceId, payload.trace_id)
            } else {
                assertTrue("Scenario: ${tc.name} generated trace ID should not be blank", payload.trace_id.isNotBlank())
            }

            if (tc.throwable != null) {
                assertEquals("Scenario: ${tc.name} error type mismatch", tc.throwable.javaClass.simpleName, payload.error_type)
                assertTrue("Scenario: ${tc.name} stack should contain exception message", payload.error_stack?.contains(tc.throwable.message ?: "") == true)
            }

            val serializedJson = AppLogger.serialize(payload)
            assertTrue("Serialized JSON must contain trace_id", serializedJson.contains("\"trace_id\""))
            assertTrue("Serialized JSON must contain service", serializedJson.contains("\"service\":\"banana-project-mobile\""))
        }
    }
}
