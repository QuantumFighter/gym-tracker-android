package com.quantumfighter.gymtracker.feature.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.quantumfighter.gymtracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Immutable presentation state consumed by [HomeScreen].
 *
 * @property title Android string resource ID, resolved against the current locale during composition.
 */
data class HomeUiState(
    @param:StringRes val title: Int = R.string.app_name,
)

/** Owns Home presentation state without retaining an Activity or accessing storage. */
class HomeViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())

    /** Read-only screen state; lifecycle-aware collection belongs to [HomeRoute]. */
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()
}
