package com.vci.vectorcamapp.incomplete_session.presentation

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.incomplete_session.presentation.components.IncompleteSessionCard

@Composable
fun IncompleteSessionScreen(
    state: IncompleteSessionState,
    onAction: (IncompleteSessionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenHeader(
        title = "Incomplete Sessions",
        subtitle = "Click on a session to resume",
        modifier = modifier
    ) {
        items(
            items = state.sessions.asReversed(),
            key = { it.localId }
        ) { session ->
            IncompleteSessionCard(
                session = session,
                onClick = { onAction(IncompleteSessionAction.ResumeSession(session.localId)) }
            )
        }
    }
}
