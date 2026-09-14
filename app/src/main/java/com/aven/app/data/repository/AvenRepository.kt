package com.aven.app.data.repository

import android.content.Context
import com.aven.app.androidintegration.notifications.AvenNotificationService
import com.aven.app.androidintegration.widget.AvenGardenWidgetProvider
import com.aven.app.core.behavior.BehaviorEngine
import com.aven.app.core.garden.GardenEngine
import com.aven.app.data.local.AvenDatabase
import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.data.local.entities.IntentEventEntity
import com.aven.app.data.local.entities.MonitoredAppEntity
import com.aven.app.data.local.entities.UsageSessionEntity
import com.aven.app.data.preferences.AvenPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AvenRepository(
    private val database: AvenDatabase,
    val preferences: AvenPreferences,
    private val context: Context? = null
) {
    val allMonitoredApps: Flow<List<MonitoredAppEntity>> = database.monitoredAppDao().getAllApps()
    val enabledMonitoredApps: Flow<List<MonitoredAppEntity>> = database.monitoredAppDao().getEnabledApps()
    val allIntentEvents: Flow<List<IntentEventEntity>> = database.intentEventDao().getAllEvents()
    val allDailySummaries: Flow<List<DailySummaryEntity>> = database.dailySummaryDao().getAllSummaries()
    val gardenState: Flow<GardenStateEntity?> = database.gardenStateDao().getGardenState()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        // 1. Seed Monitored Apps if empty
        if (database.monitoredAppDao().getAppCount() == 0) {
            val defaultApps = listOf(
                MonitoredAppEntity("com.instagram.android", "Instagram", true, "Social"),
                MonitoredAppEntity("com.twitter.android", "X / Twitter", true, "Social"),
                MonitoredAppEntity("com.google.android.youtube", "YouTube", true, "Video"),
                MonitoredAppEntity("com.zhiliaoapp.musically", "TikTok", true, "Social"),
                MonitoredAppEntity("com.reddit.frontpage", "Reddit", true, "Community"),
                MonitoredAppEntity("com.google.android.apps.messaging", "Messages", false, "Communication")
            )
            database.monitoredAppDao().insertApps(defaultApps)
        }

        // 2. Seed GardenState if missing
        val existingGarden = database.gardenStateDao().getGardenStateSnapshot()
        if (existingGarden == null) {
            val initialGarden = GardenStateEntity(
                id = 1,
                growthLevel = 0f,
                stage = "SEED",
                environmentState = "resting",
                unlockedElementsRaw = "seed_pod,rich_soil",
                consecutiveHealthyDays = 0,
                totalIntentionalDecisions = 0,
                totalAvoidedDecisions = 0,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            database.gardenStateDao().setGardenState(initialGarden)
        }
    }

    suspend fun recordInterventionDecision(
        packageName: String,
        appName: String,
        intentType: String,
        isIntentional: Boolean,
        continued: Boolean,
        pauseDurationSeconds: Int
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val event = IntentEventEntity(
            timestamp = now,
            packageName = packageName,
            appName = appName,
            intentType = intentType,
            isIntentional = isIntentional,
            continued = continued,
            pauseDurationSeconds = pauseDurationSeconds
        )
        database.intentEventDao().insertEvent(event)

        // Update Daily Summary
        val todayStr = BehaviorEngine.todayDateString()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayEvents = database.intentEventDao().getEventsSinceSnapshot(calendar.timeInMillis)
        val summary = BehaviorEngine.aggregateDaily(todayStr, todayEvents)
        database.dailySummaryDao().insertSummary(summary)

        // Update Garden State
        val currentGarden = database.gardenStateDao().getGardenStateSnapshot() ?: GardenStateEntity()
        val updatedGarden = GardenEngine.applyEvent(currentGarden, event)
        database.gardenStateDao().setGardenState(updatedGarden)

        context?.let { ctx ->
            AvenGardenWidgetProvider.updateAllWidgets(ctx)
            if (currentGarden.stage != updatedGarden.stage) {
                val notificationsOn = preferences.notificationsEnabled.first()
                if (notificationsOn) {
                    val stageReadable = updatedGarden.stage.replace('_', ' ').lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    AvenNotificationService(ctx).sendQuietMilestone(
                        title = "World Growth: $stageReadable Stage",
                        message = "Your intentional choice nurtured new life in your living world."
                    )
                }
            }
        }
    }

    suspend fun setAppEnabled(packageName: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        database.monitoredAppDao().setAppEnabled(packageName, enabled)
    }

    suspend fun addCustomMonitoredApp(packageName: String, displayName: String, category: String) = withContext(Dispatchers.IO) {
        database.monitoredAppDao().insertApp(
            MonitoredAppEntity(packageName = packageName, displayName = displayName, enabled = true, category = category)
        )
    }

    suspend fun getTodaySummary(): DailySummaryEntity? = withContext(Dispatchers.IO) {
        database.dailySummaryDao().getSummaryForDate(BehaviorEngine.todayDateString())
    }

    suspend fun generateSampleHistoricalData() = withContext(Dispatchers.IO) {
        // Creates a rich 7-day progression for testing & demonstration per Section 22
        val apps = listOf(
            Pair("com.instagram.android", "Instagram"),
            Pair("com.twitter.android", "X / Twitter"),
            Pair("com.google.android.youtube", "YouTube"),
            Pair("com.reddit.frontpage", "Reddit")
        )

        val sampleEvents = mutableListOf<IntentEventEntity>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val sampleDailySummaries = mutableListOf<DailySummaryEntity>()

        for (daysAgo in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dateStr = dateFormat.format(calendar.time)

            var dayIntentional = 0
            var dayMindless = 0
            var dayAvoided = 0

            // 4 to 8 events per day
            val count = if (daysAgo == 0) 5 else 4 + (daysAgo % 4)
            for (i in 0 until count) {
                val app = apps[i % apps.size]
                val hour = 9 + (i * 2)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, 15 + (i * 7))

                // Varied intentional vs avoided vs mindless
                val randChoice = (daysAgo + i) % 5
                val (intent, isInt, continued) = when (randChoice) {
                    0 -> Triple("Check something specific", true, true)
                    1 -> Triple("Reply to someone", true, true)
                    2 -> Triple("Look for something", true, true)
                    3 -> Triple("I'm bored", false, false) // avoided!
                    else -> Triple("I don't know", false, true) // mindless
                }

                if (!continued) dayAvoided++
                else if (isInt) dayIntentional++
                else dayMindless++

                sampleEvents.add(
                    IntentEventEntity(
                        timestamp = calendar.timeInMillis,
                        packageName = app.first,
                        appName = app.second,
                        intentType = intent,
                        isIntentional = isInt,
                        continued = continued,
                        pauseDurationSeconds = 3
                    )
                )
            }

            sampleDailySummaries.add(
                DailySummaryEntity(
                    date = dateStr,
                    intentionalLaunches = dayIntentional,
                    mindlessLaunches = dayMindless,
                    interventions = dayIntentional + dayMindless + dayAvoided,
                    avoidedLaunches = dayAvoided,
                    totalUsageSeconds = (dayIntentional + dayMindless) * 180L
                )
            )
        }

        database.intentEventDao().insertEvents(sampleEvents)
        database.dailySummaryDao().insertSummaries(sampleDailySummaries)

        // Elevate garden state to Grove or Habitat for demo
        val demoGarden = GardenStateEntity(
            id = 1,
            growthLevel = 52.5f,
            stage = "GROVE",
            environmentState = "thriving",
            unlockedElementsRaw = "seed_pod,rich_soil,tender_sprout,morning_moss,silver_birch,elder_pine,fern_meadow",
            consecutiveHealthyDays = 5,
            totalIntentionalDecisions = sampleEvents.count { it.isIntentional && it.continued },
            totalAvoidedDecisions = sampleEvents.count { !it.continued },
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        database.gardenStateDao().setGardenState(demoGarden)
        context?.let { AvenGardenWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val events = database.intentEventDao().getAllEvents().first()
        val summaries = database.dailySummaryDao().getAllSummaries().first()
        val garden = database.gardenStateDao().getGardenStateSnapshot()

        buildString {
            append("{\n")
            append("  \"exportTimestamp\": ${System.currentTimeMillis()},\n")
            append("  \"garden\": {\n")
            append("    \"stage\": \"${garden?.stage ?: "SEED"}\",\n")
            append("    \"growthLevel\": ${garden?.growthLevel ?: 0f},\n")
            append("    \"environmentState\": \"${garden?.environmentState ?: "resting"}\"\n")
            append("  },\n")
            append("  \"summariesCount\": ${summaries.size},\n")
            append("  \"eventsCount\": ${events.size}\n")
            append("}")
        }
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        database.intentEventDao().deleteAll()
        database.usageSessionDao().deleteAll()
        database.dailySummaryDao().deleteAll()
        database.gardenStateDao().deleteAll()
        database.monitoredAppDao().deleteAll()
        preferences.clearAll()
        initializeDefaultsIfNeeded()
        context?.let { AvenGardenWidgetProvider.updateAllWidgets(it) }
    }

    companion object {
        @Volatile
        private var INSTANCE: AvenRepository? = null

        fun getInstance(context: Context): AvenRepository {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val db = AvenDatabase.getInstance(appContext)
                val prefs = AvenPreferences(appContext)
                val instance = AvenRepository(db, prefs, appContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
