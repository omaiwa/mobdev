package io.github.mobdev.data.repo

import io.github.mobdev.data.api.FaeryteaApi
import io.github.mobdev.data.api.LoginRequest
import io.github.mobdev.data.api.MessageDataDto
import io.github.mobdev.data.api.MessageDto
import io.github.mobdev.data.api.NetworkModule
import io.github.mobdev.data.api.TextPayloadDto
import io.github.mobdev.data.auth.TokenHolder
import io.github.mobdev.data.local.CredentialStorage
import io.github.mobdev.data.local.StoredCredentials
import io.github.mobdev.domain.ChatChannel
import io.github.mobdev.domain.ChatMessage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class ChatRepository(
    private val api: FaeryteaApi,
    private val tokenHolder: TokenHolder,
    private val credentialStore: CredentialStorage,
) {
    val savedCredentials: Flow<StoredCredentials?> = credentialStore.credentials

    suspend fun login(username: String, password: String): Result<Unit> = try {
        val token = api.login(LoginRequest(name = username, pwd = password))
        tokenHolder.setToken(token.trim())
        credentialStore.save(username, password)
        Result.success(Unit)
    } catch (exception: HttpException) {
        if (exception.code() == 401) {
            Result.failure(LoginException())
        } else {
            Result.failure(exception)
        }
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    suspend fun logout() {
        runCatching { api.logout() }
        tokenHolder.clearToken()
    }

    suspend fun getChannels(): Result<List<ChatChannel>> = runCatching {
        api.getChannels().map { ChatChannel(name = it) }
    }

    suspend fun getMessages(
        channelName: String,
        lastKnownId: Long = 0,
        reverse: Boolean = false,
    ): Result<List<ChatMessage>> = runCatching {
        api.getChannelMessages(
            channelName = encodeChannel(channelName),
            limit = PAGE_SIZE,
            lastKnownId = lastKnownId,
            reverse = reverse,
        ).mapNotNull { it.toDomainMessage() }
    }

    suspend fun sendTextMessage(
        channelName: String,
        username: String,
        text: String,
    ): Result<String> = runCatching {
        val message = MessageDto(
            from = username,
            to = channelName,
            data = MessageDataDto(Text = TextPayloadDto(text = text)),
        )
        api.sendMessage(message).trim()
    }

    fun thumbUrl(imagePath: String): String =
        "${NetworkModule.BASE_URL}thumb/$imagePath"

    fun fullImageUrl(imagePath: String): String =
        "${NetworkModule.BASE_URL}img/$imagePath"

    private fun encodeChannel(channelName: String): String =
        URLEncoder.encode(channelName, StandardCharsets.UTF_8.toString())

    private fun MessageDto.toDomainMessage(): ChatMessage? {
        val messageId = id ?: return null
        val textPayload = data.Text
        if (textPayload != null) {
            return ChatMessage.Text(
                id = messageId,
                from = from,
                time = time,
                text = textPayload.text,
            )
        }
        val imagePayload = data.Image
        val imagePath = imagePayload?.link ?: return null
        return ChatMessage.Image(
            id = messageId,
            from = from,
            time = time,
            imagePath = imagePath,
            thumbUrl = thumbUrl(imagePath),
            fullUrl = fullImageUrl(imagePath),
        )
    }

    class LoginException : Exception()

    companion object {
        const val PAGE_SIZE = 20
    }
}
