package org.banana.project.model

import java.time.Instant

/**
 * A data class representing a SaleItem in a sale transaction (formerly SellItem).
 *
 * @property productId The ID of the product being sold.
 * @property quantity The quantity of the product sold.
 * @property unitPrice The unit price of the product at the time of sale.
 */
data class SaleItem(
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double = 0.0
)

/**
 * A data class representing a Sale domain object (formerly Sell).
 *
 * @property id The unique identifier for the sale.
 * @property items The list of items sold in this transaction.
 * @property totalAmount The total amount of the bill.
 * @property dateTime The date and time of the sale.
 */
data class Sale(
    val id: Long,
    val items: List<SaleItem>,
    val totalAmount: Double,
    val dateTime: Instant
) {
    /**
     * Calculates the total amount by summing the quantity of each SaleItem multiplied by the product's sell price.
     *
     * @param products The list of available products to look up prices.
     * @return The calculated total amount.
     */
    fun calculateTotalAmount(products: List<Product>): Double {
        return items.sumOf { item ->
            val product = products.find { it.id == item.productId }
            item.quantity * (product?.sellPrice ?: 0.0)
        }
    }
}
