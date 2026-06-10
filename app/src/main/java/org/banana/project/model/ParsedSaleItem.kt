package org.banana.project.model

import androidx.compose.runtime.Immutable
import androidx.annotation.Keep

/**
 * A UI data model representing an item identified in a sales transaction,
 * associating the parsed quantity and explicit spoken name to a potential 
 * existing matched Product from the database (formerly ParsedSellItem).
 */
@Immutable
@Keep
data class ParsedSaleItem(
    val quantity: Int,
    val parsedName: String,
    val matchedProduct: Product?
)
