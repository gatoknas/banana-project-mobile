package org.banana.project.data

import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.ProductDto
import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import org.banana.project.utils.AppLogger
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a full catalog sync: how many sellable products were stored and the
 * names of all product categories known to the backend.
 */
data class CatalogSnapshot(
    val productCount: Int,
    val categories: List<String>
)

interface CatalogSyncer {
    suspend fun sync(): Result<CatalogSnapshot>
}

@Singleton
class ProductCatalogSync @Inject constructor(
    private val api: BananaApi,
    private val productRepository: ProductRepository
) : CatalogSyncer {

    override suspend fun sync(): Result<CatalogSnapshot> {
        return try {
            val dtos = api.getProducts()
            val products = dtos.filter { it.isForSale }.map { it.toDomain() }
            productRepository.upsertAll(products)

            val categories = api.getCategories().map { it.name }.distinct().sorted()

            AppLogger.i("Product catalog synced: ${products.size} sellable products, ${categories.size} categories")
            Result.success(CatalogSnapshot(productCount = products.size, categories = categories))
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
            updatedAt = updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: now,
            categoryId = categoryId,
            categoryName = categoryName
        )
    }
}
