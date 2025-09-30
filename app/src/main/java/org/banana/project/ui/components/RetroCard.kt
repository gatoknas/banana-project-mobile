package org.banana.project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.banana.project.R
import org.banana.project.ui.theme.TechniColors

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = TechniColors.Crimson,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = 8.dp,
                color = TechniColors.Goldenrod,
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