package org.banana.project.services.Interfaces

import org.banana.project.model.PromptQuery

interface ILlmService {

    suspend fun getLlmResponse(prompt: PromptQuery): PromptQuery
    
    /**
     * Process a text prompt using the local LLM model
     * @param prompt The input text to process
     * @return The generated response from the model
     */
    suspend fun processPrompt(prompt: String): String

}