package org.banana.project.services

import org.banana.project.data.UnitOfWork
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for Sale business logic and operations (formerly SellService).
 * Acts as a facade for sale-related operations using the Unit of Work pattern.
 */
@Singleton
class SaleService @Inject constructor(
    private val unitOfWork: UnitOfWork
) {

    /**
     * Create a new sale with items.
     * Validates the sale data and calculates total amount automatically.
     */
    suspend fun createSale(sale: Sale, items: List<SaleItem>): Result<Long> {
        return try {
            validateSale(sale, items)
            val saleId = unitOfWork.createSaleWithItems(sale, items)
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a sale by ID with its items.
     */
    suspend fun getSale(saleId: Long): Result<Pair<Sale, List<SaleItem>>?> {
        return try {
            if (saleId <= 0) {
                throw IllegalArgumentException("Invalid sale ID")
            }
            // Implementation would call repository through UnitOfWork
            Result.success(null) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a sale completely (including all items).
     */
    suspend fun deleteSale(saleId: Long): Result<Unit> {
        return try {
            if (saleId <= 0) {
                throw IllegalArgumentException("Invalid sale ID")
            }
            unitOfWork.deleteSaleCompletely(saleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get sales report for a specific period.
     */
    suspend fun getSalesReport(
        startDate: Instant,
        endDate: Instant
    ): Result<UnitOfWork.SalesReport> {
        return try {
            validateDateRange(startDate, endDate)
            val report = unitOfWork.getSalesReport(startDate, endDate)
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate total amount for a sale based on current product prices.
     */
    suspend fun calculateTotalAmount(sale: Sale, items: List<SaleItem>): Result<Double> {
        return try {
            validateSale(sale, items)
            // Implementation would fetch current product prices and calculate
            Result.success(0.0) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate sale data.
     */
    private fun validateSale(sale: Sale, items: List<SaleItem>) {
        require(items.isNotEmpty()) { "Sale must have at least one item" }
        require(sale.totalAmount >= 0) { "Total amount cannot be negative" }

        items.forEach { item ->
            require(item.productId > 0) { "Invalid product ID: ${item.productId}" }
            require(item.quantity > 0) { "Quantity must be positive: ${item.quantity}" }
        }

        // Check for duplicate products in the same sale
        val productIds = items.map { it.productId }
        require(productIds.size == productIds.distinct().size) {
            "Duplicate products found in sale items"
        }
    }

    /**
     * Validate date range.
     */
    private fun validateDateRange(startDate: Instant, endDate: Instant) {
        require(startDate.isBefore(endDate)) {
            "Start date must be before end date"
        }
        require(!startDate.isAfter(Instant.now())) {
            "Start date cannot be in the future"
        }
    }
}
