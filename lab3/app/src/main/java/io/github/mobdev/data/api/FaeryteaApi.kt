package io.github.mobdev.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FaeryteaApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): String

    @POST("logout")
    suspend fun logout()

    @GET("channels")
    suspend fun getChannels(): List<String>

    @GET("channel/{channelName}")
    suspend fun getChannelMessages(
        @Path(value = "channelName", encoded = true) channelName: String,
        @Query("limit") limit: Int = 20,
        @Query("lastKnownId") lastKnownId: Long = 0,
        @Query("reverse") reverse: Boolean = false,
    ): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(@Body message: MessageDto): String


}
