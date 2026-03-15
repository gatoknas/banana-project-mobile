package org.banana.project.navigation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import org.banana.project.utils.AppLogger
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.R
import org.banana.project.viewmodels.SellCreationViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.fillMaxWidth
import org.banana.project.ui.components.RetroCard
import org.banana.project.utils.ParsedItem

class SellCreationScreen() : Screen {

    @Composable
    override fun Content() {
        val viewModel = hiltViewModel<SellCreationViewModel>()
        val parsedItems by viewModel.parsedItems.collectAsState()
        
        val context = LocalContext.current
        var recognizedText by remember { mutableStateOf("Press and hold the microphone to speak") }
        var isRecording by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var hasPermission by remember { mutableStateOf(false) }

        var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
        
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                hasPermission = isGranted
                if (!isGranted) {
                    errorMessage = "Microphone permission is required for voice input"
                }
            }
        )

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        DisposableEffect(Unit) {
            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    recognizedText = "Listening..."
                    errorMessage = null
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    errorMessage = "Error occurred: \$error"
                    isRecording = false
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedText = matches[0]
                        viewModel.parseSpeechInput(matches[0])
                    }
                    isRecording = false
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        recognizedText = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }

            onDispose {
                speechRecognizer?.destroy()
            }
        }

        val speechIntent = remember {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Left Column: Mic and Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    MicrophoneAndStatus(
                        isRecording = isRecording,
                        hasPermission = hasPermission,
                        recognizedText = recognizedText,
                        errorMessage = errorMessage,
                        onPressStart = {
                            AppLogger.d("Microphone button pressed")
                            if (hasPermission) {
                                try {
                                    isRecording = true
                                    errorMessage = null
                                    speechRecognizer?.startListening(speechIntent)
                                } catch (e: Exception) {
                                    AppLogger.e("Error starting speech recognition", e)
                                    errorMessage = "Failed to start microphone: \${e.localizedMessage}"
                                    isRecording = false
                                }
                            } else {
                                errorMessage = "Please grant microphone permission first"
                            }
                        },
                        onPressEnd = {
                            AppLogger.d("Microphone button released")
                            if (isRecording) {
                                try {
                                    isRecording = false
                                    speechRecognizer?.stopListening()
                                } catch (e: Exception) {
                                    AppLogger.e("Error stopping speech recognition", e)
                                    errorMessage = "Failed to stop microphone: \${e.localizedMessage}"
                                }
                            }
                        }
                    )
                }

                // Right Column: Table
                if (parsedItems.isNotEmpty()) {
                    Box(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                        ParsedResultsTable(items = parsedItems)
                    }
                }
            }
        } else {
            // Portrait
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MicrophoneAndStatus(
                    isRecording = isRecording,
                    hasPermission = hasPermission,
                    recognizedText = recognizedText,
                    errorMessage = errorMessage,
                    onPressStart = {
                        AppLogger.d("Microphone button pressed")
                        if (hasPermission) {
                            try {
                                isRecording = true
                                errorMessage = null
                                speechRecognizer?.startListening(speechIntent)
                            } catch (e: Exception) {
                                AppLogger.e("Error starting speech recognition", e)
                                errorMessage = "Failed to start microphone: \${e.localizedMessage}"
                                isRecording = false
                            }
                        } else {
                            errorMessage = "Please grant microphone permission first"
                        }
                    },
                    onPressEnd = {
                        AppLogger.d("Microphone button released")
                        if (isRecording) {
                            try {
                                isRecording = false
                                speechRecognizer?.stopListening()
                            } catch (e: Exception) {
                                AppLogger.e("Error stopping speech recognition", e)
                                errorMessage = "Failed to stop microphone: \${e.localizedMessage}"
                            }
                        }
                    }
                )

                if (parsedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        ParsedResultsTable(items = parsedItems)
                    }
                }
            }
        }
    }
}

@Composable
fun MicrophoneAndStatus(
    isRecording: Boolean,
    hasPermission: Boolean,
    recognizedText: String,
    errorMessage: String?,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    MicrophoneButton(
        isRecording = isRecording,
        onPressStart = onPressStart,
        onPressEnd = onPressEnd
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = recognizedText,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ParsedResultsTable(items: List<ParsedItem>) {
    RetroCard(
        backgroundColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.15f), textAlign = TextAlign.Center)
                Text(text = "Cantidad", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.25f), textAlign = TextAlign.Center)
                Text(text = "Producto", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
            }
            
            androidx.compose.material3.HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(items) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontSize = 18.sp,
                            modifier = Modifier.weight(0.15f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${item.quantity}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(0.25f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = item.name,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    if (index < items.size - 1) {
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MicrophoneButton(
    isRecording: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val baseSize = (configuration.screenHeightDp * 0.3f).dp
    val ripple1Size = baseSize * 1.5f
    val ripple2Size = baseSize * 1.2f

    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val idleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(ripple1Size)
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(ripple1Size)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .size(ripple2Size)
                    .scale(scale * 1.1f)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.5f))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(ripple2Size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = idleAlpha))
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(baseSize)
                .clip(CircleShape)
                .background(if (isRecording) Color.Red else MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPressStart()
                            tryAwaitRelease()
                            onPressEnd()
                        }
                    )
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.retro_microphone),
                contentDescription = "Microphone",
                modifier = Modifier.size(baseSize * 0.6f)
            )
        }
    }
}

