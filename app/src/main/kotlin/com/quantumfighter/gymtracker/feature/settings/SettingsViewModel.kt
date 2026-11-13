package com.quantumfighter.gymtracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumfighter.gymtracker.data.settings.UiPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class SettingsUiState(val loading: Boolean = true, val hideCompleted: Boolean = false, val saving: Boolean = false, val error: String? = null)

/** Observes committed preferences across routes; attempted values are retained for explicit retry. */
internal class SettingsViewModel(private val repository: UiPreferencesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state = mutableState.asStateFlow()
    private var observer: Job? = null
    private var attempted: Boolean? = null

    init {
        load()
    }

    fun load() {
        observer?.cancel()
        mutableState.value = mutableState.value.copy(loading = true, error = null)
        observer = viewModelScope.launch {
            try {
                repository.observeHiddenCompleted().collect { value ->
                    mutableState.value = mutableState.value.copy(loading = false, hideCompleted = value)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(loading = true, error = "Could not read preferences. Retry to open them again.")
            }
        }
    }

    fun hideCompleted(value: Boolean) {
        if (mutableState.value.saving || mutableState.value.loading) return
        attempted = value
        mutableState.value = mutableState.value.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                repository.hideCompleted(value)
                mutableState.value = mutableState.value.copy(hideCompleted = value)
                attempted = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(error = "Could not save preference. Your previous setting is still in use.")
            } finally {
                mutableState.value = mutableState.value.copy(saving = false)
            }
        }
    }

    fun retry() {
        val value = attempted
        if (value == null || mutableState.value.loading) load() else hideCompleted(value)
    }
}
