package org.banana.project.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.banana.project.data.database.BananaDatabase
import org.banana.project.model.Product

/**
 * Repository class for Product operations.
 * Handles data access and business logic for products.
 */
class ProductRepository(private val database: BananaDatabase) {

    /**
     * Insert a new product.
     */
    suspend fun insert(product: Product): Long {
        database.productQueries.insertProduct(
            product.name,
            product.description,
            product.sellPrice,
            product.createdAt.toString(),
            product.updatedAt.toString()
        )
        return database.productQueries.lastInsertRowId().executeAsOne()
    }

    /**
     * Insert multiple products.
     */
    suspend fun insertAll(products: List<Product>): List<Long> {
        return products.map { insert(it) }
    }

    /**
     * Update an existing product.
     */
    suspend fun update(product: Product) {
        database.productQueries.updateProduct(
            product.name,
            product.description,
            product.sellPrice,
            product.updatedAt.toString(),
            product.id
        )
    }

    /**
     * Delete a product.
     */
    suspend fun delete(product: Product) {
        database.productQueries.deleteProduct(product.id)
    }

    /**
     * Delete a product by ID.
     */
    suspend fun deleteById(productId: Long) {
        database.productQueries.deleteProduct(productId)
    }

    /**
     * Get a product by ID.
     */
    suspend fun getById(productId: Long): Product? {
        return database.productQueries.selectProductById(productId).executeAsOneOrNull()?.toDomain()
    }

    /**
     * Get all products as a Flow.
     */
    fun getAll(): Flow<List<Product>> {
        return database.productQueries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    /**
     * Get all products synchronously.
     */
    suspend fun getAllSync(): List<Product> {
        return database.productQueries.selectAllProducts().executeAsList().map { it.toDomain() }
    }

    /**
     * Get products by category.
     */
    fun getByCategory(category: String): Flow<List<Product>> {
        // Note: SQLDelight doesn't have category field in current schema, filtering in memory
        return getAll().map { products ->
            products.filter { it.category == category }
        }
    }

    /**
     * Search products by name.
     */
    fun searchByName(searchQuery: String): Flow<List<Product>> {
        // Note: SQLDelight doesn't have search query in current schema, filtering in memory
        return getAll().map { products ->
            products.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    /**
     * Get the total count of products.
     */
    suspend fun getCount(): Int {
        return database.productQueries.selectAllProducts().executeAsList().size
    }

    /**
     * Extension function to convert SQLDelight Product to domain model.
     */
    private fun org.banana.project.data.database.Product.toDomain(): Product {
        return Product(
            id = this.id,
            name = this.name,
            description = this.description,
            sellPrice = this.price,
            createdAt = java.time.Instant.parse(this.created_at),
            updatedAt = java.time.Instant.parse(this.updated_at)
        )
    }
}
