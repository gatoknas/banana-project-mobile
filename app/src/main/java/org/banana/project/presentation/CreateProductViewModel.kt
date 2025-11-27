package org.banana.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.banana.project.model.Product
import org.banana.project.services.ProductService
import javax.inject.Inject

/**
 * ViewModel for the Create Product screen.
 * Handles form state and product creation logic.
 */
@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val productService: ProductService
) : ViewModel() {

    // Form state
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _sellPrice = MutableStateFlow("")
    val sellPrice: StateFlow<String> = _sellPrice

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // Form actions
    fun updateName(name: String) {
        _name.value = name
        clearMessages()
    }

    fun updateDescription(description: String) {
        _description.value = description
        clearMessages()
    }

    fun updateSellPrice(sellPrice: String) {
        _sellPrice.value = sellPrice
        clearMessages()
    }

    private fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Validate form inputs.
     */
    private fun validateForm(): Boolean {
        if (_name.value.isBlank()) {
            _errorMessage.value = "Product name cannot be blank"
            return false
        }

        val sellPriceValue = _sellPrice.value.toDoubleOrNull()

        if (sellPriceValue == null || sellPriceValue < 0) {
            _errorMessage.value = "Sell price must be a valid positive number"
            return false
        }

        return true
    }

    /**
     * Create the product using the service.
     */
    fun createProduct(onSuccess: () -> Unit) {
        if (!validateForm()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val product = Product(
                    id = 0L, // Will be auto-generated
                    name = _name.value.trim(),
                    description = _description.value.trim(),
                    sellPrice = _sellPrice.value.toDouble(),
                    createdAt = java.time.Instant.now(),
                    updatedAt = java.time.Instant.now()
                )

                val result = productService.addProduct(product)
                result.onSuccess { productId ->
                    _successMessage.value = "Product created successfully!"
                    // Clear form
                    _name.value = ""
                    _description.value = ""
                    _sellPrice.value = ""
                    onSuccess()
                }.onFailure { exception ->
                    _errorMessage.value = "Failed to create product: ${exception.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}