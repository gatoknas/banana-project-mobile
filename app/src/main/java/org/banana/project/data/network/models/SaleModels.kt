package org.banana.project.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class SaleItemRequestDto(
    val productId: Long,
    val quantity: Double
)

@Serializable
data class SaleRequestDto(
    val userId: Long,
    val totalAmount: Double,
    val paymentMethod: String,
    val items: List<SaleItemRequestDto>
)

@Serializable
data class SaleResponseDto(
    val status: String,
    val message: String,
    val saleId: Long
)
