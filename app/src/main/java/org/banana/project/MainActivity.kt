package org.banana.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.banana.project.services.Interfaces.ISpeechParser
import org.banana.project.ui.screens.ParserTestScreen
import org.banana.project.ui.theme.BananaProjectTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var speechParser: ISpeechParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BananaProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ParserTestScreen()
                }
            }
        }
    }
}

