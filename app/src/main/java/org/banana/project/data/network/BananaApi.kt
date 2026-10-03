package org.banana.project.data.network

import org.banana.project.data.network.models.CategoryDto
import org.banana.project.data.network.models.LoginRequest
import org.banana.project.data.network.models.LoginResponse
import org.banana.project.data.network.models.ProductDto
import org.banana.project.data.network.models.RefreshRequest
import org.banana.project.data.network.models.SaleRequestDto
import org.banana.project.data.network.models.SaleResponseDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BananaApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/refresh")
    fun refreshToken(@Body request: RefreshRequest): Call<LoginResponse>

    @GET("/api/v1/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("/api/v1/categories")
    suspend fun getCategories(): List<CategoryDto>

    @POST("/api/v1/sales")
    suspend fun createSale(@Body request: SaleRequestDto): SaleResponseDto
}
