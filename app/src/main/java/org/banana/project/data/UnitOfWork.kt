package org.banana.project.data

import org.banana.project.data.repository.ProductRepository
import org.banana.project.data.repository.SellRepository
import org.banana.project.model.Product
import org.banana.project.model.Sell
import org.banana.project.model.SellItem

/**
 * Unit of Work pattern implementation for coordinating database operations.
 * Provides transactional boundaries and ensures data consistency across repositories.
 */
class UnitOfWork(
    private val productRepository: ProductRepository,
    private val sellRepository: SellRepository
) {

    /**
     * Create a new sell with its items in a single transaction.
     * This ensures that either all data is saved or none is saved.
     */
    suspend fun createSellWithItems(sell: Sell, items: List<SellItem>): Long {
        // Validate that all products exist
        for (item in items) {
            val product = productRepository.getById(item.productId)
            if (product == null) {
                throw IllegalArgumentException("Product with ID ${item.productId} does not exist")
            }
        }

        // Calculate total amount from the items' unit prices
        val calculatedTotal = items.sumOf { it.unitPrice * it.quantity }

        // Create sell with calculated total
        val sellWithTotal = sell.copy(totalAmount = calculatedTotal)

        return sellRepository.insertSellWithItems(sellWithTotal, items)
    }

    /**
     * Update product catalog (insert or update multiple products).
     */
    suspend fun updateProductCatalog(products: List<Product>): List<Long> {
        return productRepository.insertAll(products)
    }

    /**
     * Get product catalog with sells summary.
     * Returns products with their sell statistics.
     */
    suspend fun getProductCatalogWithStats(): List<ProductWithStats> {
        val products = productRepository.getAll() // This would need to be converted to suspend
        // Note: This is a simplified version. In a real implementation,
        // you might want to create a more complex query or use separate calls
        return emptyList() // Placeholder
    }

    /**
     * Delete a sell and all its associated items.
     */
    suspend fun deleteSellCompletely(sellId: Long) {
        sellRepository.deleteSellWithItems(sellId)
    }

    /**
     * Get sales report for a date range.
     */
    suspend fun getSalesReport(startDate: java.time.Instant, endDate: java.time.Instant): SalesReport {
        val totalAmount = sellRepository.getTotalAmountByDateRange(startDate, endDate)
        val sellCount = sellRepository.getCount() // This would need filtering by date range
        val productsSold = 0 // This would require additional queries

        return SalesReport(
            totalAmount = totalAmount,
            sellCount = sellCount,
            productsSold = productsSold,
            periodStart = startDate,
            periodEnd = endDate
        )
    }

    /**
     * Data class for product statistics.
     */
    data class ProductWithStats(
        val product: Product,
        val totalSold: Int,
        val revenue: Double
    )

    /**
     * Data class for sales report.
     */
    data class SalesReport(
        val totalAmount: Double,
        val sellCount: Int,
        val productsSold: Int,
        val periodStart: java.time.Instant,
        val periodEnd: java.time.Instant
    )
}