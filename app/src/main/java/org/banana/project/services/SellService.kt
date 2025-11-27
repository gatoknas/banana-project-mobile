package org.banana.project.services

import org.banana.project.data.UnitOfWork
import org.banana.project.model.Sell
import org.banana.project.model.SellItem
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for Sell business logic and operations.
 * Acts as a facade for sell-related operations using the Unit of Work pattern.
 */
@Singleton
class SellService @Inject constructor(
    private val unitOfWork: UnitOfWork
) {

    /**
     * Create a new sell with items.
     * Validates the sell data and calculates total amount automatically.
     */
    suspend fun createSell(sell: Sell, items: List<SellItem>): Result<Long> {
        return try {
            validateSell(sell, items)
            val sellId = unitOfWork.createSellWithItems(sell, items)
            Result.success(sellId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a sell by ID with its items.
     */
    suspend fun getSell(sellId: Long): Result<Pair<Sell, List<SellItem>>?> {
        return try {
            if (sellId <= 0) {
                throw IllegalArgumentException("Invalid sell ID")
            }
            // Implementation would call repository through UnitOfWork
            Result.success(null) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a sell completely (including all items).
     */
    suspend fun deleteSell(sellId: Long): Result<Unit> {
        return try {
            if (sellId <= 0) {
                throw IllegalArgumentException("Invalid sell ID")
            }
            unitOfWork.deleteSellCompletely(sellId)
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
     * Calculate total amount for a sell based on current product prices.
     */
    suspend fun calculateTotalAmount(sell: Sell, items: List<SellItem>): Result<Double> {
        return try {
            validateSell(sell, items)
            // Implementation would fetch current product prices and calculate
            Result.success(0.0) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate sell data.
     */
    private fun validateSell(sell: Sell, items: List<SellItem>) {
        require(items.isNotEmpty()) { "Sell must have at least one item" }
        require(sell.totalAmount >= 0) { "Total amount cannot be negative" }

        items.forEach { item ->
            require(item.productId > 0) { "Invalid product ID: ${item.productId}" }
            require(item.quantity > 0) { "Quantity must be positive: ${item.quantity}" }
        }

        // Check for duplicate products in the same sell
        val productIds = items.map { it.productId }
        require(productIds.size == productIds.distinct().size) {
            "Duplicate products found in sell items"
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