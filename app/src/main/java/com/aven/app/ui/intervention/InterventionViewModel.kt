package com.aven.app.ui.intervention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aven.app.core.audio.CalmAudioSynthesizer
import com.aven.app.core.intent.IntentEngine
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class InterventionPhase {
    data object SelectIntent : InterventionPhase()
    data class Pausing(val chosenIntent: IntentEngine.IntentOption, val secondsRemaining: Int) : InterventionPhase()
    data class DecisionReady(val chosenIntent: IntentEngine.IntentOption) : InterventionPhase()
    data class Completed(val message: String, val continued: Boolean) : InterventionPhase()
}

data class InterventionUiState(
    val packageName: String,
    val appDisplayName: String,
    val phase: InterventionPhase = InterventionPhase.SelectIntent,
    val configuredPauseSeconds: Int = 3
)

class InterventionViewModel(
    private val packageName: String,
    private val appDisplayName: String,
    private val repository: AvenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InterventionUiState(
            packageName = packageName,
            appDisplayName = appDisplayName
        )
    )
    val uiState: StateFlow<InterventionUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            val pause = repository.preferences.pauseDurationSeconds.first()
            _uiState.value = _uiState.value.copy(configuredPauseSeconds = pause)
        }
    }

    fun selectIntent(option: IntentEngine.IntentOption) {
        val totalSecs = _uiState.value.configuredPauseSeconds
        _uiState.value = _uiState.value.copy(
            phase = InterventionPhase.Pausing(chosenIntent = option, secondsRemaining = totalSecs)
        )
        CalmAudioSynthesizer.playBreatheCue()
        startCountdown(option, totalSecs)
    }

    private fun startCountdown(option: IntentEngine.IntentOption, durationSeconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                if (remaining > 0) {
                    _uiState.value = _uiState.value.copy(
                        phase = InterventionPhase.Pausing(chosenIntent = option, secondsRemaining = remaining)
                    )
                }
            }
            CalmAudioSynthesizer.playPauseCompleteChime()
            _uiState.value = _uiState.value.copy(
                phase = InterventionPhase.DecisionReady(chosenIntent = option)
            )
        }
    }

    fun chooseContinue() {
        val currentPhase = _uiState.value.phase
        val option = when (currentPhase) {
            is InterventionPhase.DecisionReady -> currentPhase.chosenIntent
            is InterventionPhase.Pausing -> currentPhase.chosenIntent
            else -> IntentEngine.allOptions.first()
        }

        viewModelScope.launch {
            repository.recordInterventionDecision(
                packageName = packageName,
                appName = appDisplayName,
                intentType = option.label,
                isIntentional = option.isIntentional,
                continued = true,
                pauseDurationSeconds = _uiState.value.configuredPauseSeconds
            )
            _uiState.value = _uiState.value.copy(
                phase = InterventionPhase.Completed(
                    message = "Opening $appDisplayName with conscious intent.",
                    continued = true
                )
            )
        }
    }

    fun chooseLeave() {
        val currentPhase = _uiState.value.phase
        val option = when (currentPhase) {
            is InterventionPhase.DecisionReady -> currentPhase.chosenIntent
            is InterventionPhase.Pausing -> currentPhase.chosenIntent
            else -> IntentEngine.allOptions.first()
        }

        viewModelScope.launch {
            CalmAudioSynthesizer.playWorldGrowthChime()
            repository.recordInterventionDecision(
                packageName = packageName,
                appName = appDisplayName,
                intentType = option.label,
                isIntentional = option.isIntentional,
                continued = false,
                pauseDurationSeconds = _uiState.value.configuredPauseSeconds
            )
            _uiState.value = _uiState.value.copy(
                phase = InterventionPhase.Completed(
                    message = "You paused and stepped away. Your garden takes quiet nourishment.",
                    continued = false
                )
            )
        }
    }

    class Factory(
        private val packageName: String,
        private val appDisplayName: String,
        private val repository: AvenRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InterventionViewModel(packageName, appDisplayName, repository) as T
        }
    }
}
