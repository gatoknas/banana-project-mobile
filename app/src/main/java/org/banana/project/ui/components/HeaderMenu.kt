package org.banana.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import org.banana.project.navigation.CreateProductScreen
import org.banana.project.navigation.DashboardScreen
import org.banana.project.navigation.SaleCreationScreen
import org.banana.project.ui.theme.retroOutline
import org.banana.project.ui.theme.retroShadow

@Composable
fun RetroMenuItem(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    if (isActive) {
        val shadowColor = MaterialTheme.colorScheme.retroShadow
        val borderColor = MaterialTheme.colorScheme.retroOutline
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp, end = 4.dp) // Leave spacing for the drawn shadow
                .drawBehind {
                    // Draw solid retro drop shadow offset to bottom-right
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = this.size,
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }
                .background(MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(12.dp))
                .border(2.dp, borderColor, shape = RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 24.dp, vertical = 14.dp), // Increased size
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    } else {
        Text(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
                .clickable { onClick() },
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun HeaderMenu(
    activeScreen: Screen?,
    onDashboardClick: () -> Unit,
    onCreateProductClick: () -> Unit,
    onSellCreationClick: () -> Unit
){
    val isSellActive = activeScreen is SaleCreationScreen
    val isProductsActive = activeScreen is CreateProductScreen
    val isDashboardActive = activeScreen is DashboardScreen

    Row(
        modifier = Modifier
            .fillMaxWidth() // Make it span the full width
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ){
        RetroMenuItem(
            text = "Crear Venta",
            isActive = isSellActive,
            onClick = onSellCreationClick
        )
        RetroMenuItem(
            text = "Administrar Productos",
            isActive = isProductsActive,
            onClick = onCreateProductClick
        )
        RetroMenuItem(
            text = "Indicadores",
            isActive = isDashboardActive,
            onClick = onDashboardClick
        )
    }
}
