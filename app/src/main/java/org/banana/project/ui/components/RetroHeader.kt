package org.banana.project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import coil.compose.AsyncImage
import org.banana.project.R
import org.banana.project.model.User
import org.banana.project.ui.theme.TechniColors
import org.banana.project.ui.theme.retroOutline
import org.banana.project.ui.theme.retroShadow

@Composable
fun RetroHeader(
    activeScreen: Screen?,
    user: User?,
    onLogout: () -> Unit,
    onDashboardClick: () -> Unit,
    onCreateProductClick: () -> Unit,
    onSellCreationClick: () -> Unit
) {
    RetroCard(
        backgroundColor = MaterialTheme.colorScheme.primary,
        borderColor = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ayurami_logo),
                contentDescription = "Ayurami Logo",
                modifier = Modifier.height(40.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            HeaderMenu(
                activeScreen = activeScreen,
                modifier = Modifier.weight(1f),
                onDashboardClick = onDashboardClick,
                onCreateProductClick = onCreateProductClick,
                onSellCreationClick = onSellCreationClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (user != null) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = user.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.role,
                        color = TechniColors.Goldenrod,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                AsyncImage(
                    model = user.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, Color.Black, CircleShape)
                        .padding(2.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            SalirButton(onClick = onLogout)
        }
    }
}

@Composable
private fun SalirButton(onClick: () -> Unit) {
    val shadowColor = MaterialTheme.colorScheme.retroShadow
    val borderColor = MaterialTheme.colorScheme.retroOutline
    Box(
        modifier = Modifier
            .padding(bottom = 4.dp, end = 4.dp)
            .drawBehind {
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = this.size,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
            .background(MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(10.dp))
            .border(2.dp, borderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Salir",
            color = MaterialTheme.colorScheme.onTertiary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
fun RetroHeaderPreview() {
    RetroHeader(
        activeScreen = null,
        user = User(
            id = "1",
            username = "admin",
            name = "Administrador Ayurami",
            role = "Administrador"
        ),
        onLogout = {},
        onDashboardClick = {},
        onCreateProductClick = {},
        onSellCreationClick = {}
    )
}
