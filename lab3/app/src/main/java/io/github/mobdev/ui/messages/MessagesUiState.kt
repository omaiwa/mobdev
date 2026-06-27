package io.github.mobdev.ui.messages

import io.github.mobdev.domain.ChatMessage

data class MessagesUiState(
    val channelName: String = "",
    val username: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val openImagePath: String? = null,
    val errorMessage: String? = null,
)
