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
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import dagger.hilt.android.AndroidEntryPoint
import org.banana.project.data.repository.AuthRepository
import org.banana.project.navigation.CreateProductScreen
import org.banana.project.navigation.DashboardScreen
import org.banana.project.navigation.SaleCreationScreen
import org.banana.project.ui.screens.LoginScreen
import org.banana.project.ui.components.RetroHeader
import org.banana.project.ui.theme.BananaProjectTheme
import org.banana.project.ui.theme.ThemeMode
import javax.inject.Inject

@ExperimentalMaterial3WindowSizeClassApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            var currentTheme by remember { mutableStateOf(ThemeMode.LIGHT) }
            val currentUser = remember { authRepository.getCurrentUser() }

            BananaProjectTheme(themeMode = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigator(screen = LoginScreen()) { navigator ->
                        val isLogin = navigator.lastItem is LoginScreen
                        val containerModifier = if (isLogin) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .background(color = MaterialTheme.colorScheme.background)
                                .fillMaxSize()
                                .padding(WindowInsets.systemBars.asPaddingValues())
                                .padding(16.dp)
                        }
                        Column(
                            modifier = containerModifier
                        ) {
                            if (!isLogin) {
                                RetroHeader(
                                    activeScreen = navigator.lastItem,
                                    user = currentUser,
                                    onLogout = {
                                        authRepository.logout()
                                        navigator.replaceAll(LoginScreen())
                                    },
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
                                        navigator.push(SaleCreationScreen())
                                    }
                                )
                            }
                            CurrentScreen()
                        }
                    }
                }
            }
        }
    }
}
