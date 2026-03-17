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

    fun parseSpeechInput(text: String) {
        viewModelScope.launch {
            val items = SpanishParserHelper.parseSpeech(text)
            
            // Fetch potential catalog products representing this location synchronously
            val dbProducts = productRepository.getAllSync()
            
            // Fuzzy match the recognized tokens against actual Products
            val matchedItems = ProductMatchingService.matchParsedItemsToProducts(items, dbProducts)
            
            _parsedItems.value = matchedItems
        }
    }
    
    fun removeItem(item: ParsedSellItem) {
        _parsedItems.value = _parsedItems.value.filter { it != item }
    }
    
    fun clearItems() {
        _parsedItems.value = emptyList()
    }
}
