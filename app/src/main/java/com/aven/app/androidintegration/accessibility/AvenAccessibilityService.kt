package com.aven.app.androidintegration.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import com.aven.app.MainActivity
import com.aven.app.androidintegration.appdetection.SimulatedAppLaunchDetector
import com.aven.app.data.local.AvenDatabase
import com.aven.app.data.preferences.AvenPreferences
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * AvenAccessibilityService:
 *
 * Real-time Android Accessibility Service for detecting when monitored apps are opened.
 * Complies with calm, non-punitive awareness design:
 * - Debounces repeated window events to prevent continuous interruption.
 * - Dispatches to Aven's Intervention overlay.
 */
/**
 * NOTE FOR ANDROID STUDIO / PLAY STORE PHASE:
 * Accessibility Service usage is one of the most heavily reviewed permission categories
 * in Google Play Store submission. The declared purpose and description string in
 * accessibility_service_config.xml and strings.xml must precisely describe that this
 * service is used exclusively for on-device app launch detection for intentional habit
 * awareness, without collecting or transmitting any personal data or screen content.
 */
class AvenAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lastInterventionTimes = ConcurrentHashMap<String, Long>()
    private var repository: AvenRepository? = null

    // System packages and top OEM home launchers to ignore so interventions never fire on the home screen
    private val ignoredPackages = setOf(
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher", // Pixel Launcher
        "com.android.launcher",                 // AOSP Launcher
        "com.android.launcher3",                // Launcher3
        "com.sec.android.app.launcher",         // Samsung One UI Home
        "com.miui.home",                        // Xiaomi / HyperOS Launcher
        "com.oppo.launcher",                    // OPPO ColorOS Launcher
        "com.oneplus.launcher",                 // OnePlus OxygenOS Launcher
        "com.huawei.android.launcher",          // Huawei / Honor EMUI
        "com.teslacoilsw.launcher",             // Nova Launcher
        "com.android.settings",                 // System Settings
        "com.google.android.packageinstaller",  // Package Installer
        "com.android.packageinstaller"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = AvenRepository.getInstance(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        // Never fire while Aven itself is in the foreground, nor on system launchers / system UI
        if (packageName == this.packageName || packageName == applicationContext.packageName || packageName in ignoredPackages) return

        val now = System.currentTimeMillis()
        val lastIntervention = lastInterventionTimes[packageName] ?: 0L

        // CRITICAL ARCHITECTURAL DESIGN NOTE:
        // 60-second cooldown per monitored package prevents repeated interventions during
        // normal within-app navigation (e.g., opening nested profiles, tabs, or system sheets
        // in the target app). DO NOT REMOVE or bypass this debounce window; without it,
        // TYPE_WINDOW_STATE_CHANGED events would repeatedly trigger pause overlays mid-session.
        if (now - lastIntervention < 60_000L) return

        serviceScope.launch {
            val repo = repository ?: return@launch
            val monitoredApps = repo.allMonitoredApps.first()
            if (monitoredApps.isEmpty()) return@launch // Do nothing if the user has zero monitored apps

            val monitored = monitoredApps.find { it.packageName == packageName && it.enabled }

            if (monitored != null) {
                lastInterventionTimes[packageName] = now

                val appDisplayName = monitored.displayName.ifEmpty {
                    try {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(packageName, 0)
                        ).toString()
                    } catch (e: Exception) {
                        packageName
                    }
                }

                // Notify both internal flow and launch MainActivity overlay
                SimulatedAppLaunchDetector.instance.triggerLaunch(packageName, appDisplayName)

                val intent = Intent(this@AvenAccessibilityService, MainActivity::class.java).apply {
                    action = ACTION_INTERVENTION
                    putExtra(EXTRA_PACKAGE_NAME, packageName)
                    putExtra(EXTRA_APP_NAME, appDisplayName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility interrupted
    }

    companion object {
        const val ACTION_INTERVENTION = "com.aven.app.aven.ACTION_INTERVENTION"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"

        fun recordExternalAllowedLaunch(packageName: String) {
            // Allows the user to freely use the app after deciding to continue
        }
    }
}
