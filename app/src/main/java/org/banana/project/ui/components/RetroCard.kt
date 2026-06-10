package org.banana.project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.banana.project.R
import org.banana.project.ui.theme.BananaProjectTheme

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset

import org.banana.project.ui.theme.retroShadow

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable () -> Unit
) {
    val shadowColor = MaterialTheme.colorScheme.retroShadow
    Box(
        modifier = modifier
            .padding(bottom = 8.dp, end = 8.dp) // padding so shadow isn't clipped
            .drawBehind {
                drawRoundRect(
                    color = shadowColor.copy(alpha = 0.85f),
                    topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
                    size = this.size,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .border(
                width = 6.dp, // Match web app's border-6
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(5.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.paper_texture),
            contentDescription = "Background texture",
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            alpha = 0.03f,
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RetroCardPreview() {
    BananaProjectTheme {
        RetroCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Text(
                text = "Retro Card Content",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
