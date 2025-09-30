package org.banana.project.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.banana.project.model.ParserResponse
import org.banana.project.services.Interfaces.ISpeechParser

class SpeechParserService() : ISpeechParser{

    override fun GetProductsAndQuantities(rawTextInput: String): ParserResponse {

        val productsAndQuantities = parseShoppingListToJson_MultiWordItems(rawTextInput)
        return ParserResponse(productsAndQuantities)
     }
    
private object SpanishNumberParser {
    // Léxico de numerales en español
    private val numberWords = mapOf(
        "cero" to 0, "uno" to 1,"una" to 1, "un" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4,
        "cinco" to 5, "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9,
        "diez" to 10, "once" to 11, "doce" to 12, "trece" to 13, "catorce" to 14,
        "quince" to 15, "dieciséis" to 16, "diecisiete" to 17, "dieciocho" to 18,
        "diecinueve" to 19, "veinte" to 20, "veintiuno" to 21, "veintiún" to 21,
        "veintidós" to 22, "veintitrés" to 23, "veinticuatro" to 24, "veinticinco" to 25,
        "veintiséis" to 26, "veintisiete" to 27, "veintiocho" to 28, "veintinueve" to 29,
        "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50, "sesenta" to 60,
        "setenta" to 70, "ochenta" to 80, "noventa" to 90, "cien" to 100, "ciento" to 100,
        "doscientos" to 200, "trescientos" to 300, "cuatrocientos" to 400, "quinientos" to 500,
        "seiscientos" to 600, "setecientos" to 700, "ochocientos" to 800, "novecientos" to 900,
        "mil" to 1000
    )

    fun isNumberWord(token: String): Boolean {
        return token.toIntOrNull()!= null || numberWords.containsKey(token.lowercase()) || token.lowercase() == "y"
    }

    fun parseWords(text: String): Int? {
        val tokens = text.lowercase().split(" ").filter { it.isNotBlank() && it!= "y" }
        if (tokens.isEmpty()) return null
        var totalValue = 0
        var currentValue = 0
        try {
            for (token in tokens) {
                val value = numberWords[token]?: return null
                if (value == 1000) {
                    currentValue = if (currentValue == 0) 1 else currentValue
                    totalValue += currentValue * 1000
                    currentValue = 0
                } else {
                    currentValue += value
                }
            }
            totalValue += currentValue
            return totalValue
        } catch (e: Exception) {
            return null
        }
    }
}

    private fun parseNumber(text: String): Int? {
        return text.trim().toIntOrNull()?: SpanishNumberParser.parseWords(text.trim())
    }

   /**
 * (NUEVA FUNCIÓN MEJORADA) Parsea una cadena de lista de compras con nombres de artículo compuestos.
 * Es capaz de manejar entradas como "veinticinco salpicones con helado cuarenta galletas de chocolate".
 *
 * @param input La cadena de entrada con formato de lenguaje natural.
 * @return Una cadena JSON con tipos consistentes (recomendado).
 */
fun parseShoppingListToJson_MultiWordItems(input: String): String {
    if (input.isBlank()) return "{}"

    val correctedInput = input.replace("treinca", "treinta")
    val tokens = correctedInput.split(" ").filter { it.isNotBlank() }
    
    val resultMap = mutableMapOf<String, Int>()
    var quantityBuffer = mutableListOf<String>()
    var itemBuffer = mutableListOf<String>()

    fun processCurrentItem() {
        if (quantityBuffer.isNotEmpty() && itemBuffer.isNotEmpty()) {
            val quantityStr = quantityBuffer.joinToString(" ")
            val itemName = itemBuffer.joinToString(" ").trim()
            parseNumber(quantityStr)?.let { numericValue ->
                resultMap[itemName] = numericValue
            }
        }
        quantityBuffer.clear()
        itemBuffer.clear()
    }

    for (token in tokens) {
        if (SpanishNumberParser.isNumberWord(token)) {
            // Si encontramos un número y ya estábamos construyendo un artículo,
            // significa que el artículo anterior ha terminado.
            if (itemBuffer.isNotEmpty()) {
                processCurrentItem()
            }
            quantityBuffer.add(token)
        } else {
            // Si no es un número, es parte del nombre del artículo.
            // Solo empezamos a recolectar el nombre si ya tenemos una cantidad.
            if (quantityBuffer.isNotEmpty()) {
                itemBuffer.add(token)
            }
        }
    }

    // Procesar el último artículo que queda en los búferes al final de la cadena
    processCurrentItem()

    return Json.encodeToString(resultMap)
}


    fun parseShoppingListToJson_TypeSafe(input: String): String {
        if (input.isBlank()) return "{}"

        val itemRegex = Regex("\\s*,\\s*|\\s+y\\s+")
        val partsRegex = Regex("""^(.+?)\s+([a-zA-Záéíóúñ]+)$""")

        val resultMap = mutableMapOf<String, Int>()

        input.split(itemRegex).forEach { chunk ->
            val trimmedChunk = chunk.trim()
            if (trimmedChunk.isNotEmpty()) {
                partsRegex.find(trimmedChunk)?.let { matchResult ->
                    val (quantityStr, itemName) = matchResult.destructured
                    parseNumber(quantityStr)?.let { numericValue ->
                        resultMap[itemName] = numericValue
                    }
                }
            }
        }

        // Serialización automática y segura de un Map<String, Int>
        return Json.encodeToString(resultMap)
    }
}