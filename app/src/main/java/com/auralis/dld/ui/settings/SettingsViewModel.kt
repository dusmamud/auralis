package com.auralis.dld.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.dld.data.repository.PythonEngineRepository
import com.auralis.dld.domain.model.EngineStatus
import androidx.compose.runtime.Immutable
import com.auralis.dld.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val engineStatus: EngineStatus = EngineStatus()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val pythonRepository = PythonEngineRepository(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadEngineVersion()
    }

    private fun loadEngineVersion() {
        viewModelScope.launch {
            val ver = pythonRepository.getEngineVersion()
            _uiState.update {
                it.copy(engineStatus = it.engineStatus.copy(currentVersion = ver))
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun updateEngine() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    engineStatus = it.engineStatus.copy(
                        isUpdating = true,
                        updateError = null
                    )
                )
            }

            val result = pythonRepository.updateEngine()
            result.onSuccess { newVersion ->
                _uiState.update {
                    it.copy(
                        engineStatus = it.engineStatus.copy(
                            currentVersion = newVersion,
                            isUpdating = false,
                            updateError = null,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        engineStatus = it.engineStatus.copy(
                            isUpdating = false,
                            updateError = err.message ?: "Update failed"
                        )
                    )
                }
            }
        }
    }
}
