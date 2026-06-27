package io.github.mobdev.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.credentialDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "chat_credentials",
)

class CredentialStore(
    private val dataStore: DataStore<Preferences>,
) : CredentialStorage {
    override val credentials: Flow<StoredCredentials?> = dataStore.data.map { preferences ->
        val username = preferences[KEY_USERNAME]
        val password = preferences[KEY_PASSWORD]
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            null
        } else {
            StoredCredentials(username = username, password = password)
        }
    }

    override suspend fun save(username: String, password: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
            preferences[KEY_PASSWORD] = password
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_USERNAME)
            preferences.remove(KEY_PASSWORD)
        }
    }

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_PASSWORD = stringPreferencesKey("password")

        fun from(context: Context): CredentialStore =
            CredentialStore(context.applicationContext.credentialDataStore)
    }
}
