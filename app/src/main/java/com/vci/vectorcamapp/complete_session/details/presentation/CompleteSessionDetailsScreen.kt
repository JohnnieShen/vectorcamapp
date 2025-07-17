package com.vci.vectorcamapp.complete_session.details.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.complete_session.details.presentation.components.SegmentedTabBar
import com.vci.vectorcamapp.complete_session.details.presentation.components.form.CompleteSessionForm
import com.vci.vectorcamapp.complete_session.details.presentation.components.specimens.CompleteSessionSpecimens
import com.vci.vectorcamapp.complete_session.details.presentation.enums.CompleteSessionDetailsTab
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun CompleteSessionDetailsScreen(
    state: CompleteSessionDetailsState,
    onAction: (CompleteSessionDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {

    ScreenHeader(
        title = "Session Information",
        subtitle = "ID: ${state.session.localId}",
        modifier = modifier
    ) {
        item {
            SegmentedTabBar(
                tabs = CompleteSessionDetailsTab.entries,
                selectedTab = state.selectedTab,
                onTabSelected = { onAction(CompleteSessionDetailsAction.ChangeSelectedTab(it)) },
            )
        }

        item {
            when (state.selectedTab) {
                CompleteSessionDetailsTab.SESSION_FORM -> CompleteSessionForm(
                    session = state.session,
                    site = state.site,
                    surveillanceForm = state.surveillanceForm,
                    modifier = modifier
                )

                CompleteSessionDetailsTab.SESSION_SPECIMENS -> CompleteSessionSpecimens(
                    session = state.session, specimens = state.specimens, modifier = modifier
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun CompleteSessionDetailsScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CompleteSessionDetailsScreen(
                state = CompleteSessionDetailsState(),
                onAction = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
