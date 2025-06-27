package com.vci.vectorcamapp.incomplete_session.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.incomplete_session.presentation.components.IncompleteSessionCard
import java.util.UUID

@Composable
fun IncompleteSessionScreen(
    state: IncompleteSessionState,
    onAction: (IncompleteSessionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = state.sessions.asReversed(), key = { it.localId }) { session ->
            IncompleteSessionCard(session = session,
                onClick = { onAction(IncompleteSessionAction.ResumeSession(session.localId)) },
                modifier = modifier
            )
        }
    }
}
