package io.github.mobdev.domain

data class ChatChannel(
    val name: String,
)

sealed interface ChatMessage {
    val id: String
    val from: String
    val time: String?

    data class Text(
        override val id: String,
        override val from: String,
        override val time: String?,
        val text: String,
    ) : ChatMessage

    data class Image(
        override val id: String,
        override val from: String,
        override val time: String?,
        val imagePath: String,
        val thumbUrl: String,
        val fullUrl: String,
    ) : ChatMessage
}
