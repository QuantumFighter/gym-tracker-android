package com.quantumfighter.gymtracker.feature.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumfighter.gymtracker.R
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Immutable presentation state consumed by [HomeScreen].
 *
 * @property title Android string resource ID, resolved against the current locale during composition.
 * @property catalogueLoading True until the local catalogue is ready to use.
 * @property catalogueFailed True when storage is unavailable; retry never resets the database.
 */
data class HomeUiState(
    @param:StringRes val title: Int = R.string.app_name,
    val catalogueLoading: Boolean = true,
    val catalogueFailed: Boolean = false,
)

/**
 * Prepares local exercises off the UI thread through a repository, retaining no Android context.
 * @param exercises Manually injected persistence boundary.
 */
class HomeViewModel(private val exercises: ExerciseRepository) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())

    /** Read-only screen state; lifecycle-aware collection belongs to [HomeRoute]. */
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    init {
        prepareCatalogue()
    }

    /** Retries a failed catalogue open/seed without discarding the existing database. */
    fun retryCatalogue() {
        if (!mutableUiState.value.catalogueLoading) prepareCatalogue()
    }

    private fun prepareCatalogue() {
        mutableUiState.value = HomeUiState(catalogueLoading = true)
        viewModelScope.launch {
            try {
                exercises.seedCatalogue()
                mutableUiState.value = HomeUiState(catalogueLoading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableUiState.value = HomeUiState(catalogueLoading = false, catalogueFailed = true)
            }
        }
    }
}
