package org.banana.project.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.banana.project.data.repository.AuthRepository
import javax.inject.Inject

sealed class LoginEvent {
    data class UsernameChanged(val username: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data class Submit(val onSuccess: () -> Unit) : LoginEvent()
    object ClearError : LoginEvent()
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> {
                _state.update { it.copy(username = event.username, errorMessage = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, errorMessage = null) }
            }
            is LoginEvent.Submit -> submit(event.onSuccess)
            LoginEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun submit(onSuccess: () -> Unit) {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "El usuario y la contraseña son requeridos.") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(currentState.username, currentState.password)
            
            _state.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(errorMessage = e.message ?: "El usuario o la contraseña son incorrectos.") }
            }
        }
    }
}
