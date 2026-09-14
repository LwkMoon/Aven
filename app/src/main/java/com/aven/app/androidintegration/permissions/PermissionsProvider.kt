package com.aven.app.androidintegration.permissions

import com.aven.app.data.preferences.AvenPreferences
import kotlinx.coroutines.flow.Flow

/**
 * PermissionsProvider interface:
 *
 * Seam for future native Android permission checks (Usage Access, Accessibility, POST_NOTIFICATIONS).
 * In this prototype, permissions can be simulated as granted or denied from Settings to test fallbacks.
 */
interface PermissionsProvider {
    val isUsageAccessGranted: Flow<Boolean>
    val isAccessibilityGranted: Flow<Boolean>
    val isNotificationGranted: Flow<Boolean>

    suspend fun setUsageAccessGranted(granted: Boolean)
    suspend fun setAccessibilityGranted(granted: Boolean)
    suspend fun setNotificationGranted(granted: Boolean)
}

class SimulatedPermissionsProvider(
    private val preferences: AvenPreferences
) : PermissionsProvider {
    override val isUsageAccessGranted: Flow<Boolean> = preferences.simulatedUsageAccess
    override val isAccessibilityGranted: Flow<Boolean> = preferences.simulatedAccessibility
    override val isNotificationGranted: Flow<Boolean> = preferences.simulatedNotifications

    override suspend fun setUsageAccessGranted(granted: Boolean) {
        preferences.setSimulatedUsageAccess(granted)
    }

    override suspend fun setAccessibilityGranted(granted: Boolean) {
        preferences.setSimulatedAccessibility(granted)
    }

    override suspend fun setNotificationGranted(granted: Boolean) {
        preferences.setSimulatedNotifications(granted)
    }
}
