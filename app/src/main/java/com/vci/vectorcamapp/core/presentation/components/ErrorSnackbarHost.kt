package com.vci.vectorcamapp.core.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.vci.vectorcamapp.core.presentation.util.ObserveAsEvents
import com.vci.vectorcamapp.core.presentation.util.error.ErrorMessageBus
import com.vci.vectorcamapp.core.presentation.util.error.LocalErrorDataFlow
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.screenWidthFraction
import kotlinx.coroutines.launch

@Composable
fun ErrorSnackbarHost(
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val errorFlow = LocalErrorDataFlow.current
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(errorFlow) { errorData ->
        coroutineScope.launch {
            ErrorMessageBus.clearLastMessage()
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = errorData.error.toString(context),
                duration = errorData.duration
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState, modifier = modifier, snackbar = { snackbarData ->
            Snackbar(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                action = {
                    TextButton(
                        onClick = {
                            snackbarData.dismiss()
                            ErrorMessageBus.clearLastMessage()
                        }) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.dimensions.paddingSmall)
                    .widthIn(max = screenWidthFraction(1.0f))
            ) {
                Text(
                    text = snackbarData.visuals.message, style = MaterialTheme.typography.bodyMedium
                )
            }
        })
}
