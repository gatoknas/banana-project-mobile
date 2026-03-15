package org.banana.project.model

/**
 * A UI data model representing an item identified in a sales transaction,
 * associating the parsed quantity and explicit spoken name to a potential 
 * existing matched Product from the database.
 */
data class ParsedSellItem(
    val quantity: Int,
    val parsedName: String,
    val matchedProduct: Product?
)
