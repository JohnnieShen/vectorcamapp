package com.vci.vectorcamapp.incomplete_session.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.incomplete_session.presentation.components.IncompleteSessionCard
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun IncompleteSessionScreen(
    state: IncompleteSessionState,
    onAction: (IncompleteSessionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenHeader(
        title = "Incomplete Sessions",
        subtitle = "Click on a session to resume",
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "Back Button",
                tint = MaterialTheme.colors.icon,
                modifier = Modifier
                    .size(MaterialTheme.dimensions.iconSizeMedium)
                    .clickable {
                        onAction(IncompleteSessionAction.ReturnToLandingScreen)
                    })
        },
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
