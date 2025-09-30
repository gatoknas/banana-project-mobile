package org.banana.project.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.banana.project.services.Interfaces.ISpeechParser
import javax.inject.Inject

@HiltViewModel
class ParserTestViewModel @Inject constructor(private val speechParser: ISpeechParser) : ViewModel() {

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result

    fun processInput(input: String) {
        val response = speechParser.GetProductsAndQuantities(input)
        _result.value = response.response
    }
}