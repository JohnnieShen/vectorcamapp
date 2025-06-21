package com.vci.vectorcamapp.complete_session.specimens.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.complete_session.specimens.presentation.components.CompleteSessionSpecimensCard

@Composable
fun CompleteSessionSpecimensScreen(
    state: CompleteSessionSpecimensState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = state.specimens.asReversed(), key = { it.id }) { specimen ->
            CompleteSessionSpecimensCard(session = state.session, specimen = specimen, modifier = modifier)
        }
    }
}
