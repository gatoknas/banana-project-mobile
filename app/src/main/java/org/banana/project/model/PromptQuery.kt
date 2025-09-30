package org.banana.project.model

/**
 * A data class representing a query and its corresponding response.
 *
 * @property input The input text for the query.
 * @property response The response text generated from the input.
 */
data class PromptQuery(
    val input: String,
    val response: String
)