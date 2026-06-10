package org.banana.project.data.repository

import kotlinx.coroutines.flow.Flow
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import java.time.Instant

/**
 * Interface contract for Sale repository operations.
 */
interface SaleRepository {
    suspend fun insert(sale: Sale): Long
    suspend fun insertSaleWithItems(sale: Sale, items: List<SaleItem>): Long
    suspend fun update(sale: Sale)
    suspend fun delete(sale: Sale)
    suspend fun deleteById(saleId: Long)
    suspend fun deleteSaleWithItems(saleId: Long)
    suspend fun getById(saleId: Long): Sale?
    suspend fun getSaleWithItems(saleId: Long): Pair<Sale, List<SaleItem>>?
    fun getAll(): Flow<List<Sale>>
    fun getAllSalesWithItems(): Flow<List<Pair<Sale, List<SaleItem>>>>
    fun getSalesByDateRange(startDate: Instant, endDate: Instant): Flow<List<Sale>>
    suspend fun getTotalAmountByDateRange(startDate: Instant, endDate: Instant): Double
    suspend fun getCount(): Int
}
