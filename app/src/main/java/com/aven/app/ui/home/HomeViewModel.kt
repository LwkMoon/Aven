package com.aven.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aven.app.core.behavior.BehaviorEngine
import com.aven.app.core.garden.GardenEngine
import com.aven.app.data.local.entities.DailySummaryEntity
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.data.local.entities.MonitoredAppEntity
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val greeting: String = "Good day",
    val gardenState: GardenStateEntity = GardenStateEntity(),
    val todaySummary: DailySummaryEntity? = null,
    val monitoredApps: List<MonitoredAppEntity> = emptyList(),
    val isReducedMotion: Boolean = false,
    val hasMonitoredApps: Boolean = true,
    val isFirstDayEmpty: Boolean = true
)

class HomeViewModel(private val repository: AvenRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.gardenState,
        repository.allDailySummaries,
        repository.enabledMonitoredApps,
        repository.preferences.isReducedMotion
    ) { garden, summaries, apps, reducedMotion ->
        val todayStr = BehaviorEngine.todayDateString()
        val todaySummary = summaries.find { it.date == todayStr }
        val hasData = todaySummary != null && todaySummary.interventions > 0

        HomeUiState(
            greeting = computeTimeAwareGreeting(),
            gardenState = garden ?: GardenStateEntity(),
            todaySummary = todaySummary,
            monitoredApps = apps,
            isReducedMotion = reducedMotion,
            hasMonitoredApps = apps.isNotEmpty(),
            isFirstDayEmpty = !hasData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(greeting = computeTimeAwareGreeting())
    )

    private fun computeTimeAwareGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }

    class Factory(private val repository: AvenRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
