package org.banana.project.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val sellPrice: Double = 0.0,
    val isForSale: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
