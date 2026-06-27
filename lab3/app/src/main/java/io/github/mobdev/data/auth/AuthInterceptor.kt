package io.github.mobdev.data.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenHolder: TokenHolder,
    private val unauthorizedEventBus: UnauthorizedEventBus,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        tokenHolder.getToken()?.let { token ->
            requestBuilder.header("X-Auth-Token", token)
        }
        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401) {
            tokenHolder.clearToken()
            unauthorizedEventBus.emitUnauthorized()
        }
        return response
    }
}
