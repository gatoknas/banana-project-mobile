package org.banana.project.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.services.Interfaces.ILlmService
import org.banana.project.ui.screens.LlmTestScreen as LlmTestComposable

class LlmTestScreen(private val llmService: ILlmService) : Screen {

    @Composable
    override fun Content() {
        LlmTestComposable(llmService = llmService)
    }
}