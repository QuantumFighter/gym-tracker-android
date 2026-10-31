package com.quantumfighter.gymtracker.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.R

/**
 * Connects [HomeViewModel] to [HomeScreen] using lifecycle-aware state collection.
 *
 * @param homeViewModel State owner constructed by the application composition root.
 */
@Composable
fun HomeRoute(homeViewModel: HomeViewModel) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState, onRetryCatalogue = homeViewModel::retryCatalogue)
}

/**
 * Renders Home from caller-owned [uiState], without performing persistence or navigation.
 *
 * @param uiState Immutable presentation state to display.
 * @param modifier Layout and semantics applied to the screen's scaffold.
 * @param onRetryCatalogue Requests another non-destructive storage initialisation attempt.
 */
@Composable
fun HomeScreen(uiState: HomeUiState, modifier: Modifier = Modifier, onRetryCatalogue: () -> Unit = {}) {
    Scaffold(modifier = modifier) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(uiState.title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.foundation_message),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (uiState.catalogueLoading) {
                Text(stringResource(R.string.catalogue_loading))
            }
            if (uiState.catalogueFailed) {
                Text(stringResource(R.string.catalogue_failed))
                Button(onClick = onRetryCatalogue) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}
