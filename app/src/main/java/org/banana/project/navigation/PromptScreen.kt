package org.banana.project.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.ui.screens.PromptScreen as PromptComposable

object PromptScreen : Screen {

    @Composable
    override fun Content() {
        PromptComposable()
    }
}