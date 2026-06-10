package org.banana.project.services

import kotlinx.coroutines.flow.Flow
import org.banana.project.model.Product

/**
 * Interface contract for Product service operations.
 */
interface ProductService {
    suspend fun addProduct(product: Product): Result<Long>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(productId: Long): Result<Unit>
    suspend fun getProduct(productId: Long): Result<Product?>
    fun getAllProducts(): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    fun getProductsByCategory(category: String): Flow<List<Product>>
    suspend fun updateProductCatalog(products: List<Product>): Result<List<Long>>
}
