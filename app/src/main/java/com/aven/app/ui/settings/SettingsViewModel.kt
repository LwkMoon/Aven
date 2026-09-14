package com.aven.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aven.app.androidintegration.apps.DiscoveredApp
import com.aven.app.androidintegration.apps.InstalledAppsProvider
import com.aven.app.core.audio.CalmAudioSynthesizer
import com.aven.app.data.local.entities.MonitoredAppEntity
import com.aven.app.data.repository.AvenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val monitoredApps: List<MonitoredAppEntity> = emptyList(),
    val pauseDurationSeconds: Int = 3,
    val themeMode: String = "system",
    val notificationsEnabled: Boolean = false,
    val isReducedMotion: Boolean = false,
    val simUsageAccess: Boolean = true,
    val simAccessibility: Boolean = true,
    val simNotifications: Boolean = true,
    val infoMessage: String? = null
)

class SettingsViewModel(private val repository: AvenRepository) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<DiscoveredApp>>(emptyList())
    val installedApps: StateFlow<List<DiscoveredApp>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.allMonitoredApps,
        repository.preferences.pauseDurationSeconds,
        repository.preferences.themeMode,
        repository.preferences.notificationsEnabled,
        repository.preferences.isReducedMotion
    ) { apps, pause, theme, notif, motion ->
        SettingsUiState(
            monitoredApps = apps,
            pauseDurationSeconds = pause,
            themeMode = theme,
            notificationsEnabled = notif,
            isReducedMotion = motion
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val provider = InstalledAppsProvider(context)
            val apps = provider.getInstalledLaunchableApps()
            val monitored = uiState.value.monitoredApps
            val mapped = apps.map { app ->
                val isMon = monitored.any { it.packageName == app.packageName && it.enabled }
                app.copy(isMonitored = isMon)
            }
            _installedApps.value = mapped
            _isLoadingApps.value = false
        }
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setAppEnabled(packageName, enabled)
        }
    }

    fun toggleDiscoveredApp(app: DiscoveredApp) {
        viewModelScope.launch {
            val existing = uiState.value.monitoredApps.find { it.packageName == app.packageName }
            if (existing != null) {
                repository.setAppEnabled(app.packageName, !existing.enabled)
            } else {
                repository.addCustomMonitoredApp(
                    packageName = app.packageName,
                    displayName = app.displayName,
                    category = "Device App"
                )
            }
            // Update local installed apps state
            _installedApps.value = _installedApps.value.map {
                if (it.packageName == app.packageName) it.copy(isMonitored = !it.isMonitored) else it
            }
        }
    }

    fun addCustomApp(displayName: String, packageName: String, category: String) {
        viewModelScope.launch {
            repository.addCustomMonitoredApp(packageName, displayName, category)
        }
    }

    fun playChimePreview() {
        CalmAudioSynthesizer.playPauseCompleteChime(force = true)
    }

    fun playGrowthPreview() {
        CalmAudioSynthesizer.playWorldGrowthChime(force = true)
    }

    fun setPauseDuration(seconds: Int) {
        viewModelScope.launch {
            repository.preferences.setPauseDuration(seconds)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.preferences.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setNotificationsEnabled(enabled)
        }
    }

    fun setReducedMotion(reduced: Boolean) {
        viewModelScope.launch {
            repository.preferences.setReducedMotion(reduced)
        }
    }

    fun generateDemoData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.generateSampleHistoricalData()
            onComplete()
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetAllData()
            onComplete()
        }
    }

    suspend fun getExportJson(): String {
        return repository.exportDataAsJson()
    }

    class Factory(private val repository: AvenRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}

