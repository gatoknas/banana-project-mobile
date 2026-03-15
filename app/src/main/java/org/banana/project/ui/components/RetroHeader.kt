package org.banana.project.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RetroHeader(
    onDashboardClick: () -> Unit,
    onCreateProductClick: () -> Unit,
    onSellCreationClick: () -> Unit
){
    RetroCard(
        backgroundColor = MaterialTheme.colorScheme.primary,
        borderColor = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ){
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,

            ) {
            Text(
                modifier = Modifier.padding(20.dp),
                text = "AYURAMI",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
            )
            HeaderMenu(onDashboardClick, onCreateProductClick, onSellCreationClick)
        }
    }
}

@Preview
@Composable
fun RetroHeaderPreview(){
    RetroHeader(onDashboardClick = {}, onCreateProductClick = {}, onSellCreationClick = {})
}
