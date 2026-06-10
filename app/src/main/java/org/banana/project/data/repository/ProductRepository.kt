package org.banana.project.data.repository

import kotlinx.coroutines.flow.Flow
import org.banana.project.model.Product

/**
 * Interface contract for Product repository operations.
 */
interface ProductRepository {
    suspend fun insert(product: Product): Long
    suspend fun insertAll(products: List<Product>): List<Long>
    suspend fun update(product: Product)
    suspend fun delete(product: Product)
    suspend fun deleteById(productId: Long)
    suspend fun getById(productId: Long): Product?
    fun getAll(): Flow<List<Product>>
    suspend fun getAllSync(): List<Product>
    fun getByCategory(category: String): Flow<List<Product>>
    fun searchByName(searchQuery: String): Flow<List<Product>>
    suspend fun getCount(): Int
}
