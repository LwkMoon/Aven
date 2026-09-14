package com.aven.app.core.behavior

import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.IntentEventEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * BehaviorEngine aggregates raw events into daily summaries and
 * derives plain-language behavioral insights directly from recorded data.
 */
object BehaviorEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun todayDateString(): String = dateFormat.format(Date())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    /**
     * Recomputes or updates a DailySummary from a list of events belonging to that date.
     */
    fun aggregateDaily(date: String, events: List<IntentEventEntity>): DailySummaryEntity {
        var intentional = 0
        var mindless = 0
        var avoided = 0
        val interventions = events.size

        for (event in events) {
            if (!event.continued) {
                avoided++
            } else if (event.isIntentional) {
                intentional++
            } else {
                mindless++
            }
        }

        // Approximate 3 minutes per continued session as default baseline if usage session not tracked
        val estimatedUsageSeconds = (intentional + mindless) * 180L

        return DailySummaryEntity(
            date = date,
            intentionalLaunches = intentional,
            mindlessLaunches = mindless,
            interventions = interventions,
            avoidedLaunches = avoided,
            totalUsageSeconds = estimatedUsageSeconds
        )
    }

    data class WeeklyStats(
        val totalInterventions: Int,
        val intentionalCount: Int,
        val mindlessCount: Int,
        val avoidedCount: Int,
        val intentionalPercentage: Int,
        val avoidedPercentage: Int
    )

    fun computeWeeklyStats(events: List<IntentEventEntity>): WeeklyStats {
        if (events.isEmpty()) {
            return WeeklyStats(0, 0, 0, 0, 0, 0)
        }
        val total = events.size
        var intentional = 0
        var mindless = 0
        var avoided = 0

        for (e in events) {
            if (!e.continued) {
                avoided++
            } else if (e.isIntentional) {
                intentional++
            } else {
                mindless++
            }
        }

        val totalDecisions = intentional + mindless + avoided
        val intentionalPct = if (totalDecisions > 0) ((intentional + avoided) * 100) / totalDecisions else 0
        val avoidedPct = if (totalDecisions > 0) (avoided * 100) / totalDecisions else 0

        return WeeklyStats(
            totalInterventions = total,
            intentionalCount = intentional,
            mindlessCount = mindless,
            avoidedCount = avoided,
            intentionalPercentage = intentionalPct,
            avoidedPercentage = avoidedPct
        )
    }

    data class AppBreakdown(
        val packageName: String,
        val displayName: String,
        val totalInterventions: Int,
        val intentionalCount: Int,
        val mindlessCount: Int,
        val avoidedCount: Int,
        val intentionalRate: Int
    )

    fun computeAppBreakdowns(events: List<IntentEventEntity>): List<AppBreakdown> {
        val grouped = events.groupBy { it.packageName }
        return grouped.map { (pkg, appEvents) ->
            val appName = appEvents.firstOrNull()?.appName ?: pkg.substringAfterLast('.')
            var intentional = 0
            var mindless = 0
            var avoided = 0
            for (e in appEvents) {
                if (!e.continued) avoided++
                else if (e.isIntentional) intentional++
                else mindless++
            }
            val total = appEvents.size
            val rate = if (total > 0) ((intentional + avoided) * 100) / total else 0
            AppBreakdown(
                packageName = pkg,
                displayName = appName,
                totalInterventions = total,
                intentionalCount = intentional,
                mindlessCount = mindless,
                avoidedCount = avoided,
                intentionalRate = rate
            )
        }.sortedByDescending { it.totalInterventions }
    }

    data class BehavioralInsight(
        val id: String,
        val title: String,
        val observation: String,
        val context: String
    )

    /**
     * Derives plain-language, non-judgmental behavioral observations from real event history.
     */
    fun deriveInsights(events: List<IntentEventEntity>): List<BehavioralInsight> {
        if (events.isEmpty()) {
            return listOf(
                BehavioralInsight(
                    id = "empty_state",
                    title = "A quiet beginning",
                    observation = "Your pattern will reveal itself as you pause before opening monitored apps.",
                    context = "Events will be analyzed to illuminate your natural rhythms without judgment."
                )
            )
        }

        val insights = mutableListOf<BehavioralInsight>()

        // 1. Most common automatic trigger
        val uncertainEvents = events.filter { !it.isIntentional }
        if (uncertainEvents.isNotEmpty()) {
            val mostCommonUncertain = uncertainEvents.groupBy { it.intentType }
                .maxByOrNull { it.value.size }
            if (mostCommonUncertain != null) {
                val pct = (mostCommonUncertain.value.size * 100) / events.size
                insights.add(
                    BehavioralInsight(
                        id = "trigger_pattern",
                        title = "Common reflex",
                        observation = "Your most frequent automatic impulse was \"${mostCommonUncertain.key}\" ($pct% of checkpoints).",
                        context = "Noticing the trigger is the entire practice. The pause gives you space to choose freely."
                    )
                )
            }
        }

        // 2. Time of day impulse concentration
        val calendar = Calendar.getInstance()
        val hourCounts = IntArray(24)
        for (e in events) {
            calendar.timeInMillis = e.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hourCounts[hour]++
        }

        val eveningImpulses = (20..23).sumOf { hourCounts[it] } + (0..1).sumOf { hourCounts[it] }
        val afternoonImpulses = (13..17).sumOf { hourCounts[it] }
        val morningImpulses = (7..11).sumOf { hourCounts[it] }

        if (eveningImpulses >= afternoonImpulses && eveningImpulses >= morningImpulses && eveningImpulses > 0) {
            insights.add(
                BehavioralInsight(
                    id = "evening_pattern",
                    title = "Time window tendency",
                    observation = "You tend to reach for monitored apps most frequently in late evening (between 8 PM and 11 PM).",
                    context = "End-of-day winding down often brings an automatic search for stimulation."
                )
            )
        } else if (afternoonImpulses >= morningImpulses && afternoonImpulses > 0) {
            insights.add(
                BehavioralInsight(
                    id = "afternoon_pattern",
                    title = "Midday pause pattern",
                    observation = "Most launch impulses cluster around the midafternoon lull (1 PM to 5 PM).",
                    context = "A natural dip in energy often manifests as a desire to switch tasks."
                )
            )
        }

        // 3. Avoided launch rhythm
        val avoidedCount = events.count { !it.continued }
        if (avoidedCount > 0) {
            val avoidedRate = (avoidedCount * 100) / events.size
            insights.add(
                BehavioralInsight(
                    id = "pause_effect",
                    title = "The power of the pause",
                    observation = "In $avoidedRate% of pauses, simply taking a breath was enough to let you choose to step away.",
                    context = "Each time you turned back, your world gained stillness and nourishment."
                )
            )
        }

        // 4. Intentional clarity
        val intentionalCount = events.count { it.continued && it.isIntentional }
        if (intentionalCount > 0) {
            val intentionalApp = events.filter { it.isIntentional }
                .groupBy { it.appName }
                .maxByOrNull { it.value.size }
            if (intentionalApp != null) {
                insights.add(
                    BehavioralInsight(
                        id = "intentional_focus",
                        title = "Purposeful engagement",
                        observation = "Your most clearly purposeful usage was with ${intentionalApp.key}.",
                        context = "Entering with a stated purpose helps you accomplish what you came for and leave satisfied."
                    )
                )
            }
        }

        return insights
    }
}
