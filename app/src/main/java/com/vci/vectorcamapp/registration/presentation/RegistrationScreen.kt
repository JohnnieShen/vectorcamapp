package com.vci.vectorcamapp.registration.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.components.button.ActionButton
import com.vci.vectorcamapp.core.presentation.components.form.DropdownField
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.customShadow
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme
import com.vci.vectorcamapp.ui.theme.screenHeightFraction

@Composable
fun RegistrationScreen(
    state: RegistrationState, onAction: (RegistrationAction) -> Unit, modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.registration_background),
            contentDescription = "Mosquito background",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colors.cardBackground
            ),
            shape = RoundedCornerShape(
                topStart = MaterialTheme.dimensions.cornerRadiusMedium,
                topEnd = MaterialTheme.dimensions.cornerRadiusMedium
            ),
            modifier = modifier
                .height(screenHeightFraction(0.4f))
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = modifier
                    .padding(
                        horizontal = MaterialTheme.dimensions.paddingExtraLarge,
                        vertical = MaterialTheme.dimensions.paddingLarge
                    )
                    .fillMaxSize(), verticalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium)
                ) {
                    Text(
                        text = "Register Program",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colors.textPrimary
                    )

                    Text(
                        text = "Select your affiliated program",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colors.textPrimary
                    )
                }

                DropdownField(
                    options = state.programs,
                    selectedOption = state.selectedProgram,
                    onOptionSelected = { onAction(RegistrationAction.SelectProgram(it)) },
                    modifier = modifier
                        .customShadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = MaterialTheme.dimensions.shadowBlurMedium,
                            spread = MaterialTheme.dimensions.shadowBlurSmall,
                            cornerRadius = MaterialTheme.dimensions.cornerRadiusSmall,
                        )
                        .height(MaterialTheme.dimensions.componentHeightExtraExtraLarge),
                ) { program ->
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.dimensions.paddingSmall)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = program.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colors.textPrimary
                        )
                        Text(
                            text = program.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colors.textSecondary
                        )
                    }
                }

                ActionButton(
                    label = "Confirm",
                    onClick = { onAction(RegistrationAction.ConfirmRegistration) },
                    enabled = state.selectedProgram != null,
                    modifier = modifier
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun RegistrationScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            RegistrationScreen(
                state = RegistrationState(),
                onAction = { },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
