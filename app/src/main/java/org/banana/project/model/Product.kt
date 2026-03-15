package org.banana.project.model

/**
 * A data class representing a Product domain object.
 *
 * @property id The unique identifier for the product.
 * @property name The name of the product.
 * @property description The description of the product.
 * @property sellPrice The selling price of the product.
 * @property createdAt The creation timestamp.
 * @property updatedAt The last update timestamp.
 */
data class Product(
    val id: Long,
    val name: String,
    val description: String?,
    val sellPrice: Double,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
) {
    // Computed properties for backward compatibility
    val category: String get() = "General"
    val cost: Double get() = sellPrice * 0.7 // Assume 70% cost
    val supplier: String get() = "Unknown"
}