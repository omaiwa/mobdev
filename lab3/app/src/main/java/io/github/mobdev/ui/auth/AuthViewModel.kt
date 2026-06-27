package io.github.mobdev.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.mobdev.R
import io.github.mobdev.data.repo.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: ChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var autoLoginAttempted = false

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun dismissError() {
        _uiState.update { it.copy(showErrorDialog = false, errorMessageRes = null) }
    }

    fun tryAutoLogin() {
        if (autoLoginAttempted) return
        autoLoginAttempted = true
        viewModelScope.launch {
            val saved = repository.savedCredentials.first() ?: return@launch
            _uiState.update {
                it.copy(username = saved.username, password = saved.password)
            }
            login(saved.username, saved.password, silent = true)
        }
    }

    fun login() {
        val state = _uiState.value
        login(state.username, state.password, silent = false)
    }

    private fun login(username: String, password: String, silent: Boolean) {
        if (username.isBlank() || password.isBlank()) {
            if (!silent) {
                _uiState.update {
                    it.copy(
                        showErrorDialog = true,
                        errorMessageRes = R.string.login_error,
                    )
                }
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showErrorDialog = false) }
            val result = repository.login(username, password)
            _uiState.update { current ->
                if (result.isSuccess) {
                    current.copy(isLoading = false, isLoggedIn = true)
                } else {
                    current.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        showErrorDialog = !silent,
                        errorMessageRes = if (result.exceptionOrNull() is ChatRepository.LoginException) {
                            R.string.login_error
                        } else {
                            R.string.login_error_generic
                        },
                    )
                }
            }
        }
    }

    fun onLoggedOut() {
        _uiState.update {
            it.copy(isLoggedIn = false, isLoading = false)
        }
    }

    class Factory(
        private val repository: ChatRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
