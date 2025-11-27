package org.banana.project.model

import java.time.Instant

/**
 * A data class representing a SellItem in a sell transaction.
 *
 * @property productId The ID of the product being sold.
 * @property quantity The quantity of the product sold.
 */
data class SellItem(
    val productId: Long,
    val quantity: Int
)

/**
 * A data class representing a Sell domain object.
 *
 * @property id The unique identifier for the sell.
 * @property items The list of items sold in this transaction.
 * @property totalAmount The total amount of the bill.
 * @property dateTime The date and time of the sell.
 */
data class Sell(
    val id: Long,
    val items: List<SellItem>,
    val totalAmount: Double,
    val dateTime: Instant
) {
    /**
     * Calculates the total amount by summing the quantity of each SellItem multiplied by the product's sell price.
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