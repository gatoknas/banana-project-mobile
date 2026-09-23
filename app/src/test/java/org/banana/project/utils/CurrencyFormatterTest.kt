package org.banana.project.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    data class TestCase(
        val name: String,
        val input: Double,
        val expected: String
    )

    @Test
    fun `formatCop formats amounts according to Colombian Peso standards`() {
        val testCases = listOf(
            TestCase(
                name = "Standard product price 45000",
                input = 45000.0,
                expected = "$45.000,00"
            ),
            TestCase(
                name = "Standard product price 25000",
                input = 25000.0,
                expected = "$25.000,00"
            ),
            TestCase(
                name = "Large amount one million",
                input = 1000000.0,
                expected = "$1.000.000,00"
            ),
            TestCase(
                name = "Zero amount",
                input = 0.0,
                expected = "$0,00"
            ),
            TestCase(
                name = "Amount with fractional cents",
                input = 1234.50,
                expected = "$1.234,50"
            ),
            TestCase(
                name = "Small fractional amount",
                input = 0.99,
                expected = "$0,99"
            ),
            TestCase(
                name = "Rounding cents up",
                input = 99.999,
                expected = "$100,00"
            )
        )

        testCases.forEach { tc ->
            val actualDirect = CurrencyFormatter.formatCop(tc.input)
            val actualExtension = tc.input.toCopCurrency()

            assertEquals("Failed scenario '${tc.name}' via formatCop", tc.expected, actualDirect)
            assertEquals("Failed scenario '${tc.name}' via extension", tc.expected, actualExtension)
        }
    }
}
