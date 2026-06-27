package io.github.mobdev.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TokenHolder {
    private var token: String? = null

    fun getToken(): String? = token

    fun setToken(value: String) {
        token = value
    }

    fun clearToken() {
        token = null
    }
}

class UnauthorizedEventBus {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun emitUnauthorized() {
        _events.tryEmit(Unit)
    }
}
