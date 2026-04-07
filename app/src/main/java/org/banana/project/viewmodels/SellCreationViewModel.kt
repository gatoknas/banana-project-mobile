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
import org.banana.project.utils.ParsedItem
import org.banana.project.utils.ProductMatchingService
import org.banana.project.utils.SpanishParserHelper
import javax.inject.Inject

@HiltViewModel
class SellCreationViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _parsedItems = MutableStateFlow<List<ParsedSellItem>>(emptyList())
    val parsedItems: StateFlow<List<ParsedSellItem>> = _parsedItems.asStateFlow()

    private val _mergedItemKeys = MutableStateFlow<Set<String>>(emptySet())
    val mergedItemKeys: StateFlow<Set<String>> = _mergedItemKeys.asStateFlow()

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
