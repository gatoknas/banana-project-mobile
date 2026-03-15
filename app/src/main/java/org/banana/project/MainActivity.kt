package org.banana.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.banana.project.data.DatabaseSeeder
import org.banana.project.navigation.CreateProductScreen
import org.banana.project.navigation.DashboardScreen
import org.banana.project.navigation.SellCreationScreen
import org.banana.project.ui.components.RetroHeader
import org.banana.project.ui.theme.BananaProjectTheme
import org.banana.project.ui.theme.ThemeMode
import org.banana.project.utils.AppLogger
import javax.inject.Inject

@ExperimentalMaterial3WindowSizeClassApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                databaseSeeder.seed()
            } catch (e: Exception) {
                AppLogger.e("Error executing database seeder: ${e.message}")
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            var currentTheme by remember { mutableStateOf(ThemeMode.LIGHT) }

            BananaProjectTheme(themeMode = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigator(screen = SellCreationScreen()) { navigator ->
                        Column(
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.background)
                                .fillMaxSize()
                                .padding(WindowInsets.systemBars.asPaddingValues())
                                .padding(16.dp)
                        ) {
                            RetroHeader(
                                onDashboardClick = {
                                    navigator.replaceAll(
                                        DashboardScreen(
                                            windowSizeClass = windowSizeClass,
                                            isTokyoNight = currentTheme == ThemeMode.TOKYO_NIGHT,
                                            onThemeToggle = {
                                                currentTheme = if (currentTheme == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
                                            }
                                        )
                                    )
                                },
                                onCreateProductClick = {
                                    navigator.replaceAll(CreateProductScreen(windowSizeClass))
                                },
                                onSellCreationClick = {
                                    navigator.push(SellCreationScreen())
                                }
                            )
                            CurrentScreen()
                        }
                    }
                }
            }
        }
    }
}
