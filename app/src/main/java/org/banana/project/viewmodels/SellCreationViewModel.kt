package org.banana.project.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.ParsedSellItem
import org.banana.project.model.Sell
import org.banana.project.model.SellItem
import org.banana.project.services.SellService
import org.banana.project.utils.ParsedItem
import org.banana.project.utils.ProductMatchingService
import org.banana.project.utils.SpanishParserHelper
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SellCreationViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val sellService: SellService
) : ViewModel() {

    private val _parsedItems = MutableStateFlow<List<ParsedSellItem>>(emptyList())
    val parsedItems: StateFlow<List<ParsedSellItem>> = _parsedItems.asStateFlow()

    private val _mergedItemKeys = MutableStateFlow<Set<String>>(emptySet())
    val mergedItemKeys: StateFlow<Set<String>> = _mergedItemKeys.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitResult = MutableStateFlow<SubmitResult?>(null)
    val submitResult: StateFlow<SubmitResult?> = _submitResult.asStateFlow()

    sealed class SubmitResult {
        data class Success(val sellId: Long) : SubmitResult()
        data class Error(val message: String) : SubmitResult()
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

    fun parseSpeechInput(text: String) {
        viewModelScope.launch {
            val items = SpanishParserHelper.parseSpeech(text)
            
            // Fetch potential catalog products representing this location synchronously
            val dbProducts = productRepository.getAllSync()
            
            // Fuzzy match the recognized tokens against actual Products
            val matchedItems = ProductMatchingService.matchParsedItemsToProducts(items, dbProducts)
            
            val (merged, mergedKeys) = mergeItems(_parsedItems.value, matchedItems)
            _parsedItems.value = merged
            _mergedItemKeys.value = mergedKeys
        }
    }
    
    fun removeItem(item: ParsedSellItem) {
        _parsedItems.value = _parsedItems.value.filter { it != item }
    }
    
    fun updateItemQuantity(item: ParsedSellItem, newQuantity: Int) {
        val clampedQuantity = newQuantity.coerceAtLeast(1)
        _parsedItems.value = _parsedItems.value.map {
            if (it == item) it.copy(quantity = clampedQuantity) else it
        }
    }

    /**
     * Submits the current sell to the database.
     * Only matched items are included. Blocks if any unmatched items exist (Option B).
     */
    fun submitSell() {
        val currentItems = _parsedItems.value
        if (currentItems.isEmpty()) return

        // Option B: block if unmatched items exist
        if (currentItems.any { it.matchedProduct == null }) {
            _submitResult.value = SubmitResult.Error(
                "Hay productos sin identificar en la lista. Elimínalos antes de registrar la venta."
            )
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true

            val sellItems = currentItems.map { parsed ->
                SellItem(
                    productId = parsed.matchedProduct!!.id,
                    quantity = parsed.quantity,
                    unitPrice = parsed.matchedProduct.sellPrice
                )
            }

            val totalAmount = currentItems.sumOf { parsed ->
                (parsed.matchedProduct?.sellPrice ?: 0.0) * parsed.quantity
            }

            val sell = Sell(
                id = 0,
                items = emptyList(),
                totalAmount = totalAmount,
                dateTime = Instant.now()
            )

            val result = sellService.createSell(sell, sellItems)

            result.fold(
                onSuccess = { sellId ->
                    _submitResult.value = SubmitResult.Success(sellId)
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

    fun clearSubmitResult() {
        _submitResult.value = null
    }
    
    fun clearItems() {
        _parsedItems.value = emptyList()
    }

    fun clearMergedKeys() {
        _mergedItemKeys.value = emptySet()
    }

    /**
     * Returns a unique string key for a ParsedSellItem, used to
     * identify duplicates during merge.
     */
    private fun itemKey(item: ParsedSellItem): String {
        return if (item.matchedProduct != null) {
            "product_${item.matchedProduct.id}"
        } else {
            "name_${item.parsedName.lowercase()}"
        }
    }

    /**
     * Merges incoming items into the existing list.
     * - If a product already exists (same Product ID or same parsedName
     *   for unmatched items), its quantity is summed.
     * - New products are appended at the end.
     *
     * Returns the merged list AND the set of keys that were merged
     * (so the UI can highlight them).
     */
    private fun mergeItems(
        existing: List<ParsedSellItem>,
        incoming: List<ParsedSellItem>
    ): Pair<List<ParsedSellItem>, Set<String>> {
        val merged = existing.toMutableList()
        val mergedKeys = mutableSetOf<String>()

        for (newItem in incoming) {
            val newKey = itemKey(newItem)
            val existingIndex = merged.indexOfFirst { itemKey(it) == newKey }

            if (existingIndex != -1) {
                // Merge: sum quantities
                val current = merged[existingIndex]
                merged[existingIndex] = current.copy(
                    quantity = current.quantity + newItem.quantity
                )
                mergedKeys.add(newKey)
            } else {
                // Append new item
                merged.add(newItem)
            }
        }

        return merged to mergedKeys
    }
}

