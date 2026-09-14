package com.aven.app.androidintegration.appdetection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Represents an app launch detected by Aven. */
data class AppLaunchRequest(
    val packageName: String,
    val appDisplayName: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** Publishes app launches without coupling the UI to Android platform APIs. */
interface AppLaunchObserver {
    val launchRequests: Flow<AppLaunchRequest>
    suspend fun publishLaunch(packageName: String, appDisplayName: String)
}

class AppLaunchEventBus : AppLaunchObserver {
    private val _launchRequests = MutableSharedFlow<AppLaunchRequest>(extraBufferCapacity = 16)
    override val launchRequests: Flow<AppLaunchRequest> = _launchRequests.asSharedFlow()

    override suspend fun publishLaunch(packageName: String, appDisplayName: String) {
        _launchRequests.emit(
            AppLaunchRequest(
                packageName = packageName,
                appDisplayName = appDisplayName
            )
        )
    }

    companion object {
        val instance = AppLaunchEventBus()
    }
}
