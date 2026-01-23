package com.example.loanova_android.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "loanova_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessTokenKey = stringPreferencesKey("access_token")

    val accessToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[accessTokenKey]
        }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = token
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
