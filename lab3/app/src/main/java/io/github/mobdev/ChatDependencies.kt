package io.github.mobdev

import android.content.Context
import io.github.mobdev.data.api.NetworkModule
import io.github.mobdev.data.local.CredentialStore
import io.github.mobdev.data.repo.ChatRepository

object ChatDependencies {
    lateinit var repository: ChatRepository
        private set

    fun init(context: Context) {
        if (::repository.isInitialized) return
        repository = ChatRepository(
            api = NetworkModule.createApi(),
            tokenHolder = NetworkModule.tokenHolder,
            credentialStore = CredentialStore.from(context),
        )
    }
}
