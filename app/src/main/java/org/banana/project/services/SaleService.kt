package org.banana.project.services

import org.banana.project.data.UnitOfWork
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import java.time.Instant

/**
 * Interface contract for Sale service operations.
 */
interface SaleService {
    suspend fun createSale(sale: Sale, items: List<SaleItem>): Result<Long>
    suspend fun getSale(saleId: Long): Result<Pair<Sale, List<SaleItem>>?>
    suspend fun deleteSale(saleId: Long): Result<Unit>
    suspend fun getSalesReport(startDate: Instant, endDate: Instant): Result<UnitOfWork.SalesReport>
    suspend fun calculateTotalAmount(sale: Sale, items: List<SaleItem>): Result<Double>
}
