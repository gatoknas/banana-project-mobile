package org.banana.project.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.ui.screens.DashboardScreen as DashboardComposable

class DashboardScreen(private val windowSizeClass: WindowSizeClass) : Screen {

    @Composable
    override fun Content() {
        DashboardComposable(windowSizeClass = windowSizeClass)
    }
}