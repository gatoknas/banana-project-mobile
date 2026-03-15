package org.banana.project.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.banana.project.utils.ParsedItem
import org.banana.project.utils.SpanishParserHelper
import javax.inject.Inject

@HiltViewModel
class SellCreationViewModel @Inject constructor() : ViewModel() {

    private val _parsedItems = MutableStateFlow<List<ParsedItem>>(emptyList())
    val parsedItems: StateFlow<List<ParsedItem>> = _parsedItems.asStateFlow()

    fun parseSpeechInput(text: String) {
        val items = SpanishParserHelper.parseSpeech(text)
        _parsedItems.value = items
    }
    
    fun clearItems() {
        _parsedItems.value = emptyList()
    }
}
