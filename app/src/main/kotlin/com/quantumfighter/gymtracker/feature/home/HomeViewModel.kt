package com.quantumfighter.gymtracker.feature.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.quantumfighter.gymtracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    @param:StringRes val title: Int = R.string.app_name,
)

class HomeViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()
}
