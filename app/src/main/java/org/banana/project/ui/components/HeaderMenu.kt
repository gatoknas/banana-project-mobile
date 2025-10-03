package org.banana.project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.banana.project.ui.theme.TechniColors

@Composable
fun HeaderMenu(
    onDashboardClick: () -> Unit,
    onLlmTestClick: () -> Unit,
    onParserTestClick: () -> Unit,
    onPromptClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth() // Make it span the full width
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Text(
            modifier = Modifier
                .padding(10.dp)
                .clickable { onDashboardClick() }, // Make it clickable
            text = "Dashboard",
            color = TechniColors.Cream,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier
                .padding(10.dp)
                .clickable { onLlmTestClick() }, // Make it clickable
            text = "LLM Test",
            color = TechniColors.Cream,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier
                .padding(10.dp)
                .clickable { onParserTestClick() }, // Make it clickable
            text = "Parser Test",
            color = TechniColors.Cream,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier
                .padding(10.dp)
                .clickable { onPromptClick() }, // Make it clickable
            text = "Prompt",
            color = TechniColors.Cream,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}