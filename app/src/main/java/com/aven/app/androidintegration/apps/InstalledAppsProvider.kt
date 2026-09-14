package com.aven.app.androidintegration.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DiscoveredApp(
    val packageName: String,
    val displayName: String,
    val iconDrawable: Drawable? = null,
    val isMonitored: Boolean = false
)

class InstalledAppsProvider(private val context: Context) {

    suspend fun getInstalledLaunchableApps(): List<DiscoveredApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val selfPackage = context.packageName

        resolveInfos
            .filter { it.activityInfo != null && it.activityInfo.packageName != selfPackage }
            .map { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                val label = try {
                    resolveInfo.loadLabel(pm).toString()
                } catch (e: Exception) {
                    pkgName
                }
                val icon = try {
                    resolveInfo.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }
                DiscoveredApp(
                    packageName = pkgName,
                    displayName = label,
                    iconDrawable = icon
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
    }
}
