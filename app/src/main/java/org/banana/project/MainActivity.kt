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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import dagger.hilt.android.AndroidEntryPoint
import org.banana.project.navigation.DashboardScreen
import org.banana.project.navigation.LlmTestScreen
import org.banana.project.navigation.ParserTestScreen
import org.banana.project.navigation.PromptScreen
import org.banana.project.services.Interfaces.ILlmService
import org.banana.project.services.Interfaces.ISpeechParser
import org.banana.project.ui.components.RetroHeader
import org.banana.project.ui.theme.BananaProjectTheme
import org.banana.project.ui.theme.TechniColors
import javax.inject.Inject

@ExperimentalMaterial3WindowSizeClassApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var speechParser: ISpeechParser

    @Inject
    lateinit var llmService: ILlmService

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            BananaProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Navigator(screen = DashboardScreen(windowSizeClass)) { navigator ->
                        Column(
                            modifier = Modifier
                                .background(color = TechniColors.skyBlue)
                                .fillMaxSize()
                                .padding(WindowInsets.systemBars.asPaddingValues())
                                .padding(16.dp)
                        ) {
                            RetroHeader(
                                onDashboardClick = {
                                    navigator.replaceAll(
                                        DashboardScreen(
                                            windowSizeClass
                                        )
                                    )
                                },
                                onLlmTestClick = { navigator.replaceAll(LlmTestScreen(llmService)) },
                                onParserTestClick = { navigator.replaceAll(ParserTestScreen) },
                                onPromptClick = { navigator.replaceAll(PromptScreen) }
                            )
                            CurrentScreen()
                        }
                    }
                }
            }
        }
    }
}
