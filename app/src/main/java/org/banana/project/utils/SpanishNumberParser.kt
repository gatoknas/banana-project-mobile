package org.banana.project.utils



data class ParsedItem(
    val quantity: Int,
    val name: String
)

object SpanishParserHelper {
    fun parseSpeech(speech: String): List<ParsedItem> {
        AppLogger.i("Input speech: $speech")
        val parsedItems = mutableListOf<ParsedItem>()
        if (speech.isBlank()) return parsedItems

        // Normalize text
        val text = speech.lowercase().trim()
            .replace(",", "")
            .replace(".", "")

        val words = text.split("\\s+".toRegex())

        var currentQuantity: Int? = null
        val currentProductNameTokens = mutableListOf<String>()

        var i = 0
        while (i < words.size) {
            val (number, skipTokens) = extractNumber(words, i)

            if (number != null) {
                // If we hit a number and we already have a quantity/product, save it.
                if (currentQuantity != null && currentProductNameTokens.isNotEmpty()) {
                    parsedItems.add(ParsedItem(currentQuantity, currentProductNameTokens.joinToString(" ")))
                    currentProductNameTokens.clear()
                } else if (currentProductNameTokens.isNotEmpty()) {
                    // "Manzanas 5" -> Handle leading text without a number as qty 1
                    parsedItems.add(ParsedItem(1, currentProductNameTokens.joinToString(" ")))
                    currentProductNameTokens.clear()
                }
                
                currentQuantity = number
                i += skipTokens
            } else {
                val word = words[i]
                if (word == "y" && currentProductNameTokens.isEmpty()) {
                    // Skip floating "y" if no product name has started
                } else {
                    currentProductNameTokens.add(word)
                }
                i++
            }
        }

        // Finalize remaining
        if (currentQuantity != null) {
            val name = if (currentProductNameTokens.isEmpty()) "Producto Desconocido" else currentProductNameTokens.joinToString(" ")
            parsedItems.add(ParsedItem(currentQuantity, name))
        } else if (currentProductNameTokens.isNotEmpty() && parsedItems.isEmpty()) {
            parsedItems.add(ParsedItem(1, currentProductNameTokens.joinToString(" ")))
        }

        AppLogger.i("Parsed result: $parsedItems")
        return parsedItems
    }

    private val wordsToNumbers = mapOf(
        "un" to 1, "uno" to 1, "una" to 1,
        "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
        "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10,
        "once" to 11, "doce" to 12, "trece" to 13, "catorce" to 14, "quince" to 15,
        "dieciseis" to 16, "dieciséis" to 16, "diecisiete" to 17, "dieciocho" to 18, "diecinueve" to 19,
        "veinte" to 20, "veintiuno" to 21, "veintiun" to 21, "veintiuna" to 21,
        "veintidos" to 22, "veintidós" to 22, "veintitres" to 23, "veintitrés" to 23,
        "veinticuatro" to 24, "veinticinco" to 25, "veintiseis" to 26, "veintiséis" to 26,
        "veintisiete" to 27, "veintiocho" to 28, "veintinueve" to 29,
        "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50, "sesenta" to 60,
        "setenta" to 70, "ochenta" to 80, "noventa" to 90, "cien" to 100, "ciento" to 100
    )

    private fun extractNumber(words: List<String>, startIndex: Int): Pair<Int?, Int> {
        val firstToken = words[startIndex]

        val asDigit = firstToken.toIntOrNull()
        if (asDigit != null) return Pair(asDigit, 1)

        val firstVal = wordsToNumbers[firstToken]
        if (firstVal != null) {
            if (firstVal in listOf(30, 40, 50, 60, 70, 80, 90) && startIndex + 2 < words.size) {
                if (words[startIndex + 1] == "y") {
                    val secondVal = wordsToNumbers[words[startIndex + 2]]
                    if (secondVal != null && secondVal in 1..9) {
                        return Pair(firstVal + secondVal, 3)
                    }
                }
            }
            return Pair(firstVal, 1)
        }

        return Pair(null, 0)
    }
}
