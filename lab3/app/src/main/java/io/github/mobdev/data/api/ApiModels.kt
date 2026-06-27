package io.github.mobdev.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class LoginRequest(
    val name: String,
    val pwd: String,
)

@JsonClass(generateAdapter = false)
data class MessageDto(
    val id: String? = null,
    val from: String,
    @Json(name = "to") val to: String? = null,
    val data: MessageDataDto,
    val time: String? = null,
)

@JsonClass(generateAdapter = false)
data class MessageDataDto(
    val Text: TextPayloadDto? = null,
    val Image: ImagePayloadDto? = null,
)

@JsonClass(generateAdapter = false)
data class TextPayloadDto(
    val text: String,
)

@JsonClass(generateAdapter = false)
data class ImagePayloadDto(
    val link: String? = null,
)
