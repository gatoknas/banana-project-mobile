package org.banana.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.banana.project.ui.components.RetroCard

val cardHeight = 250.dp
val cardWidth = 350.dp

@Composable
fun DashboardScreen(
    windowSizeClass: WindowSizeClass,
    isTokyoNight: Boolean = false,
    onThemeToggle: () -> Unit = {}
){
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = if (isTokyoNight) Icons.Default.Brightness4 else Icons.Default.BrightnessHigh,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        when {
            isExpanded || isMedium -> {
                DashboardFlowRow(
                    cardColor1 = primary,
                    cardColor2 = secondary,
                    cardColor3 = tertiary,
                    cardColor4 = surface,
                    cardColor5 = outline
                )
            }

            else -> {
                // Stack all columns vertically for compact screens
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DashboardCard(
                        title = "TOTAL REVENUE",
                        value = "$1,000,000",
                        backgroundColor = primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                    DashboardCard(
                        title = "ACTIVE USERS",
                        value = "1,234",
                        backgroundColor = secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                    DashboardCard(
                        title = "NEW ORDERS",
                        value = "56",
                        backgroundColor = tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                    DashboardCard(
                        title = "PENDING TASKS",
                        value = "12",
                        backgroundColor = surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                    DashboardCard(
                        title = "ALERTS",
                        value = "3",
                        backgroundColor = outline,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    backgroundColor: Color,
    contentColor: Color
) {
    RetroCard(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight),
        backgroundColor = backgroundColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(Modifier.height(18.dp))
            Text(
                value,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DashboardFlowRow(
    cardColor1: Color,
    cardColor2: Color,
    cardColor3: Color,
    cardColor4: Color,
    cardColor5: Color,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardCard(
            title = "TOTAL REVENUE",
            value = "$1,000,000",
            backgroundColor = cardColor1,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )

        DashboardCard(
            title = "ACTIVE USERS",
            value = "1,234",
            backgroundColor = cardColor2,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )

        DashboardCard(
            title = "NEW ORDERS",
            value = "56",
            backgroundColor = cardColor3,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )

        DashboardCard(
            title = "PENDING TASKS",
            value = "12",
            backgroundColor = cardColor4,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        DashboardCard(
            title = "ALERTS",
            value = "3",
            backgroundColor = cardColor5,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    }
}
