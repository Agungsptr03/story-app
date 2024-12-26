package com.dicoding.storyapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserConfig private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveUserSession(user: User) {
        dataStore.edit { prefs ->
            prefs[USER_EMAIL_KEY] = user.email
            prefs[USER_TOKEN_KEY] = user.token
            prefs[USER_IS_LOGGED_IN_KEY] = true
        }
    }

    fun getUserSession(): Flow<User> {
        return dataStore.data.map { prefs ->
            User(
                prefs[USER_EMAIL_KEY] ?: "",
                prefs[USER_TOKEN_KEY] ?: "",
                prefs[USER_IS_LOGGED_IN_KEY] ?: false
            )
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        @Volatile
        private var instance: UserConfig? = null

        private val USER_EMAIL_KEY = stringPreferencesKey("email")
        private val USER_TOKEN_KEY = stringPreferencesKey("token")
        private val USER_IS_LOGGED_IN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): UserConfig {
            return instance ?: synchronized(this) {
                instance ?: UserConfig(dataStore).also { instance = it }
            }
        }
    }
}
