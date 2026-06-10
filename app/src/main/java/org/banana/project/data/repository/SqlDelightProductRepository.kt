package org.banana.project.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.banana.project.data.database.BananaDatabase
import org.banana.project.model.Product
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SQLDelight implementation of the ProductRepository interface.
 */
@Singleton
class SqlDelightProductRepository @Inject constructor(
    private val database: BananaDatabase
) : ProductRepository {

    override suspend fun insert(product: Product): Long {
        database.productQueries.insertProduct(
            product.name,
            product.description,
            product.sellPrice,
            product.createdAt.toString(),
            product.updatedAt.toString()
        )
        return database.productQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun insertAll(products: List<Product>): List<Long> {
        return products.map { insert(it) }
    }

    override suspend fun update(product: Product) {
        database.productQueries.updateProduct(
            product.name,
            product.description,
            product.sellPrice,
            product.updatedAt.toString(),
            product.id
        )
    }

    override suspend fun delete(product: Product) {
        database.productQueries.deleteProduct(product.id)
    }

    override suspend fun deleteById(productId: Long) {
        database.productQueries.deleteProduct(productId)
    }

    override suspend fun getById(productId: Long): Product? {
        return database.productQueries.selectProductById(productId).executeAsOneOrNull()?.toDomain()
    }

    override fun getAll(): Flow<List<Product>> {
        return database.productQueries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getAllSync(): List<Product> {
        return database.productQueries.selectAllProducts().executeAsList().map { it.toDomain() }
    }

    override fun getByCategory(category: String): Flow<List<Product>> {
        return getAll().map { products ->
            products.filter { it.category == category }
        }
    }

    override fun searchByName(searchQuery: String): Flow<List<Product>> {
        return getAll().map { products ->
            products.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    override suspend fun getCount(): Int {
        return database.productQueries.selectAllProducts().executeAsList().size
    }

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
