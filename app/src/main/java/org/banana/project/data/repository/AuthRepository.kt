package org.banana.project.data.repository

import org.banana.project.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Unit>
    fun logout()
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getCurrentUser(): User?
}
