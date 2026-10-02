package org.banana.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.banana.project.data.CatalogSyncer
import org.banana.project.model.Product
import org.banana.project.services.ProductService
import javax.inject.Inject

/**
 * How the read-only product catalog is rendered.
 */
enum class CatalogViewMode { GRID, LIST }

/**
 * Immutable UI state for the product catalog screen.
 */
data class ProductCatalogState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val viewMode: CatalogViewMode = CatalogViewMode.GRID,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Events accepted by the product catalog screen.
 */
sealed interface ProductCatalogEvent {
    data class UpdateSearchQuery(val query: String) : ProductCatalogEvent
    data class SelectCategory(val category: String?) : ProductCatalogEvent
    data class SetViewMode(val mode: CatalogViewMode) : ProductCatalogEvent
    data object Retry : ProductCatalogEvent
}

/**
 * ViewModel for the read-only product catalog.
 *
 * Refreshes the local catalog from the API on creation, then observes the local
 * database. Products can be filtered by name and category; rendering is either a
 * grid or a list. There is no create, edit, or delete behavior.
 */
@HiltViewModel
class ProductCatalogViewModel @Inject constructor(
    private val productService: ProductService,
    private val catalogSyncer: CatalogSyncer
) : ViewModel() {

    private val _state = MutableStateFlow(ProductCatalogState())
    val state: StateFlow<ProductCatalogState> = _state.asStateFlow()

    /** Products after applying the current name search and category filter. */
    val filteredProducts: List<Product>
        get() = filter(_state.value)

    init {
        observeProducts()
        refresh()
    }

    fun onEvent(event: ProductCatalogEvent) {
        when (event) {
            is ProductCatalogEvent.UpdateSearchQuery ->
                _state.update { it.copy(searchQuery = event.query) }

            is ProductCatalogEvent.SelectCategory ->
                _state.update { it.copy(selectedCategory = event.category) }

            is ProductCatalogEvent.SetViewMode ->
                _state.update { it.copy(viewMode = event.mode) }

            ProductCatalogEvent.Retry -> refresh()
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            productService.getAllProducts().collect { products ->
                _state.update { current ->
                    val categories = current.categories.ifEmpty {
                        products.mapNotNull { it.categoryName }.distinct().sorted()
                    }
                    current.copy(products = products, categories = categories)
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            catalogSyncer.sync()
                .onSuccess { snapshot ->
                    _state.update { current ->
                        current.copy(
                            categories = snapshot.categories.ifEmpty { current.categories },
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "No se pudo actualizar el catálogo"
                        )
                    }
                }
        }
    }

    private fun filter(state: ProductCatalogState): List<Product> {
        val query = state.searchQuery.trim().lowercase()
        return state.products.filter { product ->
            val matchesName = query.isEmpty() || product.name.lowercase().contains(query)
            val matchesCategory =
                state.selectedCategory == null || product.category == state.selectedCategory
            matchesName && matchesCategory
        }
    }
}
