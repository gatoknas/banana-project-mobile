package org.banana.project.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.banana.project.model.PromptQuery
import org.banana.project.services.Interfaces.ILlmService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun LlmTestScreen(llmService: ILlmService) {
    var inputText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Text(
            text = "Gemma LLM Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Companion.Bold,
            modifier = Modifier.Companion.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter your prompt") },
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 3,
            maxLines = 5
        )

        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            // Test using processPrompt directly
                            responseText = llmService.processPrompt(inputText)
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            Log.e("MainActivity", "Error processing prompt", e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && inputText.isNotBlank(),
                modifier = Modifier.Companion.padding(end = 8.dp)
            ) {
                Text("Process Prompt")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            // Test using getLlmResponse
                            val query = PromptQuery(inputText, "")
                            val response = llmService.getLlmResponse(query)
                            responseText = response.response
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            Log.e("MainActivity", "Error getting LLM response", e)
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && inputText.isNotBlank(),
                modifier = Modifier.Companion.padding(start = 8.dp)
            ) {
                Text("Get LLM Response")
            }
        }

        // Sample prompts
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp)
            ) {
                Text(
                    text = "Sample Prompts:",
                    fontWeight = FontWeight.Companion.Bold,
                    modifier = Modifier.Companion.padding(bottom = 8.dp)
                )

                val samplePrompts = listOf(
                    "What is the capital of France?",
                    "Explain quantum computing in simple terms",
                    "Write a haiku about Android development",
                    "What are the benefits of Kotlin?"
                )

                samplePrompts.forEach { prompt ->
                    TextButton(
                        onClick = { inputText = prompt },
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text(
                            text = prompt,
                            modifier = Modifier.Companion.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.Companion.padding(16.dp)
            )
            Text("Processing... This may take a moment on first run.")
        }

        errorMessage?.let { error ->
            Card(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.Companion.padding(16.dp)
                )
            }
        }

        if (responseText.isNotBlank()) {
            Card(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.Companion.padding(16.dp)
                ) {
                    Text(
                        text = "Response:",
                        fontWeight = FontWeight.Companion.Bold,
                        modifier = Modifier.Companion.padding(bottom = 8.dp)
                    )
                    Text(text = responseText)
                }
            }
        }

        // Model status info
        Card(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp)
            ) {
                Text(
                    text = "Model Status",
                    fontWeight = FontWeight.Companion.Bold,
                    modifier = Modifier.Companion.padding(bottom = 8.dp)
                )
                Text(
                    text = "⚠️ Note: Ensure gemma-2b-it.bin is in app/src/main/assets/",
                    modifier = Modifier.Companion.padding(bottom = 4.dp)
                )
                Text(
                    text = "See README_MODEL.md for download instructions",
                    modifier = Modifier.Companion.padding(bottom = 4.dp)
                )
            }
        }
    }
}