package org.banana.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.banana.project.presentation.CreateProductViewModel
import org.banana.project.ui.components.RetroCard
import org.banana.project.ui.theme.TechniColors

@Composable
fun CreateProductScreen(
    onProductCreated: () -> Unit,
    viewModel: CreateProductViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val sellPrice by viewModel.sellPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RetroCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TechniColors.Crimson
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "CREATE NEW PRODUCT",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TechniColors.Cream
                )

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Product Name", color = TechniColors.Cream) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description", color = TechniColors.Cream) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Sell Price field
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = viewModel::updateSellPrice,
                    label = { Text("Sell Price", color = TechniColors.Cream) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = TechniColors.Guayaba,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Success message
                successMessage?.let {
                    Text(
                        text = it,
                        color = TechniColors.Emerald,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create button
                Button(
                    onClick = {
                        viewModel.createProduct(onProductCreated)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechniColors.Emerald
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = TechniColors.Cream,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Creating...", color = TechniColors.Cream)
                    } else {
                        Text("Create Product", color = TechniColors.Cream)
                    }
                }
            }
        }
    }
}