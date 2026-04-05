package org.banana.project.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.banana.project.R

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
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = idleAlpha))
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
