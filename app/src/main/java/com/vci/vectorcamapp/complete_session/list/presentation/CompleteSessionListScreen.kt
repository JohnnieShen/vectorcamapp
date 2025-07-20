package com.vci.vectorcamapp.complete_session.list.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.complete_session.list.presentation.components.CompleteSessionListTile
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun CompleteSessionListScreen(
    state: CompleteSessionListState,
    onAction: (CompleteSessionListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenHeader(
        title = "Complete Sessions",
        subtitle = "Click on a session to view more details",
        modifier = modifier
    ) {
        items(
            items = state.sessionsAndSites.asReversed(),
            key = { it.session.localId }) { sessionAndSite ->
            CompleteSessionListTile(
                session = sessionAndSite.session, site = sessionAndSite.site, onClick = {
                    onAction(
                        CompleteSessionListAction.ViewCompleteSessionDetails(
                            sessionAndSite.session.localId
                        )
                    )
                })
        }
    }
}

@PreviewLightDark
@Composable
fun CompleteSessionListScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CompleteSessionListScreen(
                state = CompleteSessionListState(),
                onAction = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
