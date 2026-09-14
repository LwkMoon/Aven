package com.aven.app.core.goal

import com.aven.app.data.local.entities.DailySummaryEntity

/**
 * GoalEngine manages optional behavior goals.
 * Goals are additive and never required; the app functions fully with zero goals.
 */
object GoalEngine {

    data class GoalItem(
        val id: String,
        val title: String,
        val description: String,
        val targetDescription: String,
        val isEnabled: Boolean,
        val currentProgress: Float, // 0f to 1f
        val statusText: String,
        val isMet: Boolean = currentProgress >= 1f
    )

    fun evaluateGoals(
        todaySummary: DailySummaryEntity?,
        recentSummaries: List<DailySummaryEntity>
    ): List<GoalItem> {
        val totalDecisionsToday = todaySummary?.let { it.intentionalLaunches + it.mindlessLaunches + it.avoidedLaunches } ?: 0
        val intentionalDecisionsToday = todaySummary?.let { it.intentionalLaunches + it.avoidedLaunches } ?: 0
        val intentionalRateToday = if (totalDecisionsToday > 0) (intentionalDecisionsToday.toFloat() / totalDecisionsToday) else 1f
        val mindlessToday = todaySummary?.mindlessLaunches ?: 0

        return listOf(
            GoalItem(
                id = "goal_intentional_rate",
                title = "Maintain 70% intentionality",
                description = "Keep most app launches purposeful or paused.",
                targetDescription = "Target: >= 70%",
                isEnabled = true,
                currentProgress = (intentionalRateToday / 0.70f).coerceIn(0f, 1f),
                statusText = if (totalDecisionsToday == 0) "Awaiting today's first pause" else "${(intentionalRateToday * 100).toInt()}% today"
            ),
            GoalItem(
                id = "goal_mindless_limit",
                title = "Keep automatic opens under 5",
                description = "Catch impulsive checks before they multiply.",
                targetDescription = "Target: < 5 per day",
                isEnabled = true,
                currentProgress = (1f - (mindlessToday / 5f)).coerceIn(0f, 1f),
                statusText = "$mindlessToday mindless launches today"
            ),
            GoalItem(
                id = "goal_mindful_pause",
                title = "Pause and step away 3 times",
                description = "Choose 'Go back' when you realize you don't need the app.",
                targetDescription = "Target: 3 avoided opens",
                isEnabled = true,
                currentProgress = ((todaySummary?.avoidedLaunches ?: 0) / 3f).coerceIn(0f, 1f),
                statusText = "${todaySummary?.avoidedLaunches ?: 0} / 3 paused & avoided"
            )
        )
    }
}
