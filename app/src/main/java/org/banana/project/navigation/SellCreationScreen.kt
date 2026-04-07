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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import org.banana.project.ui.components.MicrophoneAndStatus
import org.banana.project.ui.components.ParsedResultsTable
import java.text.NumberFormat
import java.util.Locale

class SellCreationScreen() : Screen {

    @Composable
    override fun Content() {
        val viewModel = hiltViewModel<SellCreationViewModel>()
        val parsedItems by viewModel.parsedItems.collectAsState()
        val mergedKeys by viewModel.mergedItemKeys.collectAsState()
        val isSubmitting by viewModel.isSubmitting.collectAsState()
        val submitResult by viewModel.submitResult.collectAsState()
        
        val context = LocalContext.current
        val defaultMessage = "Presione y mantenga el micrófono para hablar"
        var recognizedText by remember { mutableStateOf(defaultMessage) }
        var isRecording by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var hasPermission by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }

        var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Handle submit result → show snackbar
        LaunchedEffect(submitResult) {
            when (val result = submitResult) {
                is SellCreationViewModel.SubmitResult.Success -> {
                    recognizedText = defaultMessage
                    snackbarHostState.showSnackbar(
                        message = "¡Venta registrada exitosamente!",
                        duration = SnackbarDuration.Short
                    )
                    viewModel.clearSubmitResult()
                }
                is SellCreationViewModel.SubmitResult.Error -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Long
                    )
                    viewModel.clearSubmitResult()
                }
                null -> {}
            }
        }

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
                    errorMessage = "Error occurred: $error"
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

        // Confirmation dialog
        if (showConfirmDialog) {
            val totalAmount = parsedItems.sumOf {
                (it.matchedProduct?.sellPrice ?: 0.0) * it.quantity
            }
            val itemCount = parsedItems.size
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val hasUnmatched = viewModel.hasUnmatchedItems

            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                icon = {
                    Icon(
                        imageVector = if (hasUnmatched) Icons.Default.Warning else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (hasUnmatched) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        if (hasUnmatched) "No se puede registrar" else "Confirmar venta",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    if (hasUnmatched) {
                        Column {
                            Text("Hay productos sin identificar en la lista. Elimínalos antes de registrar la venta:")
                            Spacer(modifier = Modifier.height(8.dp))
                            viewModel.unmatchedItemNames.forEach { name ->
                                Text(
                                    "• $name",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Column {
                            Text("¿Deseas registrar esta venta?")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "$itemCount producto${if (itemCount != 1) "s" else ""}",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Total: ${format.format(totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!hasUnmatched) {
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                viewModel.submitSell()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Registrar")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showConfirmDialog = false }) {
                        Text(if (hasUnmatched) "Entendido" else "Cancelar")
                    }
                }
            )
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Shared submit button composable
        val submitButton: @Composable () -> Unit = {
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = parsedItems.isNotEmpty() && !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Registrando...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Venta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        ) { paddingValues ->
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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
                                        errorMessage = "Failed to start microphone: ${e.localizedMessage}"
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
                                        errorMessage = "Failed to stop microphone: ${e.localizedMessage}"
                                    }
                                }
                            }
                        )
                    }

                    // Right Column: Table + Submit Button
                    if (parsedItems.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ParsedResultsTable(
                                    items = parsedItems,
                                    onItemRemoved = { viewModel.removeItem(it) },
                                    onQuantityChanged = { item, newQty -> viewModel.updateItemQuantity(item, newQty) },
                                    mergedItemKeys = mergedKeys,
                                    onMergedAnimationComplete = { viewModel.clearMergedKeys() }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            submitButton()
                        }
                    }
                }
            } else {
                // Portrait
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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
                                    errorMessage = "Failed to start microphone: ${e.localizedMessage}"
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
                                    errorMessage = "Failed to stop microphone: ${e.localizedMessage}"
                                }
                            }
                        }
                    )

                    if (parsedItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            ParsedResultsTable(
                                items = parsedItems,
                                onItemRemoved = { viewModel.removeItem(it) },
                                onQuantityChanged = { item, newQty -> viewModel.updateItemQuantity(item, newQty) },
                                mergedItemKeys = mergedKeys,
                                onMergedAnimationComplete = { viewModel.clearMergedKeys() }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        submitButton()
                    }
                }
            }
        }
    }
}
