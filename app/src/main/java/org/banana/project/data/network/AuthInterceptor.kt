package org.banana.project.data.network

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val encryptedPrefs: SharedPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Don't add token to login or refresh endpoints
        if (request.url.pathSegments.last() == "login" || request.url.pathSegments.last() == "refresh") {
            return chain.proceed(request)
        }

        val token = encryptedPrefs.getString("access_token", null)
        if (token != null) {
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }
        return chain.proceed(request)
    }
}
