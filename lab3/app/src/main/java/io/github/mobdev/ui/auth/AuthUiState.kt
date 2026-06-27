package io.github.mobdev.ui.auth

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessageRes: Int? = null,
    val isLoggedIn: Boolean = false,
)
