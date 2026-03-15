package org.banana.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.banana.project.data.database.BananaDatabase
import org.banana.project.model.Sell
import org.banana.project.model.SellItem
import java.time.Instant

/**
 * Repository class for Sell operations.
 * Handles data access and business logic for sells.
 */
class SellRepository(private val database: BananaDatabase) {

    /**
     * Insert a new sell.
     */
    suspend fun insert(sell: Sell): Long {
        database.productQueries.insertSell(
            sell.totalAmount,
            sell.dateTime.toString()
        )
        return database.productQueries.lastInsertRowId().executeAsOne()
    }

    /**
     * Insert a sell with its items.
     */
    suspend fun insertSellWithItems(sell: Sell, items: List<SellItem>): Long {
        return database.productQueries.transactionWithResult {
            database.productQueries.insertSell(
                sell.totalAmount,
                sell.dateTime.toString()
            )
            val sellId = database.productQueries.lastInsertRowId().executeAsOne()
            
            items.forEach { item ->
                database.productQueries.insertSellItem(
                    sellId,
                    item.productId,
                    item.quantity.toLong(),
                    // In a real app, you'd probably fetch the price from the Product table
                    sell.totalAmount / items.size // Placeholder for price per item
                )
            }
            sellId
        }
    }

    /**
     * Update an existing sell.
     */
    suspend fun update(sell: Sell) {
        // Implement if needed
    }

    /**
     * Delete a sell.
     */
    suspend fun delete(sell: Sell) {
        database.productQueries.deleteSell(sell.id)
    }

    /**
     * Delete a sell by ID.
     */
    suspend fun deleteById(sellId: Long) {
        database.productQueries.deleteSell(sellId)
    }

    /**
     * Delete a sell with all its items.
     */
    suspend fun deleteSellWithItems(sellId: Long) {
        database.productQueries.transaction {
            database.productQueries.deleteSellItemsBySellId(sellId)
            database.productQueries.deleteSell(sellId)
        }
    }

    /**
     * Get a sell by ID.
     */
    suspend fun getById(sellId: Long): Sell? {
        return database.productQueries.selectSellById(sellId).executeAsOneOrNull()?.toDomain()
    }

    /**
     * Get a sell with its items by ID.
     */
    suspend fun getSellWithItems(sellId: Long): Pair<Sell, List<SellItem>>? {
        val sell = getById(sellId) ?: return null
        val items = database.productQueries.selectSellItemsBySellId(sellId).executeAsList().map { it.toDomain() }
        return Pair(sell, items)
    }

    /**
     * Get all sells as a Flow.
     */
    fun getAll(): Flow<List<Sell>> {
        return kotlinx.coroutines.flow.flow {
            val entities = database.productQueries.selectAllSells().executeAsList()
            emit(entities.map { it.toDomain() })
        }
    }

    /**
     * Get all sells with their items as a Flow.
     */
    fun getAllSellsWithItems(): Flow<List<Pair<Sell, List<SellItem>>>> {
        return getAll().map { sells ->
            sells.map { sell ->
                val items = database.productQueries.selectSellItemsBySellId(sell.id).executeAsList().map { it.toDomain() }
                Pair(sell, items)
            }
        }
    }

    /**
     * Get sells by date range.
     */
    fun getSellsByDateRange(startDate: Instant, endDate: Instant): Flow<List<Sell>> {
        return getAll().map { sells ->
            sells.filter { it.dateTime.isAfter(startDate) && it.dateTime.isBefore(endDate) }
        }
    }

    /**
     * Get total amount by date range.
     */
    suspend fun getTotalAmountByDateRange(startDate: Instant, endDate: Instant): Double {
        return database.productQueries.getTotalAmountByDateRange(
            startDate.toString(),
            endDate.toString()
        ).executeAsOne().SUM ?: 0.0
    }

    /**
     * Get the total count of sells.
     */
    suspend fun getCount(): Int {
        return database.productQueries.selectAllSells().executeAsList().size.toInt()
    }

    /**
     * Extension function to convert SQLDelight Sell to domain model.
     */
    private fun org.banana.project.data.database.Sell.toDomain(): Sell {
        return Sell(
            id = this.id,
            items = emptyList(), // Items are handled separately
            totalAmount = this.total,
            dateTime = java.time.Instant.parse(this.created_at)
        )
    }

    /**
     * Extension function to convert SQLDelight SellItem to domain model.
     */
    private fun org.banana.project.data.database.SellItem.toDomain(): SellItem {
        return SellItem(
            productId = this.product_id,
            quantity = this.quantity.toInt()
        )
    }
}
