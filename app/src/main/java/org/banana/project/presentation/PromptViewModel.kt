package org.banana.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.banana.project.model.PromptQuery
import org.banana.project.services.Interfaces.ILlmService
import javax.inject.Inject

@HiltViewModel
class PromptViewModel @Inject constructor(private val llmService: ILlmService) : ViewModel() {

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response

    fun getLlmResponse(prompt: String) {
        viewModelScope.launch {
            val result = llmService.getLlmResponse(PromptQuery(prompt, ""))
            _response.value = result.response
        }
    }
}