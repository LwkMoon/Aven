package com.aven.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aven.app.core.behavior.BehaviorEngine
import com.aven.app.core.goal.GoalEngine
import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.IntentEventEntity
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InsightsUiState(
    val weeklyStats: BehaviorEngine.WeeklyStats = BehaviorEngine.WeeklyStats(0, 0, 0, 0, 0, 0),
    val observations: List<BehaviorEngine.BehavioralInsight> = emptyList(),
    val appBreakdowns: List<BehaviorEngine.AppBreakdown> = emptyList(),
    val goals: List<GoalEngine.GoalItem> = emptyList(),
    val isEmpty: Boolean = true
)

class InsightsViewModel(private val repository: AvenRepository) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        repository.allIntentEvents,
        repository.allDailySummaries
    ) { events, summaries ->
        val weekly = BehaviorEngine.computeWeeklyStats(events)
        val observations = BehaviorEngine.deriveInsights(events)
        val breakdowns = BehaviorEngine.computeAppBreakdowns(events)
        val todayStr = BehaviorEngine.todayDateString()
        val todaySummary = summaries.find { it.date == todayStr }
        val goals = GoalEngine.evaluateGoals(todaySummary, summaries)

        InsightsUiState(
            weeklyStats = weekly,
            observations = observations,
            appBreakdowns = breakdowns,
            goals = goals,
            isEmpty = events.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsUiState()
    )

    class Factory(private val repository: AvenRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightsViewModel(repository) as T
        }
    }
}
