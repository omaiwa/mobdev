package io.github.mobdev.ui.chats

import io.github.mobdev.domain.ChatChannel

data class ChatListUiState(
    val channels: List<ChatChannel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedChannelName: String? = null,
)
