package org.banana.project.model

/**
 * A data class representing a Product domain object.
 *
 * @property id The unique identifier for the product.
 * @property name The name of the product.
 * @property category The category of the product.
 * @property sellPrice The selling price of the product.
 * @property cost The cost price of the product.
 * @property supplier The supplier of the product.
 */
data class Product(
    val id: Long,
    val name: String,
    val category: String,
    val sellPrice: Double,
    val cost: Double,
    val supplier: String
)