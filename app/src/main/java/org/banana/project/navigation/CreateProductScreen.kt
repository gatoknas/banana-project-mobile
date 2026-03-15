package org.banana.project.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.ui.screens.CreateProductScreen as CreateProductComposable

class CreateProductScreen(private val windowSizeClass: WindowSizeClass) : Screen {

    @Composable
    override fun Content() {
        CreateProductComposable(
            onProductCreated = {
                // Navigate back to dashboard after successful creation
                // This will be handled by the navigator in MainActivity
            }
        )
    }
}