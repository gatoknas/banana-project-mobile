package org.banana.project.data

import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.ProductDto
import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import org.banana.project.utils.AppLogger
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface CatalogSyncer {
    suspend fun sync(): Result<Int>
}

@Singleton
class ProductCatalogSync @Inject constructor(
    private val api: BananaApi,
    private val productRepository: ProductRepository
) : CatalogSyncer {

    override suspend fun sync(): Result<Int> {
        return try {
            val dtos = api.getProducts()
            val products = dtos.filter { it.isForSale }.map { it.toDomain() }
            productRepository.upsertAll(products)
            AppLogger.i("Product catalog synced: ${products.size} sellable products")
            Result.success(products.size)
        } catch (e: Exception) {
            AppLogger.e("Product catalog sync failed", e)
            Result.failure(e)
        }
    }

    private fun ProductDto.toDomain(): Product {
        val now = Instant.now()
        return Product(
            id = id,
            name = name,
            description = description,
            sellPrice = sellPrice,
            createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: now,
            updatedAt = updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: now
        )
    }
}
