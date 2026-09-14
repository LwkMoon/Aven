package com.aven.app.androidintegration.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.aven.app.MainActivity
import com.aven.app.androidintegration.appdetection.AppLaunchEventBus
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/** Detects launches of monitored apps and starts Aven's intervention flow. */
class AvenAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lastInterventionTimes = ConcurrentHashMap<String, Long>()
    private var repository: AvenRepository? = null

    private val ignoredPackages = setOf(
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher",
        "com.android.launcher3",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.oneplus.launcher",
        "com.huawei.android.launcher",
        "com.teslacoilsw.launcher",
        "com.android.settings",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AvenRepository.getInstance(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName in ignoredPackages) return

        val now = System.currentTimeMillis()
        val lastIntervention = lastInterventionTimes[packageName] ?: 0L
        if (now - lastIntervention < 60_000L) return

        serviceScope.launch {
            val repo = repository ?: return@launch
            val monitored = repo.allMonitoredApps.first()
                .find { it.packageName == packageName && it.enabled }
                ?: return@launch

            lastInterventionTimes[packageName] = now

            val appDisplayName = monitored.displayName.ifEmpty {
                runCatching {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                }.getOrDefault(packageName)
            }

            AppLaunchEventBus.instance.publishLaunch(packageName, appDisplayName)

            val intent = Intent(this@AvenAccessibilityService, MainActivity::class.java).apply {
                action = ACTION_INTERVENTION
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_APP_NAME, appDisplayName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() = Unit

    companion object {
        const val ACTION_INTERVENTION = "com.aven.app.aven.ACTION_INTERVENTION"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"

        fun recordExternalAllowedLaunch(packageName: String) = Unit
    }
}
