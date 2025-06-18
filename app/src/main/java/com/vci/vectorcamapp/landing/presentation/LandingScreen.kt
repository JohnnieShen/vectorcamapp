package com.vci.vectorcamapp.landing.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun LandingScreen(
    state: LandingState,
    onAction: (LandingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonModifier = Modifier
        .fillMaxWidth(0.8f)
        .height(60.dp)

    val verticalScrollState = rememberScrollState();

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(verticalScrollState),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "VectorCam",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Democratizing Vector Surveillance",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        // Buttons Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Button(
                onClick = { onAction(LandingAction.StartNewSurveillanceSession) },
                modifier = buttonModifier,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "New Surveillance Session",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onAction(LandingAction.StartNewNonSurveillanceSession) },
                modifier = buttonModifier,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "New Non-Surveillance Session",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { onAction(LandingAction.ViewIncompleteSessions) },
                modifier = buttonModifier,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "View Incomplete Sessions",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (state.incompleteSessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = "${state.incompleteSessions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { onAction(LandingAction.ViewCompleteSessions) },
                modifier = buttonModifier,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "View Complete Sessions",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Footer Section
        Text(
            text = "Version: ${state.versionName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
    if (state.showResumeDialog) {
        AlertDialog(
            onDismissRequest = { onAction(LandingAction.DismissResumePrompt) },
            title = { Text("Resume unfinished session?") },
            text = { Text("You have an incomplete surveillance session. Resume where you left off?") },
            confirmButton = {
                TextButton(onClick = { onAction(LandingAction.ResumeSession) }) {
                    Text("Yes, resume")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(LandingAction.DismissResumePrompt) }) {
                    Text("No, start new")
                }
            }
        )
    }
}

@PreviewLightDark
@Composable
fun LandingScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            LandingScreen(
                state = LandingState(),
                onAction = {},
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}