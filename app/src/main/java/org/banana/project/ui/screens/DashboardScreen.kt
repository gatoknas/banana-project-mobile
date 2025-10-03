package org.banana.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.banana.project.ui.theme.TechniColors

val cardHeight = 500.dp
val cardWidth = 450.dp

@Composable
fun DashboardScreen(
    windowSizeClass: WindowSizeClass
){
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val isMedium = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,

    ) {
        when {
            isExpanded -> {
                DashboardFlowRow(
                    cardColor1 = TechniColors.Crimson,
                    cardColor2 = TechniColors.Emerald,
                    cardColor3 = TechniColors.LessGolden,
                    cardColor4 = TechniColors.Emerald,
                    cardColor5 = TechniColors.Guayaba
                )
            }

            isMedium -> {

                DashboardFlowRow(
                    cardColor1 = TechniColors.Crimson,
                    cardColor2 = TechniColors.Emerald,
                    cardColor3 = TechniColors.LessGolden,
                    cardColor4 = TechniColors.Emerald,
                    cardColor5 = TechniColors.Guayaba
                )

            }

            else -> {
                // Stack all columns vertically for compact screens
                Column(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    DashboardColumn(
                        cardColor2 = TechniColors.skyBlue,
                        cardColor1 = TechniColors.Crimson
                    )
                    Spacer(Modifier.Companion.height(10.dp))
                    DashboardColumn(
                        cardColor1 = TechniColors.Guayaba,
                        cardColor2 = TechniColors.LessGolden
                    )
                    Spacer(Modifier.Companion.height(10.dp))
                    DashboardColumn(cardColor1 = Color(0xff016a4d), cardColor2 = Color(0xff2da5fa))
                }
            }
        }
    }
}

@Composable
private fun DashboardColumn(
    cardColor1: Color,
    cardColor2: Color
) {
    Column(
        modifier = Modifier.Companion
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        RetroCard(
            modifier = Modifier.Companion
                .weight(0.6f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor1
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "TOTAL REVENUE",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = TechniColors.GoldenTitle
                )
                Spacer(Modifier.Companion.height(18.dp))
                Text(
                    "$1,000,000",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = TechniColors.Cream
                )
            }
        }

        Spacer(Modifier.Companion.width(5.dp).weight(0.05f))

        RetroCard(
            modifier = Modifier.Companion
                .weight(1f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor2
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Retro Card Title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = Color.Companion.White
                )
                Spacer(Modifier.Companion.height(8.dp))
                Text(
                    "A cool retro styled component.",
                    fontSize = 14.sp,
                    color = Color.Companion.LightGray
                )
            }
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
        modifier = Modifier.Companion
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Companion.CenterVertically)
    ) {
        RetroCard(
            modifier = Modifier.Companion
                .weight(0.6f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor1
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "TOTAL REVENUE",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = TechniColors.GoldenTitle
                )
                Spacer(Modifier.Companion.height(18.dp))
                Text(
                    "$1,000,000",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = TechniColors.Cream
                )
            }
        }

        Spacer(Modifier.Companion.width(5.dp).weight(0.05f))

        RetroCard(
            modifier = Modifier.Companion
                .weight(1f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor2
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Retro Card Title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = Color.Companion.White
                )
                Spacer(Modifier.Companion.height(8.dp))
            }
        }

        Spacer(Modifier.Companion.width(5.dp).weight(0.05f))

        RetroCard(
            modifier = Modifier.Companion
                .weight(1f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor3
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Retro Card Title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = Color.Companion.White
                )
                Spacer(Modifier.Companion.height(8.dp))
                Text(
                    "A cool retro styled component.",
                    fontSize = 14.sp,
                    color = Color.Companion.LightGray
                )
            }
        }

        Spacer(Modifier.Companion.width(5.dp).weight(0.05f))

        RetroCard(
            modifier = Modifier.Companion
                .weight(1f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor4
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Retro Card Title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = Color.Companion.White
                )
                Spacer(Modifier.Companion.height(8.dp))
                Text(
                    "A cool retro styled component.",
                    fontSize = 14.sp,
                    color = Color.Companion.LightGray
                )
            }
        }

        Spacer(Modifier.Companion.width(5.dp).weight(0.05f))

        RetroCard(
            modifier = Modifier.Companion
                .weight(1f)
                .width(cardWidth)
                .height(cardHeight),
            backgroundColor = cardColor5
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Retro Card Title",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = Color.Companion.White
                )
                Spacer(Modifier.Companion.height(8.dp))
                Text(
                    "A cool retro styled component.",
                    fontSize = 14.sp,
                    color = Color.Companion.LightGray
                )
            }
        }
    }
}