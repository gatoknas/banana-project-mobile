package org.banana.project.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.banana.project.model.ParsedSellItem
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import org.banana.project.model.Product
import java.time.Instant

@Composable
fun ParsedResultsTable(
    items: List<ParsedSellItem>,
    onItemRemoved: (ParsedSellItem) -> Unit = {}
) {
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
                Text(text = "#", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f), textAlign = TextAlign.Center)
                Text(text = "Cantidad", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center)
                Text(text = "Producto", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                Text(text = "Precio", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center)
            }
            
            androidx.compose.material3.HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.hashCode() }
                ) { index, item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                onItemRemoved(item)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    
                    val haptic = LocalHapticFeedback.current
                    LaunchedEffect(dismissState.targetValue) {
                        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }

                    val isSwiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled
                    
                    val beatScale = remember { androidx.compose.animation.core.Animatable(1f) }
                    LaunchedEffect(isSwiping) {
                        if (isSwiping) {
                            beatScale.animateTo(
                                targetValue = 0.96f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(250, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                        } else {
                            beatScale.animateTo(1f, animationSpec = tween(150))
                        }
                    }
                    
                    val smoothScale = beatScale.value
                    
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Item",
                                    tint = Color.White
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(smoothScale)
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontSize = 18.sp,
                                modifier = Modifier.weight(0.1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${item.quantity}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.2f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = item.parsedName,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(0.4f),
                                textAlign = TextAlign.Center
                            )
                            
                            val priceText = item.matchedProduct?.sellPrice?.let {
                                val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
                                format.format(it)
                            } ?: "N/A"
                            
                            Text(
                                text = priceText,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(0.3f),
                                textAlign = TextAlign.Center,
                                color = if (item.matchedProduct == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    if (index < items.size - 1) {
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            thickness = 1.dp
                        )
                    }
                }
                
                if (items.isNotEmpty()) {
                    item {
                        val total = items.sumOf { (it.matchedProduct?.sellPrice ?: 0.0) * it.quantity }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            thickness = 2.dp
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total:",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(0.7f),
                                textAlign = TextAlign.End
                            )
                            
                            val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
                            Text(
                                text = format.format(total),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(0.3f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParsedResultsTablePreview() {
    MaterialTheme {
        Surface {
            val mockProduct1 = Product(
                id = 1L,
                name = "Manzana",
                description = "Manzana roja",
                sellPrice = 1500.0,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            val mockProduct2 = Product(
                id = 2L,
                name = "Plátano",
                description = "Plátano maduro",
                sellPrice = 800.0,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

            val mockItems = listOf(
                ParsedSellItem(quantity = 2, parsedName = "Manzanas", matchedProduct = mockProduct1),
                ParsedSellItem(quantity = 5, parsedName = "Plátanos", matchedProduct = mockProduct2),
                ParsedSellItem(quantity = 1, parsedName = "Pera", matchedProduct = null)
            )

            Box(modifier = Modifier.padding(16.dp)) {
                ParsedResultsTable(items = mockItems)
            }
        }
    }
}
