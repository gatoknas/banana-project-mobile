package org.banana.project.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.ui.screens.ParserTestScreen as ParserTestComposable

object ParserTestScreen : Screen {

    @Composable
    override fun Content() {
        ParserTestComposable()
    }
}