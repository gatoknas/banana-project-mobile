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
    onInputDashboardClick: () -> Unit
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
            text = "Tablero",
            color = TechniColors.Cream,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier
                .padding(10.dp)
                .clickable { onInputDashboardClick() }, // Make it clickable
            text = "Compras",
            color = TechniColors.Cream,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}