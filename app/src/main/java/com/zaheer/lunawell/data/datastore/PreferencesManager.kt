package com.zaheer.lunawell.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lunawell_preferences")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_PRO_ACTIVE = booleanPreferencesKey("is_pro_active")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val LAST_ACTIVE_PROFILE_ID = longPreferencesKey("last_active_profile_id")
        val NOTIFICATION_PRIVACY_ENABLED = booleanPreferencesKey("notification_privacy_enabled")
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    fun getOnboardingCompleted(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun saveIsProActive(isProActive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PRO_ACTIVE] = isProActive
        }
    }

    fun getIsProActive(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_PRO_ACTIVE] ?: false
        }
    }

    suspend fun saveAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }

    fun getAppLockEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false
        }
    }

    suspend fun saveAppLockPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_PIN] = pin
        }
    }

    fun getAppLockPin(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.APP_LOCK_PIN]
        }
    }

    suspend fun saveLastActiveProfileId(profileId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVE_PROFILE_ID] = profileId
        }
    }

    fun getLastActiveProfileId(): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVE_PROFILE_ID]
        }
    }

    suspend fun saveNotificationPrivacyEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_PRIVACY_ENABLED] = enabled
        }
    }

    fun getNotificationPrivacyEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_PRIVACY_ENABLED] ?: false
        }
    }
}
