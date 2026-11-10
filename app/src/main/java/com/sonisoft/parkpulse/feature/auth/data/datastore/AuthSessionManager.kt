package com.sonisoft.parkpulse.feature.auth.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sonisoft.parkpulse.feature.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ID = stringPreferencesKey("user_id")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val authUser: Flow<AuthUser?> = dataStore.data.map { preferences ->
        val id = preferences[USER_ID]
        val name = preferences[USER_NAME]
        val email = preferences[USER_EMAIL]
        if (id != null && name != null && email != null) {
            AuthUser(id, name, email)
        } else {
            null
        }
    }

    suspend fun saveSession(user: AuthUser) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
