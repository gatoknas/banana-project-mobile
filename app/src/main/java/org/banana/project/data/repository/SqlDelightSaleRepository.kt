package org.banana.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.banana.project.data.database.BananaDatabase
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SQLDelight implementation of the SaleRepository interface.
 */
@Singleton
class SqlDelightSaleRepository @Inject constructor(
    private val database: BananaDatabase
) : SaleRepository {

    override suspend fun insert(sale: Sale): Long {
        database.productQueries.insertSale(
            sale.totalAmount,
            sale.dateTime.toString()
        )
        return database.productQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun insertSaleWithItems(sale: Sale, items: List<SaleItem>): Long {
        return database.productQueries.transactionWithResult {
            database.productQueries.insertSale(
                sale.totalAmount,
                sale.dateTime.toString()
            )
            val saleId = database.productQueries.lastInsertRowId().executeAsOne()
            
            items.forEach { item ->
                database.productQueries.insertSaleItem(
                    saleId,
                    item.productId,
                    item.quantity.toLong(),
                    item.unitPrice * item.quantity
                )
            }
            saleId
        }
    }

    override suspend fun update(sale: Sale) {
        // Implement if needed
    }

    override suspend fun delete(sale: Sale) {
        database.productQueries.deleteSale(sale.id)
    }

    override suspend fun deleteById(saleId: Long) {
        database.productQueries.deleteSale(saleId)
    }

    override suspend fun deleteSaleWithItems(saleId: Long) {
        database.productQueries.transaction {
            database.productQueries.deleteSaleItemsBySaleId(saleId)
            database.productQueries.deleteSale(saleId)
        }
    }

    override suspend fun getById(saleId: Long): Sale? {
        return database.productQueries.selectSaleById(saleId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getSaleWithItems(saleId: Long): Pair<Sale, List<SaleItem>>? {
        val sale = getById(saleId) ?: return null
        val items = database.productQueries.selectSaleItemsBySaleId(saleId).executeAsList().map { it.toDomain() }
        return Pair(sale, items)
    }

    override fun getAll(): Flow<List<Sale>> {
        return kotlinx.coroutines.flow.flow {
            val entities = database.productQueries.selectAllSales().executeAsList()
            emit(entities.map { it.toDomain() })
        }
    }

    override fun getAllSalesWithItems(): Flow<List<Pair<Sale, List<SaleItem>>>> {
        return getAll().map { sales ->
            sales.map { sale ->
                val items = database.productQueries.selectSaleItemsBySaleId(sale.id).executeAsList().map { it.toDomain() }
                Pair(sale, items)
            }
        }
    }

    override fun getSalesByDateRange(startDate: Instant, endDate: Instant): Flow<List<Sale>> {
        return getAll().map { sales ->
            sales.filter { it.dateTime.isAfter(startDate) && it.dateTime.isBefore(endDate) }
        }
    }

    override suspend fun getTotalAmountByDateRange(startDate: Instant, endDate: Instant): Double {
        return database.productQueries.getTotalAmountByDateRange(
            startDate.toString(),
            endDate.toString()
        ).executeAsOne().SUM ?: 0.0
    }

    override suspend fun getCount(): Int {
        return database.productQueries.selectAllSales().executeAsList().size.toInt()
    }

    private fun org.banana.project.data.database.Sale.toDomain(): Sale {
        return Sale(
            id = this.id,
            items = emptyList(),
            totalAmount = this.total,
            dateTime = java.time.Instant.parse(this.created_at)
        )
    }

    private fun org.banana.project.data.database.SaleItem.toDomain(): SaleItem {
        return SaleItem(
            productId = this.product_id,
            quantity = this.quantity.toInt(),
            unitPrice = if (this.quantity > 0) this.price / this.quantity else this.price
        )
    }
}
