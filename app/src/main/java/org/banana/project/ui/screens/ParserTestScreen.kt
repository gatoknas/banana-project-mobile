package org.banana.project.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import org.banana.project.R
import org.banana.project.presentation.ParserTestViewModel
import org.banana.project.ui.components.RetroCard

@Composable
fun ParserTestScreen(viewModel: ParserTestViewModel = hiltViewModel()) {
    var inputText by remember { mutableStateOf("") }
    val result by viewModel.result.collectAsState()
    var isListening by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                Log.e("ParserTestScreen", "Speech recognition error: $error")
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let {
                    inputText = it
                    viewModel.processInput(it)
                    Toast.makeText(context, "Speech recognized and processed", Toast.LENGTH_SHORT).show()
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
    speechRecognizer.setRecognitionListener(recognitionListener)

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("ParserTestScreen", "Permission result: $isGranted")
        if (isGranted) {
            Log.d("ParserTestScreen", "Permission granted")
            Toast.makeText(context, "Microphone permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Check if we should show rationale or direct to settings
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(context as android.app.Activity, Manifest.permission.RECORD_AUDIO)
            Log.d("ParserTestScreen", "Permission denied, shouldShowRationale: $shouldShowRationale")
            if (!shouldShowRationale) {
                // User has denied permission and checked "don't ask again"
                Log.d("ParserTestScreen", "Permission permanently denied, opening settings")
                Toast.makeText(context, "Microphone permission is required. Please enable it in app settings.", Toast.LENGTH_LONG).show()
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Microphone permission required for speech input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizer.startListening(intent)
                isListening = true
                Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            speechRecognizer.stopListening()
            isListening = false
        }
    }

    RetroCard {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter shopping list text") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { viewModel.processInput(inputText) }) {
                    Text("Parse")
                }

                IconButton(
                    onClick = {},
                    interactionSource = interactionSource,
                    modifier = Modifier.size(248.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.retro_microphone),
                        contentDescription = "Hold to speak for speech input",
                        tint = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Result: $result")
        }
    }


}