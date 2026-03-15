package org.banana.project.services

import kotlinx.coroutines.flow.Flow
import org.banana.project.data.UnitOfWork
import org.banana.project.model.Product
import org.banana.project.data.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for Product business logic and operations.
 * Acts as a facade for product-related operations using the Unit of Work pattern.
 */
@Singleton
class ProductService @Inject constructor(
    private val unitOfWork: UnitOfWork,
    private val productRepository: ProductRepository
) {

    /**
     * Add a new product to the catalog.
     * Performs validation before insertion.
     */
    suspend fun addProduct(product: Product): Result<Long> {
        return try {
            validateProduct(product)
            val ids = unitOfWork.updateProductCatalog(listOf(product))
            Result.success(ids.first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing product.
     */
    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            validateProduct(product)
            if (product.id == 0L) {
                throw IllegalArgumentException("Product ID cannot be 0 for update")
            }
            productRepository.update(product)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a product by ID.
     */
    suspend fun deleteProduct(productId: Long): Result<Unit> {
        return try {
            if (productId <= 0) {
                throw IllegalArgumentException("Invalid product ID")
            }
            productRepository.deleteById(productId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a product by ID.
     */
    suspend fun getProduct(productId: Long): Result<Product?> {
        return try {
            if (productId <= 0) {
                throw IllegalArgumentException("Invalid product ID")
            }
            val product = productRepository.getById(productId)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all products.
     */
    fun getAllProducts(): Flow<List<Product>> {
        return productRepository.getAll()
    }

    /**
     * Search products by name.
     */
    fun searchProducts(query: String): Flow<List<Product>> {
        return productRepository.searchByName(query)
    }

    /**
     * Get products by category.
     */
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productRepository.getByCategory(category)
    }

    /**
     * Update the entire product catalog.
     */
    suspend fun updateProductCatalog(products: List<Product>): Result<List<Long>> {
        return try {
            products.forEach { validateProduct(it) }
            val ids = unitOfWork.updateProductCatalog(products)
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate product data.
     */
    private fun validateProduct(product: Product) {
        require(product.name.isNotBlank()) { "Product name cannot be blank" }
        require(product.sellPrice >= 0) { "Sell price cannot be negative" }
        // Computed properties validation if needed
        require(product.cost >= 0) { "Cost cannot be negative" }
        require(product.sellPrice >= product.cost) { "Sell price must be greater than or equal to cost" }
    }
}
