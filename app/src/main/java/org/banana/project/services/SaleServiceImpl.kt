package org.banana.project.services

import org.banana.project.data.UnitOfWork
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of the SaleService interface.
 */
@Singleton
class SaleServiceImpl @Inject constructor(
    private val unitOfWork: UnitOfWork
) : SaleService {

    override suspend fun createSale(sale: Sale, items: List<SaleItem>): Result<Long> {
        return try {
            validateSale(sale, items)
            val saleId = unitOfWork.createSaleWithItems(sale, items)
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSale(saleId: Long): Result<Pair<Sale, List<SaleItem>>?> {
        return try {
            if (saleId <= 0) {
                throw IllegalArgumentException("Invalid sale ID")
            }
            Result.success(null) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSale(saleId: Long): Result<Unit> {
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

    override suspend fun getSalesReport(
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

    override suspend fun calculateTotalAmount(sale: Sale, items: List<SaleItem>): Result<Double> {
        return try {
            validateSale(sale, items)
            Result.success(0.0) // Placeholder
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun validateSale(sale: Sale, items: List<SaleItem>) {
        require(items.isNotEmpty()) { "Sale must have at least one item" }
        require(sale.totalAmount >= 0) { "Total amount cannot be negative" }

        items.forEach { item ->
            require(item.productId > 0) { "Invalid product ID: ${item.productId}" }
            require(item.quantity > 0) { "Quantity must be positive: ${item.quantity}" }
        }

        val productIds = items.map { it.productId }
        require(productIds.size == productIds.distinct().size) {
            "Duplicate products found in sale items"
        }
    }

    private fun validateDateRange(startDate: Instant, endDate: Instant) {
        require(startDate.isBefore(endDate)) {
            "Start date must be before end date"
        }
        require(!startDate.isAfter(Instant.now())) {
            "Start date cannot be in the future"
        }
    }
}
