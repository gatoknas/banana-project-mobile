package org.banana.project.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)
