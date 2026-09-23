package org.banana.project.data.network

import org.banana.project.data.network.models.LoginRequest
import org.banana.project.data.network.models.LoginResponse
import org.banana.project.data.network.models.RefreshRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BananaApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/refresh")
    fun refreshToken(@Body request: RefreshRequest): Call<LoginResponse>
}
