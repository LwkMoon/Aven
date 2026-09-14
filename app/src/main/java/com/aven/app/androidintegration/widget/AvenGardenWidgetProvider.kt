package com.aven.app.androidintegration.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aven.app.MainActivity
import com.aven.app.R
import com.aven.app.core.behavior.BehaviorEngine
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ==============================================================================
 * OPTIONAL FEATURE: Aven Living World Home Screen Widget
 * ==============================================================================
 * WHAT THIS DOES:
 * Provides an Android Home Screen Widget displaying the living world's current stage,
 * environmental status, and daily intentionality percentage with a one-tap shortcut
 * to enter Aven.
 *
 * OPTIONAL STATUS:
 * This widget is strictly optional and not core to Aven's value proposition.
 * Aven's core habit intervention, garden evolution, and offline reflection
 * function completely independently of this widget.
 *
 * HOW TO REMOVE OR DISABLE:
 * To remove this feature and decrease APK footprint:
 * 1. Remove the <receiver android:name=".androidintegration.widget.AvenGardenWidgetProvider">
 *    block from AndroidManifest.xml.
 * 2. Delete res/xml/aven_widget_info.xml and res/layout/aven_widget_layout.xml.
 * 3. Delete this file (AvenGardenWidgetProvider.kt).
 * ==============================================================================
 */
class AvenGardenWidgetProvider : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    private fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        widgetScope.launch {
            try {
                val repository = AvenRepository.getInstance(context)
                val gardenState = repository.gardenState.first()
                val events = repository.allIntentEvents.first()
                val weeklyStats = BehaviorEngine.computeWeeklyStats(events)

                val stageName = gardenState?.stage?.replace('_', ' ')?.lowercase()
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    ?: "Seed"
                val envState = gardenState?.environmentState?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                } ?: "Resting"

                val intentionalRate = weeklyStats.intentionalPercentage

                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.aven_widget_layout)

                    views.setTextViewText(R.id.widget_stage_text, "$stageName Stage")
                    views.setTextViewText(R.id.widget_environment_badge, envState)
                    views.setTextViewText(
                        R.id.widget_stat_text,
                        "$intentionalRate% intentional choices today"
                    )

                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_action_pause, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (t: Throwable) {
                // Graceful fallback: render calm placeholder state without crashing widget host
                try {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    for (appWidgetId in appWidgetIds) {
                        val fallbackViews = RemoteViews(context.packageName, R.layout.aven_widget_layout)
                        fallbackViews.setTextViewText(R.id.widget_stage_text, "Aven Garden")
                        fallbackViews.setTextViewText(R.id.widget_environment_badge, "Calm")
                        fallbackViews.setTextViewText(R.id.widget_stat_text, "A calm space for reflection")
                        fallbackViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                        fallbackViews.setOnClickPendingIntent(R.id.widget_action_pause, pendingIntent)
                        appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
                    }
                } catch (ignored: Throwable) {
                    // Suppress any secondary errors
                }
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetComponent = ComponentName(context, AvenGardenWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
                if (widgetIds.isNotEmpty()) {
                    val intent = Intent(context, AvenGardenWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (t: Throwable) {
                // Ignore widget update failures safely
            }
        }
    }
}
