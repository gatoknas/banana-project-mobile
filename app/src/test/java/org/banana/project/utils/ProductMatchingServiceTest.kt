package org.banana.project.utils

import org.banana.project.model.Product
import org.banana.project.model.ParsedSaleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ProductMatchingServiceTest {

    private val dbProducts = listOf(
        Product(1L, "Manzana", "Manzana roja", 1.5, Instant.now(), Instant.now()),
        Product(2L, "Lápiz", "Lápiz negro", 0.5, Instant.now(), Instant.now()),
        Product(3L, "Limón", "Limón verde", 0.8, Instant.now(), Instant.now()),
        Product(4L, "Naranja", "Naranja dulce", 1.2, Instant.now(), Instant.now())
    )

    @Test
    fun `matchParsedItemsToProducts TDT scenarios`() {
        data class TestCase(
            val name: String,
            val parsedItem: ParsedItem,
            val expectedProductId: Long?,
            val expectedParsedName: String
        )

        val testCases = listOf(
            TestCase(
                name = "Exact match",
                parsedItem = ParsedItem(2, "Manzana"),
                expectedProductId = 1L,
                expectedParsedName = "Manzana"
            ),
            TestCase(
                name = "Exact match lowercase",
                parsedItem = ParsedItem(1, "manzana"),
                expectedProductId = 1L,
                expectedParsedName = "manzana"
            ),
            TestCase(
                name = "Plural match ending in s",
                parsedItem = ParsedItem(3, "manzanas"),
                expectedProductId = 1L,
                expectedParsedName = "manzanas"
            ),
            TestCase(
                name = "Plural match ending in ces",
                parsedItem = ParsedItem(5, "lápices"),
                expectedProductId = 2L,
                expectedParsedName = "lápices"
            ),
            TestCase(
                name = "Plural match ending in es",
                parsedItem = ParsedItem(4, "limones"),
                expectedProductId = 3L,
                expectedParsedName = "limones"
            ),
            TestCase(
                name = "Fuzzy match with Levenshtein distance 1",
                parsedItem = ParsedItem(1, "naranjas"),
                expectedProductId = 4L,
                expectedParsedName = "naranjas"
            ),
            TestCase(
                name = "Fuzzy match with distance 2",
                parsedItem = ParsedItem(1, "manzan"),
                expectedProductId = 1L,
                expectedParsedName = "manzan"
            ),
            TestCase(
                name = "No match due to excessive distance",
                parsedItem = ParsedItem(1, "automóvil"),
                expectedProductId = null,
                expectedParsedName = "automóvil"
            )
        )

        testCases.forEach { tc ->
            val actualList = ProductMatchingService.matchParsedItemsToProducts(listOf(tc.parsedItem), dbProducts)
            val actualItem = actualList.first()
            assertEquals("Failed scenario: ${tc.name} (name check)", tc.expectedParsedName, actualItem.parsedName)
            if (tc.expectedProductId != null) {
                assertNotNull("Failed scenario: ${tc.name} (should match product)", actualItem.matchedProduct)
                assertEquals("Failed scenario: ${tc.name} (product ID check)", tc.expectedProductId, actualItem.matchedProduct!!.id)
            } else {
                assertNull("Failed scenario: ${tc.name} (should not match product)", actualItem.matchedProduct)
            }
        }
    }
}
