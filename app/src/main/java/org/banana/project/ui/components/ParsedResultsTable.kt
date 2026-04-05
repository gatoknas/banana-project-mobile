package org.banana.project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.banana.project.model.Product
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParsedResultsTable(
    items: List<ParsedSellItem>,
    onItemRemoved: (ParsedSellItem) -> Unit = {},
    onQuantityChanged: (ParsedSellItem, Int) -> Unit = { _, _ -> }
) {
    // Bottom sheet state
    var editingItem by remember { mutableStateOf<ParsedSellItem?>(null) }
    var tempQuantity by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Bottom Sheet for quantity editing
    if (editingItem != null) {
        ModalBottomSheet(
            onDismissRequest = { editingItem = null },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Item name header
                Text(
                    text = editingItem!!.parsedName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Modificar cantidad",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Stepper row:  [ – ]   quantity   [ + ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Decrease button
                    FilledIconButton(
                        onClick = { if (tempQuantity > 1) tempQuantity-- },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Disminuir",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Current quantity
                    Text(
                        text = "$tempQuantity",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(100.dp)
                    )

                    // Increase button
                    FilledIconButton(
                        onClick = { tempQuantity++ },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                editingItem = null
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            editingItem?.let { item ->
                                onQuantityChanged(item, tempQuantity)
                            }
                            scope.launch {
                                sheetState.hide()
                                editingItem = null
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Aceptar", fontSize = 16.sp)
                    }
                }
            }
        }
    }

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

                            // Tappable quantity — opens bottom sheet
                            Text(
                                text = "${item.quantity}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .weight(0.2f)
                                    .clickable {
                                        editingItem = item
                                        tempQuantity = item.quantity
                                    }
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
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
