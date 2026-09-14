package com.aven.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aven.app.data.local.entities.MonitoredAppEntity
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentPage: Int = 0,
    val monitoredApps: List<MonitoredAppEntity> = emptyList(),
    val selectedPauseSeconds: Int = 3,
    val isCompleted: Boolean = false
)

class OnboardingViewModel(private val repository: AvenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            val apps = repository.allMonitoredApps.first()
            val pause = repository.preferences.pauseDurationSeconds.first()
            _uiState.value = _uiState.value.copy(
                monitoredApps = apps,
                selectedPauseSeconds = pause
            )
        }
    }

    fun nextPage() {
        if (_uiState.value.currentPage < 4) {
            _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage + 1)
        }
    }

    fun previousPage() {
        if (_uiState.value.currentPage > 0) {
            _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage - 1)
        }
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setAppEnabled(packageName, enabled)
            val updated = _uiState.value.monitoredApps.map {
                if (it.packageName == packageName) it.copy(enabled = enabled) else it
            }
            _uiState.value = _uiState.value.copy(monitoredApps = updated)
        }
    }

    fun selectPause(seconds: Int) {
        viewModelScope.launch {
            repository.preferences.setPauseDuration(seconds)
            _uiState.value = _uiState.value.copy(selectedPauseSeconds = seconds)
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.preferences.setOnboardingCompleted(true)
            _uiState.value = _uiState.value.copy(isCompleted = true)
            onFinished()
        }
    }

    class Factory(private val repository: AvenRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(repository) as T
        }
    }
}
