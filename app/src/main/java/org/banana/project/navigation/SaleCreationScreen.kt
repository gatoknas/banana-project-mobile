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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cafe.adriel.voyager.core.screen.Screen
import android.content.res.Configuration
import org.banana.project.viewmodels.SaleCreationViewModel
import org.banana.project.ui.components.MicrophoneAndStatus
import org.banana.project.ui.components.ParsedResultsTable
import org.banana.project.ui.theme.retroOutline
import org.banana.project.ui.theme.retroShadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import java.text.NumberFormat
import java.util.Locale

class SaleCreationScreen() : Screen {

    @Composable
    override fun Content() {
        val viewModel = hiltViewModel<SaleCreationViewModel>()
        val parsedItems by viewModel.parsedItems.collectAsStateWithLifecycle()
        val mergedKeys by viewModel.mergedItemKeys.collectAsStateWithLifecycle()
        val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
        val submitResult by viewModel.submitResult.collectAsStateWithLifecycle()
        
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
                is SaleCreationViewModel.SubmitResult.Success -> {
                    recognizedText = defaultMessage
                    snackbarHostState.showSnackbar(
                        message = "¡Venta registrada exitosamente!",
                        duration = SnackbarDuration.Short
                    )
                    viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.ClearSubmitResult)
                }
                is SaleCreationViewModel.SubmitResult.Error -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Long
                    )
                    viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.ClearSubmitResult)
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
                        viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.ParseSpeech(matches[0]))
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!hasUnmatched) {
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.SubmitSale)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
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
            val shadowColor = MaterialTheme.colorScheme.retroShadow
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(54.dp)
                        .padding(bottom = 6.dp, end = 6.dp) // Leave spacing for the drawn shadow
                        .drawBehind {
                            drawRoundRect(
                                color = shadowColor,
                                topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
                                size = this.size,
                                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                            )
                        },
                    enabled = parsedItems.isNotEmpty() && !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.retroOutline)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
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
                                    onItemRemoved = { viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.RemoveItem(it)) },
                                    onQuantityChanged = { item, newQty -> viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.UpdateItemQuantity(item, newQty)) },
                                    mergedItemKeys = mergedKeys,
                                    onMergedAnimationComplete = { viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.ClearMergedKeys) }
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
                                onItemRemoved = { viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.RemoveItem(it)) },
                                onQuantityChanged = { item, newQty -> viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.UpdateItemQuantity(item, newQty)) },
                                mergedItemKeys = mergedKeys,
                                onMergedAnimationComplete = { viewModel.onEvent(SaleCreationViewModel.SaleCreationEvent.ClearMergedKeys) }
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
