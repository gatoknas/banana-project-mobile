package org.banana.project.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class SpanishParserHelperTest {

    @Test
    fun `parseSpeech TDT scenarios`() {
        data class TestCase(
            val name: String,
            val speechInput: String,
            val expectedItems: List<ParsedItem>
        )

        val testCases = listOf(
            TestCase(
                name = "Empty input",
                speechInput = "",
                expectedItems = emptyList()
            ),
            TestCase(
                name = "Single item with digit number",
                speechInput = "5 manzanas",
                expectedItems = listOf(ParsedItem(5, "manzanas"))
            ),
            TestCase(
                name = "Single item with word number",
                speechInput = "cinco manzanas",
                expectedItems = listOf(ParsedItem(5, "manzanas"))
            ),
            TestCase(
                name = "Compound word number",
                speechInput = "treinta y cinco limones",
                expectedItems = listOf(ParsedItem(35, "limones"))
            ),
            TestCase(
                name = "Implicit quantity of 1",
                speechInput = "pera",
                expectedItems = listOf(ParsedItem(1, "pera"))
            ),
            TestCase(
                name = "Multiple items sequentially",
                speechInput = "dos manzanas tres peras",
                expectedItems = listOf(
                    ParsedItem(2, "manzanas"),
                    ParsedItem(3, "peras")
                )
            )
        )

        testCases.forEach { tc ->
            val actual = SpanishParserHelper.parseSpeech(tc.speechInput)
            assertEquals("Failed scenario: ${tc.name}", tc.expectedItems, actual)
        }
    }
}
