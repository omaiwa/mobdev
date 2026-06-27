package io.github.mobdev.ui.messages

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import io.github.mobdev.data.repo.ChatRepository
import io.github.mobdev.domain.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val repository: ChatRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var initialLoaded = false

    init {
        val channelName = savedStateHandle.get<String>(KEY_CHANNEL).orEmpty()
        val username = savedStateHandle.get<String>(KEY_USERNAME).orEmpty()
        val imagePath = savedStateHandle.get<String>(KEY_OPEN_IMAGE)
        _uiState.value = MessagesUiState(
            channelName = channelName,
            username = username,
            openImagePath = imagePath,
        )
    }

    fun bind(channelName: String, username: String) {
        val current = _uiState.value
        if (current.channelName == channelName && current.username == username) {
            return
        }
        if (current.channelName != channelName) {
            initialLoaded = false
            _uiState.value = MessagesUiState(
                channelName = channelName,
                username = username,
                openImagePath = savedStateHandle.get<String>(KEY_OPEN_IMAGE),
            )
        } else {
            _uiState.update { it.copy(username = username) }
        }
        savedStateHandle[KEY_CHANNEL] = channelName
        savedStateHandle[KEY_USERNAME] = username
    }

    fun loadInitialIfNeeded() {
        if (initialLoaded) return
        initialLoaded = true
        loadInitialMessages()
    }

    private fun loadInitialMessages() {
        val channelName = _uiState.value.channelName
        if (channelName.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getMessages(channelName)
            _uiState.update { current ->
                if (result.isSuccess) {
                    val messages = result.getOrDefault(emptyList())
                    current.copy(
                        messages = messages,
                        isLoading = false,
                        canLoadMore = messages.size >= ChatRepository.PAGE_SIZE,
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

    fun loadOlderMessages() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.canLoadMore || state.messages.isEmpty()) return
        val oldestId = state.messages.first().id.toLongOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val result = repository.getMessages(
                channelName = state.channelName,
                lastKnownId = oldestId,
                reverse = true,
            )
            _uiState.update { current ->
                if (result.isSuccess) {
                    val older = result.getOrDefault(emptyList())
                    val merged = (older + current.messages).distinctBy { it.id }
                    current.copy(
                        messages = merged,
                        isLoadingMore = false,
                        canLoadMore = older.size >= ChatRepository.PAGE_SIZE,
                    )
                } else {
                    current.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        val username = state.username
        if (text.isEmpty() || username.isBlank()) return
        viewModelScope.launch {
            val result = repository.sendTextMessage(
                channelName = state.channelName,
                username = username,
                text = text,
            )
            if (result.isSuccess) {
                val messageId = result.getOrDefault("")
                val sentMessage = ChatMessage.Text(
                    id = messageId,
                    from = username,
                    time = null,
                    text = text,
                )
                _uiState.update { current ->
                    val updatedMessages = if (current.messages.any { it.id == messageId }) {
                        current.messages
                    } else {
                        current.messages + sentMessage
                    }
                    current.copy(inputText = "", messages = updatedMessages)
                }
                appendNewMessages()
            }
        }
    }

    private suspend fun appendNewMessages() {
        val state = _uiState.value
        if (state.channelName.isBlank()) return
        val lastKnownId = state.messages.maxOfOrNull { it.id.toLongOrNull() ?: 0L } ?: 0L
        val result = repository.getMessages(
            channelName = state.channelName,
            lastKnownId = lastKnownId,
            reverse = false,
        )
        if (result.isSuccess) {
            val newer = result.getOrDefault(emptyList())
            if (newer.isNotEmpty()) {
                _uiState.update { current ->
                    current.copy(
                        messages = (current.messages + newer).distinctBy { it.id },
                    )
                }
            }
        }
    }

    fun openImage(imagePath: String) {
        savedStateHandle[KEY_OPEN_IMAGE] = imagePath
        _uiState.update { it.copy(openImagePath = imagePath) }
    }

    fun closeImage() {
        savedStateHandle.remove<String>(KEY_OPEN_IMAGE)
        _uiState.update { it.copy(openImagePath = null) }
    }

    fun clear() {
        initialLoaded = false
        savedStateHandle.remove<String>(KEY_CHANNEL)
        savedStateHandle.remove<String>(KEY_USERNAME)
        savedStateHandle.remove<String>(KEY_OPEN_IMAGE)
        _uiState.value = MessagesUiState()
    }

    fun fullImageUrl(): String? {
        val path = _uiState.value.openImagePath ?: return null
        return repository.fullImageUrl(path)
    }

    class Factory(
        owner: SavedStateRegistryOwner,
        private val repository: ChatRepository,
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle,
        ): T {
            if (modelClass.isAssignableFrom(MessagesViewModel::class.java)) {
                return MessagesViewModel(repository, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        const val KEY_CHANNEL = "channel_name"
        const val KEY_USERNAME = "username"
        const val KEY_OPEN_IMAGE = "open_image_path"
    }
}
