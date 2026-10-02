package org.banana.project.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.banana.project.model.Product
import org.banana.project.presentation.CatalogViewMode
import org.banana.project.presentation.ProductCatalogEvent
import org.banana.project.presentation.ProductCatalogViewModel
import org.banana.project.ui.components.RetroCard
import org.banana.project.ui.theme.retroOutline
import org.banana.project.utils.CurrencyFormatter

/**
 * Read-only catalog of all products with their sale price.
 *
 * Supports name search, category filtering, and a grid/list toggle. It intentionally
 * offers no create, edit, or delete actions.
 */
@Composable
fun ProductCatalogScreen(
    viewModel: ProductCatalogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val products = viewModel.filteredProducts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CatalogHeader()

        CatalogFilters(
            searchQuery = state.searchQuery,
            onSearchChange = { viewModel.onEvent(ProductCatalogEvent.UpdateSearchQuery(it)) },
            selectedCategory = state.selectedCategory,
            categories = state.categories,
            onCategorySelected = { viewModel.onEvent(ProductCatalogEvent.SelectCategory(it)) },
            viewMode = state.viewMode,
            onViewModeChange = { viewModel.onEvent(ProductCatalogEvent.SetViewMode(it)) }
        )

        state.error?.let { message ->
            CatalogErrorBanner(
                message = message,
                onRetry = { viewModel.onEvent(ProductCatalogEvent.Retry) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                state.isLoading && state.products.isEmpty() -> CatalogLoading()

                products.isEmpty() -> CatalogEmpty(
                    hasError = state.error != null,
                    onRetry = { viewModel.onEvent(ProductCatalogEvent.Retry) }
                )

                state.viewMode == CatalogViewMode.GRID -> ProductGrid(products)

                else -> ProductList(products)
            }
        }
    }
}

@Composable
private fun CatalogHeader() {
    RetroCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primary,
        borderColor = MaterialTheme.colorScheme.secondary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "CATÁLOGO DE PRODUCTOS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Consulta el precio de venta de todos los productos.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun CatalogFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    viewMode: CatalogViewMode,
    onViewModeChange: (CatalogViewMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Buscar producto") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryDropdown(
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )

            ModeButton(
                text = "Tarjetas",
                selected = viewMode == CatalogViewMode.GRID,
                onClick = { onViewModeChange(CatalogViewMode.GRID) }
            )
            ModeButton(
                text = "Lista",
                selected = viewMode == CatalogViewMode.LIST,
                onClick = { onViewModeChange(CatalogViewMode.LIST) }
            )
        }
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: String?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedCategory ?: "Todas las categorías",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las categorías") },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.retroOutline),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun CatalogErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
            .border(2.dp, MaterialTheme.colorScheme.retroOutline, RoundedCornerShape(10.dp))
            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onRetry) {
            Text(
                text = "Reintentar",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ProductGrid(products: List<Product>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductGridCard(product)
        }
    }
}

@Composable
private fun ProductGridCard(product: Product) {
    RetroCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryBadge(product.category)
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            product.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = CurrencyFormatter.formatCop(product.sellPrice),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProductList(products: List<Product>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductListRow(product)
        }
    }
}

@Composable
private fun ProductListRow(product: Product) {
    RetroCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CategoryBadge(product.category)
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                product.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = CurrencyFormatter.formatCop(product.sellPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CategoryBadge(category: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .border(2.dp, MaterialTheme.colorScheme.retroOutline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CatalogLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
            Text(
                text = "Cargando catálogo...",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun CatalogEmpty(
    hasError: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RetroCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            borderColor = MaterialTheme.colorScheme.retroOutline
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No se encontraron productos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onTertiary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (hasError) {
                        "No se pudo actualizar el catálogo. Revisa tu conexión e inténtalo de nuevo."
                    } else {
                        "Intenta cambiando los términos de búsqueda o filtros."
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                if (hasError) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Reintentar", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
