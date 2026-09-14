package com.aven.app.androidintegration.appdetection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * AppLaunchRequest encapsulates an intercepted or simulated app launch.
 */
data class AppLaunchRequest(
    val packageName: String,
    val appDisplayName: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AppLaunchObserver interface:
 *
 * Seam for future native Android implementation (AccessibilityService or UsageStatsManager).
 * The UI layer only observes this interface, ensuring zero coupling with platform-specific code.
 */
interface AppLaunchObserver {
    val launchRequests: Flow<AppLaunchRequest>

    /**
     * Manually simulates an app launch (used in this prototype environment).
     */
    suspend fun triggerLaunch(packageName: String, appDisplayName: String)
}

/**
 * Prototype implementation of AppLaunchObserver.
 */
class SimulatedAppLaunchDetector : AppLaunchObserver {
    private val _launchRequests = MutableSharedFlow<AppLaunchRequest>(extraBufferCapacity = 16)
    override val launchRequests: Flow<AppLaunchRequest> = _launchRequests.asSharedFlow()

    override suspend fun triggerLaunch(packageName: String, appDisplayName: String) {
        _launchRequests.emit(
            AppLaunchRequest(
                packageName = packageName,
                appDisplayName = appDisplayName,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    companion object {
        val instance = SimulatedAppLaunchDetector()
    }
}
