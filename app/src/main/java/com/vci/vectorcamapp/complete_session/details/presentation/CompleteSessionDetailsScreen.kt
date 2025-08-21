package com.vci.vectorcamapp.complete_session.details.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.complete_session.details.presentation.components.SegmentedTabBar
import com.vci.vectorcamapp.complete_session.details.presentation.components.form.CompleteSessionForm
import com.vci.vectorcamapp.complete_session.details.presentation.components.specimens.CompleteSessionSpecimens
import com.vci.vectorcamapp.complete_session.details.presentation.enums.CompleteSessionDetailsTab
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
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
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "Back Button",
                tint = MaterialTheme.colors.icon,
                modifier = Modifier
                    .size(MaterialTheme.dimensions.iconSizeMedium)
                    .clickable {
                        onAction(CompleteSessionDetailsAction.ReturnToCompleteSessionListScreen)
                    })
        },
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
                    session = state.session,
                    specimensWithImagesAndInferenceResults = state.specimensWithImagesAndInferenceResults,
                    modifier = modifier
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
