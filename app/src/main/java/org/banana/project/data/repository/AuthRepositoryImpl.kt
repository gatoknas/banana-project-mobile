package org.banana.project.data.repository

import android.content.SharedPreferences
import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.LoginRequest
import org.banana.project.model.User
import org.banana.project.utils.AppLogger
import org.banana.project.utils.JwtDecoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: BananaApi,
    private val encryptedPrefs: SharedPreferences
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(username, password))
            
            encryptedPrefs.edit()
                .putString("access_token", response.token)
                .putString("refresh_token", response.refreshToken)
                .apply()
                
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Login failed", e)
            Result.failure(e)
        }
    }

    override fun logout() {
        encryptedPrefs.edit().clear().apply()
    }

    override fun getAccessToken(): String? {
        return encryptedPrefs.getString("access_token", null)
    }

    override fun getRefreshToken(): String? {
        return encryptedPrefs.getString("refresh_token", null)
    }

    override fun getCurrentUser(): User? {
        val token = getAccessToken() ?: return null
        val claims = JwtDecoder.decodePayload(token)
        val username = claims["username"] ?: return null

        val backendRole = claims["role"] ?: ""
        val displayRole = when (backendRole) {
            "ayurami-admin" -> "Administrador"
            "ayurami-salesperson" -> "Vendedor"
            else -> "Cliente"
        }

        val name = if (username == "admin") "Administrador Ayurami" else username
        val id = claims["id"] ?: username

        return User(
            id = id,
            username = username,
            name = name,
            role = displayRole
        )
    }
}
