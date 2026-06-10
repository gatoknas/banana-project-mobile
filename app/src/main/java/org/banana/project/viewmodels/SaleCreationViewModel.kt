package org.banana.project.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.banana.project.domain.usecase.ParseAndMatchSpeechUseCase
import org.banana.project.model.ParsedSaleItem
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import org.banana.project.services.SaleService
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SaleCreationViewModel @Inject constructor(
    private val saleService: SaleService,
    private val parseAndMatchSpeechUseCase: ParseAndMatchSpeechUseCase
) : ViewModel() {

    private val _parsedItems = MutableStateFlow<List<ParsedSaleItem>>(emptyList())
    val parsedItems: StateFlow<List<ParsedSaleItem>> = _parsedItems.asStateFlow()

    private val _mergedItemKeys = MutableStateFlow<Set<String>>(emptySet())
    val mergedItemKeys: StateFlow<Set<String>> = _mergedItemKeys.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitResult = MutableStateFlow<SubmitResult?>(null)
    val submitResult: StateFlow<SubmitResult?> = _submitResult.asStateFlow()

    sealed class SubmitResult {
        data class Success(val saleId: Long) : SubmitResult()
        data class Error(val message: String) : SubmitResult()
    }

    sealed class SaleCreationEvent {
        data class ParseSpeech(val text: String) : SaleCreationEvent()
        data class RemoveItem(val item: ParsedSaleItem) : SaleCreationEvent()
        data class UpdateItemQuantity(val item: ParsedSaleItem, val newQuantity: Int) : SaleCreationEvent()
        object SubmitSale : SaleCreationEvent()
        object ClearSubmitResult : SaleCreationEvent()
        object ClearItems : SaleCreationEvent()
        object ClearMergedKeys : SaleCreationEvent()
    }

    /**
     * Returns true if any item in the list has no matched product.
     */
    val hasUnmatchedItems: Boolean
        get() = _parsedItems.value.any { it.matchedProduct == null }

    /**
     * Returns the names of unmatched items for display in the UI.
     */
    val unmatchedItemNames: List<String>
        get() = _parsedItems.value
            .filter { it.matchedProduct == null }
            .map { it.parsedName }

    /**
     * Single entry point to handle user and UI events.
     */
    fun onEvent(event: SaleCreationEvent) {
        when (event) {
            is SaleCreationEvent.ParseSpeech -> parseSpeechInput(event.text)
            is SaleCreationEvent.RemoveItem -> removeItem(event.item)
            is SaleCreationEvent.UpdateItemQuantity -> updateItemQuantity(event.item, event.newQuantity)
            is SaleCreationEvent.SubmitSale -> submitSale()
            is SaleCreationEvent.ClearSubmitResult -> clearSubmitResult()
            is SaleCreationEvent.ClearItems -> clearItems()
            is SaleCreationEvent.ClearMergedKeys -> clearMergedKeys()
        }
    }

    private fun parseSpeechInput(text: String) {
        viewModelScope.launch {
            val matchedItems = parseAndMatchSpeechUseCase(text)
            val (merged, mergedKeys) = mergeItems(_parsedItems.value, matchedItems)
            _parsedItems.value = merged
            _mergedItemKeys.value = mergedKeys
        }
    }
    
    private fun removeItem(item: ParsedSaleItem) {
        _parsedItems.value = _parsedItems.value.filter { it != item }
    }
    
    private fun updateItemQuantity(item: ParsedSaleItem, newQuantity: Int) {
        val clampedQuantity = newQuantity.coerceAtLeast(1)
        _parsedItems.value = _parsedItems.value.map {
            if (it == item) it.copy(quantity = clampedQuantity) else it
        }
    }

    private fun submitSale() {
        val currentItems = _parsedItems.value
        if (currentItems.isEmpty()) return

        if (currentItems.any { it.matchedProduct == null }) {
            _submitResult.value = SubmitResult.Error(
                "Hay productos sin identificar en la lista. Elimínalos antes de registrar la venta."
            )
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true

            val saleItems = currentItems.map { parsed ->
                SaleItem(
                    productId = parsed.matchedProduct!!.id,
                    quantity = parsed.quantity,
                    unitPrice = parsed.matchedProduct.sellPrice
                )
            }

            val totalAmount = currentItems.sumOf { parsed ->
                (parsed.matchedProduct?.sellPrice ?: 0.0) * parsed.quantity
            }

            val sale = Sale(
                id = 0,
                items = emptyList(),
                totalAmount = totalAmount,
                dateTime = Instant.now()
            )

            val result = saleService.createSale(sale, saleItems)

            result.fold(
                onSuccess = { saleId ->
                    _submitResult.value = SubmitResult.Success(saleId)
                    _parsedItems.value = emptyList()
                    _mergedItemKeys.value = emptySet()
                },
                onFailure = { error ->
                    _submitResult.value = SubmitResult.Error(
                        error.localizedMessage ?: "Error al registrar la venta"
                    )
                }
            )

            _isSubmitting.value = false
        }
    }

    private fun clearSubmitResult() {
        _submitResult.value = null
    }
    
    private fun clearItems() {
        _parsedItems.value = emptyList()
    }

    private fun clearMergedKeys() {
        _mergedItemKeys.value = emptySet()
    }

    private fun itemKey(item: ParsedSaleItem): String {
        return if (item.matchedProduct != null) {
            "product_${item.matchedProduct.id}"
        } else {
            "name_${item.parsedName.lowercase()}"
        }
    }

    private fun mergeItems(
        existing: List<ParsedSaleItem>,
        incoming: List<ParsedSaleItem>
    ): Pair<List<ParsedSaleItem>, Set<String>> {
        val merged = existing.toMutableList()
        val mergedKeys = mutableSetOf<String>()

        for (newItem in incoming) {
            val newKey = itemKey(newItem)
            val existingIndex = merged.indexOfFirst { itemKey(it) == newKey }

            if (existingIndex != -1) {
                val current = merged[existingIndex]
                merged[existingIndex] = current.copy(
                    quantity = current.quantity + newItem.quantity
                )
                mergedKeys.add(newKey)
            } else {
                merged.add(newItem)
            }
        }

        return merged to mergedKeys
    }
}
