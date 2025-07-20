package com.vci.vectorcamapp.landing.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.landing.presentation.components.LandingActionTile
import com.vci.vectorcamapp.landing.presentation.components.LandingSection
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun LandingScreen(
    state: LandingState, onAction: (LandingAction) -> Unit, modifier: Modifier = Modifier
) {

    ScreenHeader(
        title = "Welcome to VectorCam!",
        subtitle = "Program: ${state.enrolledProgram.name}",
        modifier = modifier
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium)
            ) {
                LandingActionTile(
                    title = "Getting Started",
                    description = "Learn how to use the app and start your first session.",
                    icon = painterResource(R.drawable.ic_help),
                    onClick = { Log.d("LandingScreen", "Getting Started") })

                LandingSection(title = "Imaging") {
                    LandingActionTile(
                        title = "Start New Session",
                        description = "Begin a new household visit and capture mosquito images.",
                        icon = painterResource(R.drawable.ic_specimen),
                        onClick = { onAction(LandingAction.StartNewSession) })

                    LandingActionTile(
                        title = "Data Collection Mode",
                        description = "Capture and upload mosquito images without filling forms.",
                        icon = painterResource(R.drawable.ic_database),
                        onClick = { Log.d("LandingScreen", "Data Collection Mode") })
                }

                LandingSection(title = "Library") {
                    LandingActionTile(
                        title = "View Incomplete Sessions",
                        description = "Resume and complete any unfinished sessions.",
                        icon = painterResource(R.drawable.ic_minus_circle),
                        onClick = { onAction(LandingAction.ViewIncompleteSessions) },
                        badgeCount = state.incompleteSessionsCount)

                    LandingActionTile(
                        title = "View Complete Sessions",
                        description = "Review fully completed sessions and uploaded data.",
                        icon = painterResource(R.drawable.ic_complete),
                        onClick = { onAction(LandingAction.ViewCompleteSessions) })
                }
            }
        }
    }

    if (state.showResumeDialog) {
        AlertDialog(onDismissRequest = { onAction(LandingAction.DismissResumePrompt) }, title = {
            Text(
                text = "Resume unfinished session?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colors.textPrimary
            )
        }, text = {
            Text(
                text = "You have an incomplete surveillance session. Resume where you left off?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colors.textSecondary
            )
        }, confirmButton = {
            Button(
                onClick = { onAction(LandingAction.ResumeSession) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colors.successConfirm
                )
            ) {
                Text(
                    text = "Yes, resume",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colors.buttonText
                )
            }
        }, dismissButton = {
            TextButton(onClick = { onAction(LandingAction.DismissResumePrompt) }) {
                Text(
                    text = "No, start new",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colors.error
                )
            }
        })
    }
}

@PreviewLightDark
@Composable
fun LandingScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            LandingScreen(
                state = LandingState(), onAction = {}, modifier = Modifier.padding(innerPadding)
            )
        }
    }
}