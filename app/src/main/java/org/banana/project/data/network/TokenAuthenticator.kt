package org.banana.project.data.network

import android.content.SharedPreferences
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.banana.project.data.network.models.RefreshRequest
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val encryptedPrefs: SharedPreferences,
    private val apiProvider: Provider<BananaApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // If the request itself was a refresh token call and it failed with 401, don't loop
        if (response.request.url.pathSegments.last() == "refresh") {
            return null
        }

        synchronized(this) {
            val currentToken = encryptedPrefs.getString("access_token", null)
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // If the token has already been refreshed by another thread
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = encryptedPrefs.getString("refresh_token", null) ?: return null

            val refreshCall = apiProvider.get().refreshToken(RefreshRequest(refreshToken))
            val refreshResponse = refreshCall.execute()

            if (refreshResponse.isSuccessful) {
                val newTokens = refreshResponse.body()
                if (newTokens != null) {
                    encryptedPrefs.edit()
                        .putString("access_token", newTokens.token)
                        .putString("refresh_token", newTokens.refreshToken)
                        .apply()

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.token}")
                        .build()
                }
            } else {
                // Clear tokens if refresh failed
                encryptedPrefs.edit().clear().apply()
            }
        }
        return null
    }
}
