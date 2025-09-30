package org.banana.project.services

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.banana.project.model.PromptQuery
import org.banana.project.services.Interfaces.ILlmService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LlmService @Inject constructor(
    @ApplicationContext private val context: Context
) : ILlmService {

    companion object {
        private const val TAG = "LlmService"
        private const val MODEL_PATH = "gemma-2b-it-cpu-int4.bin"
        private const val MAX_TOKENS = 1024
        private const val TEMPERATURE = 0.7f
        private const val TOP_K = 40
        private const val RANDOM_SEED = 101
    }

    private var llmInference: LlmInference? = null
    private var isModelInitialized = false

    /**
     * Initialize the MediaPipe Gemma model
     * This should be called before using processPrompt
     */
    private suspend fun initializeModel() = withContext(Dispatchers.IO) {
        if (isModelInitialized) return@withContext

        try {
            Log.d(TAG, "Initializing Gemma3-1B-IT model...")
            Log.d(TAG, "Model path: $MODEL_PATH")

            // Check if asset exists
            val assetManager = context.assets
            val assetFilename = MODEL_PATH
            try {
                val inputStream = assetManager.open(assetFilename)
                inputStream.close()
                Log.d(TAG, "Asset file found: $assetFilename")
            } catch (e: Exception) {
                Log.e(TAG, "Asset file not found: $assetFilename", e)
                throw RuntimeException("Model asset not found: $assetFilename", e)
            }

            // Copy asset to temp file for MediaPipe
            val tempFile = File.createTempFile("gemma_model", ".bin")
            tempFile.deleteOnExit()
            context.assets.open(assetFilename).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Create MediaPipe LLM Inference options
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(tempFile.absolutePath)
                .setMaxTokens(MAX_TOKENS)
                .setTemperature(TEMPERATURE)
                .setTopK(TOP_K)
                .setRandomSeed(RANDOM_SEED)
                .build()

            // Initialize the model
            llmInference = LlmInference.createFromOptions(context, options)
            Log.d(TAG, "Model file copied to: ${tempFile.absolutePath}")
            isModelInitialized = true

            Log.d(TAG, "Model initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model", e)
            throw e
        }
    }

    /**
     * Process a text prompt using the local LLM model
     * @param prompt The input text to process
     * @return The generated response from the model
     */
    override suspend fun processPrompt(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure model is initialized
                if (!isModelInitialized) {
                    initializeModel()
                }

                // Generate response using MediaPipe
                val response = llmInference?.generateResponse(prompt)

                if (response.isNullOrEmpty()) {
                    Log.w(TAG, "Model returned empty response, using fallback")
                    return@withContext getFallbackResponse(prompt)
                }

                Log.d(TAG, "Generated response: ${response.take(100)}...")
                response
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                getFallbackResponse(prompt, e.message)
            }
        }
    }

    /**
     * Takes a prompt and returns a response from the LLM.
     * Now uses the local Gemma model via processPrompt
     */
    override suspend fun getLlmResponse(prompt: PromptQuery): PromptQuery {
        val response = try {
            // Try to use the local model first
            processPrompt(prompt.input)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to use local model, falling back", e)
            getFallbackResponse(prompt.input, e.message)
        }

        return PromptQuery(
            input = prompt.input,
            response = response
        )
    }

    /**
     * Provides a fallback response when the model is not available
     */
    private fun getFallbackResponse(prompt: String, error: String? = null): String {
        return if (error != null) {
            "Model unavailable (Error: $error). Fallback response for: '$prompt'"
        } else {
            "This is a fallback response for the input: '$prompt'. Please ensure the Phi-3 model file is in assets/"
        }
    }

    /**
     * Clean up resources when the service is no longer needed
     */
    fun cleanup() {
        try {
            llmInference?.close()
            llmInference = null
            isModelInitialized = false
            Log.d(TAG, "Model resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}