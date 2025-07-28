package com.vci.vectorcamapp.main.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.animation.presentation.LoadingAnimation
import com.vci.vectorcamapp.main.presentation.MainAction
import com.vci.vectorcamapp.main.presentation.MainState
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun PermissionAndGpsPrompt(
    state: MainState, onAction: (MainAction) -> Unit, modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        if (!state.allGranted) {
            onAction(MainAction.RequestPermissions)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimensions.paddingMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "This app needs access to your camera and location to function properly. " + "Camera access is essential for capturing images of specimens, while location access helps identify where the specimens were collected. " + "It looks like you’ve denied one or more of these permissions. " + "To continue using the app, please enable these permissions in the app settings. ",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!state.allGranted) {
                Button(onClick = { onAction(MainAction.OpenAppSettings) }) {
                    Text(
                        text = "Grant Permissions",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (!state.isGpsEnabled) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingSmall))
                Button(onClick = { onAction(MainAction.OpenLocationSettings) }) {
                    Text(
                        text = "Enable GPS",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
