package org.banana.project.utils

import org.banana.project.model.Product
import org.banana.project.model.ParsedSellItem
import kotlin.math.min

object ProductMatchingService {

    /**
     * Matches a list of ParsedItems to the closest corresponding Products from the database.
     * Includes diagnostic AppLogger outputs.
     */
    fun matchParsedItemsToProducts(
        parsedItems: List<ParsedItem>,
        dbProducts: List<Product>
    ): List<ParsedSellItem> {
        return parsedItems.map { parsedItem ->
            val match = findBestMatch(parsedItem.name, dbProducts)
            
            if (match != null) {
                AppLogger.d("Match found: '${parsedItem.name}' -> '${match.name}'")
            } else {
                AppLogger.w("No match found for: '${parsedItem.name}'")
            }
            
            ParsedSellItem(
                quantity = parsedItem.quantity,
                parsedName = parsedItem.name,
                matchedProduct = match
            )
        }
    }

    private fun findBestMatch(targetName: String, products: List<Product>): Product? {
        if (products.isEmpty() || targetName.isBlank()) return null
        
        val normalizedTarget = targetName.trim().lowercase().normalizeSpanishPlural()
        var bestProduct: Product? = null
        var lowestDistance = Int.MAX_VALUE

        for (product in products) {
            val normalizedProductName = product.name.trim().lowercase().normalizeSpanishPlural()
            
            // Fast-path for exact match
            if (normalizedProductName == normalizedTarget) {
                return product
            }
            
            // Fast-path for simple plurals or substrings
            if (normalizedProductName.contains(normalizedTarget) || normalizedTarget.contains(normalizedProductName)) {
                return product
            }

            // Calculate Levenshtein distance for fuzzy matching
            val distance = calculateLevenshteinDistance(normalizedTarget, normalizedProductName)
            
            // Allow up to 3 character differences for a match
            if (distance <= 3 && distance < lowestDistance) {
                lowestDistance = distance
                bestProduct = product
            }
        }
        
        return bestProduct
    }

    /**
     * Applies basic Spanish morphology rules to strip common plural endings
     * (e.g. "s", "es", "ces" -> "z") down to a singular semantic stem.
     */
    private fun String.normalizeSpanishPlural(): String {
        if (this.length <= 3) return this // Too short to safely trim
        
        return when {
            this.endsWith("ces") -> this.dropLast(3) + "z" // e.g. lápices -> lápiz
            this.endsWith("es") && isConsonant(this[this.length - 3]) -> this.dropLast(2) // e.g. limones -> limon
            this.endsWith("s") && isUnaccentedVowel(this[this.length - 2]) -> this.dropLast(1) // e.g. manzanas -> manzana
            else -> this
        }
    }

    private fun isConsonant(c: Char): Boolean {
        return c.isLetter() && !isUnaccentedVowel(c) && !isAccentedVowel(c)
    }

    private fun isUnaccentedVowel(c: Char): Boolean {
        return c in listOf('a', 'e', 'i', 'o', 'u')
    }

    private fun isAccentedVowel(c: Char): Boolean {
        return c in listOf('á', 'é', 'í', 'ó', 'ú')
    }

    /**
     * Simple implementation of the Levenshtein distance algorithm
     * measuring the difference between two String sequences.
     */
    private fun calculateLevenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val len0 = lhs.length + 1
        val len1 = rhs.length + 1

        var cost = IntArray(len0)
        var newcost = IntArray(len0)

        for (i in 0 until len0) cost[i] = i

        for (j in 1 until len1) {
            newcost[0] = j
            for (i in 1 until len0) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                val costReplace = cost[i - 1] + match
                val costInsert = cost[i] + 1
                val costDelete = newcost[i - 1] + 1
                newcost[i] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newcost
            newcost = swap
        }
        return cost[len0 - 1]
    }
}
