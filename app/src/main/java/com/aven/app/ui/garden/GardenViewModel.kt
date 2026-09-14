package com.aven.app.ui.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aven.app.core.garden.GardenEngine
import com.aven.app.data.local.entities.GardenStateEntity
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GardenUiState(
    val gardenState: GardenStateEntity = GardenStateEntity(),
    val currentStage: GardenEngine.GardenStage = GardenEngine.GardenStage.SEED,
    val unlockedFlora: List<GardenEngine.EnvironmentalElement> = emptyList(),
    val isReducedMotion: Boolean = false,
    val historyMilestones: List<MilestoneItem> = emptyList()
)

data class MilestoneItem(
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

class GardenViewModel(private val repository: AvenRepository) : ViewModel() {

    val uiState: StateFlow<GardenUiState> = combine(
        repository.gardenState,
        repository.preferences.isReducedMotion
    ) { state, reducedMotion ->
        val entity = state ?: GardenStateEntity()
        val stage = GardenEngine.GardenStage.fromName(entity.stage)

        val unlocked = GardenEngine.allElements.filter {
            it.stage.ordinal <= stage.ordinal
        }

        val milestones = listOf(
            MilestoneItem(
                title = "A Quiet Beginning",
                description = "Planted the seed of awareness.",
                isUnlocked = true
            ),
            MilestoneItem(
                title = "Tender Sprout",
                description = "Broke through the surface with your first conscious pauses.",
                isUnlocked = stage >= GardenEngine.GardenStage.SPROUT
            ),
            MilestoneItem(
                title = "Sapling Grove",
                description = "Canopy formed across days of steady intentionality.",
                isUnlocked = stage >= GardenEngine.GardenStage.GROVE
            ),
            MilestoneItem(
                title = "Living Sanctuary",
                description = "A winding brook and stone cairn shelter the wild earth.",
                isUnlocked = stage >= GardenEngine.GardenStage.HABITAT
            ),
            MilestoneItem(
                title = "Living World",
                description = "An expansive balanced sanctuary of timeless redwood and flora.",
                isUnlocked = stage >= GardenEngine.GardenStage.LIVING_WORLD
            )
        )

        GardenUiState(
            gardenState = entity,
            currentStage = stage,
            unlockedFlora = unlocked,
            isReducedMotion = reducedMotion,
            historyMilestones = milestones
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GardenUiState()
    )

    class Factory(private val repository: AvenRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GardenViewModel(repository) as T
        }
    }
}
