package org.banana.project.utils

import java.text.Normalizer

object SpanishTextNormalizer {

    fun foldAccents(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
    }

    fun normalize(input: String): String {
        return foldAccents(input.lowercase().trim())
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
