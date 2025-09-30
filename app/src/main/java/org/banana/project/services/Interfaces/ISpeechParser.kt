package org.banana.project.services.Interfaces

import org.banana.project.model.ParserResponse

interface ISpeechParser {

    fun GetProductsAndQuantities(rawTextInput: String) : ParserResponse
}