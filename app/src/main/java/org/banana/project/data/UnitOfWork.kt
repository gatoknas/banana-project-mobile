package org.banana.project.data

import org.banana.project.data.repository.ProductRepository
import org.banana.project.data.repository.SaleRepository
import org.banana.project.model.Product
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem

/**
 * Unit of Work pattern implementation for coordinating database operations.
 * Provides transactional boundaries and ensures data consistency across repositories.
 */
class UnitOfWork(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) {

    /**
     * Create a new sale with its items in a single transaction.
     * This ensures that either all data is saved or none is saved.
     */
    suspend fun createSaleWithItems(sale: Sale, items: List<SaleItem>): Long {
        // Validate that all products exist
        for (item in items) {
            val product = productRepository.getById(item.productId)
            if (product == null) {
                throw IllegalArgumentException("Product with ID ${item.productId} does not exist")
            }
        }

        // Calculate total amount from the items' unit prices
        val calculatedTotal = items.sumOf { it.unitPrice * it.quantity }

        // Create sale with calculated total
        val saleWithTotal = sale.copy(totalAmount = calculatedTotal)

        return saleRepository.insertSaleWithItems(saleWithTotal, items)
    }

    /**
     * Update product catalog (insert or update multiple products).
     */
    suspend fun updateProductCatalog(products: List<Product>): List<Long> {
        return productRepository.insertAll(products)
    }

    /**
     * Get product catalog with sales summary.
     * Returns products with their sale statistics.
     */
    suspend fun getProductCatalogWithStats(): List<ProductWithStats> {
        val products = productRepository.getAll() // This would need to be converted to suspend
        // Note: This is a simplified version. In a real implementation,
        // you might want to create a more complex query or use separate calls
        return emptyList() // Placeholder
    }

    /**
     * Delete a sale and all its associated items.
     */
    suspend fun deleteSaleCompletely(saleId: Long) {
        saleRepository.deleteSaleWithItems(saleId)
    }

    /**
     * Get sales report for a date range.
     */
    suspend fun getSalesReport(startDate: java.time.Instant, endDate: java.time.Instant): SalesReport {
        val totalAmount = saleRepository.getTotalAmountByDateRange(startDate, endDate)
        val saleCount = saleRepository.getCount() // This would need filtering by date range
        val productsSold = 0 // This would require additional queries

        return SalesReport(
            totalAmount = totalAmount,
            saleCount = saleCount,
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
        val saleCount: Int,
        val productsSold: Int,
        val periodStart: java.time.Instant,
        val periodEnd: java.time.Instant
    )
}