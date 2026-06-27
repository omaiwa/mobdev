package io.github.mobdev.data.local

import kotlinx.coroutines.flow.Flow

data class StoredCredentials(
    val username: String,
    val password: String,
)

interface CredentialStorage {
    val credentials: Flow<StoredCredentials?>
    suspend fun save(username: String, password: String)
    suspend fun clear()
}
