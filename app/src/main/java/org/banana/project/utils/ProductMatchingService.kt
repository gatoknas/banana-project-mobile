package org.banana.project.utils

import org.banana.project.model.Product
import org.banana.project.model.ParsedSaleItem
import kotlin.math.abs
import kotlin.math.min

object ProductMatchingService {

    /**
     * Matches a list of ParsedItems to the closest corresponding Products from the database.
     * Uses accent folding, tokenized scoring and an optional popularity (sold count) tie-breaker.
     */
    fun matchParsedItemsToProducts(
        parsedItems: List<ParsedItem>,
        dbProducts: List<Product>,
        soldCounts: Map<Long, Int> = emptyMap()
    ): List<ParsedSaleItem> {
        return parsedItems.map { parsedItem ->
            val match = findBestMatch(parsedItem.name, dbProducts, soldCounts)

            if (match != null) {
                AppLogger.d("Match found: '${parsedItem.name}' -> '${match.name}'")
            } else {
                AppLogger.w("No match found for: '${parsedItem.name}'")
            }

            ParsedSaleItem(
                quantity = parsedItem.quantity,
                parsedName = parsedItem.name,
                matchedProduct = match
            )
        }
    }

    private data class Match(val product: Product, val distance: Double, val sold: Int)

    private fun findBestMatch(
        targetName: String,
        products: List<Product>,
        soldCounts: Map<Long, Int>
    ): Product? {
        if (products.isEmpty() || targetName.isBlank()) return null

        val targetTokens = tokenize(targetName)
        if (targetTokens.isEmpty()) return null

        val threshold = maxAllowedDistance(targetName)
        var best: Match? = null

        for (product in products) {
            val productTokens = tokenize(product.name)
            if (productTokens.isEmpty()) continue

            val distance = tokenDistance(targetTokens, productTokens)
            if (distance > threshold) continue

            val candidate = Match(product, distance, soldCounts[product.id] ?: 0)
            if (best == null || isBetter(candidate, best)) {
                best = candidate
            }
        }

        return best?.product
    }

    private fun isBetter(candidate: Match, current: Match): Boolean {
        if (candidate.distance != current.distance) return candidate.distance < current.distance
        if (candidate.sold != current.sold) return candidate.sold > current.sold
        return candidate.product.name.length < current.product.name.length
    }

    private fun maxAllowedDistance(target: String): Double {
        val normalized = SpanishTextNormalizer.normalize(target)
        return when {
            normalized.length <= 4 -> 1.0
            normalized.length <= 8 -> 2.0
            else -> 3.0
        }
    }

    private fun tokenize(name: String): List<String> {
        return SpanishTextNormalizer.normalize(name)
            .split(" ")
            .filter { it.isNotBlank() }
    }

    /**
     * Candidate singular stems for a normalized token. Spanish plurals are formed by
     * adding "-s" (after a vowel) or "-es" (after a consonant), and "-z" becomes "-ces"
     * (e.g. "lápiz" -> "lápices"). Returning a small candidate set and taking the
     * minimum edit distance keeps comparison symmetric: "sanduches" and "sanduche"
     * both reduce to a shared candidate instead of one being over-stripped to
     * "sanduch".
     */
    private fun stems(token: String): Set<String> {
        if (token.length <= 3) return setOf(token)

        val candidates = linkedSetOf(token)
        if (token.endsWith("ces")) candidates.add(token.dropLast(3) + "z")
        if (token.endsWith("es")) candidates.add(token.dropLast(2))
        if (token.endsWith("s")) candidates.add(token.dropLast(1))
        return candidates
    }

    private fun tokenDistance(target: List<String>, product: List<String>): Double {
        if (target == product) return 0.0

        if (isSubsequence(target, product) || isSubsequence(product, target)) {
            return 0.5
        }

        var total = 0.0
        for (tt in target) {
            total += product.minOf { stemDistance(tt, it) }
        }
        total += abs(target.size - product.size) * 0.5
        return total
    }

    /**
     * Minimum edit distance across the candidate stems of both tokens.
     */
    private fun stemDistance(target: String, product: String): Double {
        var best = editDistance(target, product).toDouble()
        for (targetStem in stems(target)) {
            for (productStem in stems(product)) {
                val distance = editDistance(targetStem, productStem).toDouble()
                if (distance < best) best = distance
            }
        }
        return best
    }

    private fun isSubsequence(a: List<String>, b: List<String>): Boolean {
        if (a.isEmpty()) return true
        var i = 0
        for (tb in b) {
            if (tb == a[i]) {
                i++
                if (i == a.size) return true
            }
        }
        return false
    }

    /**
     * Damerau-Levenshtein (optimal string alignment) distance, which also
     * accounts for adjacent transpositions common in speech-to-text output.
     */
    private fun editDistance(lhs: String, rhs: String): Int {
        val n = lhs.length
        val m = rhs.length
        if (n == 0) return m
        if (m == 0) return n

        val d = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) d[i][0] = i
        for (j in 0..m) d[0][j] = j

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                d[i][j] = min(
                    min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                    d[i - 1][j - 1] + cost
                )
                if (i > 1 && j > 1 && lhs[i - 1] == rhs[j - 2] && lhs[i - 2] == rhs[j - 1]) {
                    d[i][j] = min(d[i][j], d[i - 2][j - 2] + cost)
                }
            }
        }
        return d[n][m]
    }
}
