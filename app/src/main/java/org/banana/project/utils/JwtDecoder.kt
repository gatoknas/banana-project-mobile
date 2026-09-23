package org.banana.project.utils

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object JwtDecoder {

    fun decodePayload(token: String): Map<String, String> {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return emptyMap()

            val payload = parts[1]
            val bytes = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val json = Json.parseToJsonElement(String(bytes)).jsonObject
            json.mapValues { (_, value) ->
                value.jsonPrimitive.content
            }
        } catch (e: Exception) {
            AppLogger.e("Failed to decode JWT payload: ${e.message}")
            emptyMap()
        }
    }
}
