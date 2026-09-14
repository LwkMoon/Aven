package com.aven.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.avenDataStore: DataStore<Preferences> by preferencesDataStore(name = "aven_settings")

class AvenPreferences(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_PAUSE_DURATION = intPreferencesKey("pause_duration_seconds")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val KEY_SIM_USAGE_ACCESS = booleanPreferencesKey("sim_usage_access")
        val KEY_SIM_ACCESSIBILITY = booleanPreferencesKey("sim_accessibility")
        val KEY_SIM_NOTIFICATIONS = booleanPreferencesKey("sim_notifications")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val pauseDurationSeconds: Flow<Int> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_PAUSE_DURATION] ?: 3
    }

    val themeMode: Flow<String> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "system"
    }

    val notificationsEnabled: Flow<Boolean> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: false
    }

    val isReducedMotion: Flow<Boolean> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_REDUCED_MOTION] ?: false
    }

    val simulatedUsageAccess: Flow<Boolean> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_SIM_USAGE_ACCESS] ?: true
    }

    val simulatedAccessibility: Flow<Boolean> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_SIM_ACCESSIBILITY] ?: true
    }

    val simulatedNotifications: Flow<Boolean> = context.avenDataStore.data.map { prefs ->
        prefs[KEY_SIM_NOTIFICATIONS] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.avenDataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setPauseDuration(seconds: Int) {
        context.avenDataStore.edit { it[KEY_PAUSE_DURATION] = seconds }
    }

    suspend fun setThemeMode(mode: String) {
        context.avenDataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.avenDataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.avenDataStore.edit { it[KEY_REDUCED_MOTION] = reduced }
    }

    suspend fun setSimulatedUsageAccess(granted: Boolean) {
        context.avenDataStore.edit { it[KEY_SIM_USAGE_ACCESS] = granted }
    }

    suspend fun setSimulatedAccessibility(granted: Boolean) {
        context.avenDataStore.edit { it[KEY_SIM_ACCESSIBILITY] = granted }
    }

    suspend fun setSimulatedNotifications(granted: Boolean) {
        context.avenDataStore.edit { it[KEY_SIM_NOTIFICATIONS] = granted }
    }

    suspend fun clearAll() {
        context.avenDataStore.edit { it.clear() }
    }
}
