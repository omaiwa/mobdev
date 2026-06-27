package io.github.mobdev.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.mobdev.data.repo.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val repository: ChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var loaded = false

    fun loadChannelsIfNeeded() {
        if (loaded) return
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getChannels()
            _uiState.update { current ->
                if (result.isSuccess) {
                    loaded = true
                    current.copy(
                        channels = result.getOrDefault(emptyList()),
                        isLoading = false,
                    )
                } else {
                    current.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message,
                    )
                }
            }
        }
    }

    fun selectChannel(channelName: String?) {
        _uiState.update { it.copy(selectedChannelName = channelName) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            loaded = false
            _uiState.value = ChatListUiState()
            onLoggedOut()
        }
    }

    class Factory(
        private val repository: ChatRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
                return ChatListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
