
package com.vci.vectorcamapp.registration.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.core.domain.model.Program
import com.vci.vectorcamapp.animation.presentation.LoadingAnimation
import com.vci.vectorcamapp.registration.domain.enums.ProgramOption
import com.vci.vectorcamapp.surveillance_form.presentation.components.DropdownField

@Composable
fun RegistrationScreen(
    state: RegistrationState,
    onAction: (RegistrationAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            if (state.isLoading) {
                LoadingAnimation(text = "Loading programs…")
            } else {
                state.error?.let {
                    Text("Error: $it", modifier = Modifier.padding(16.dp))
                }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    DropdownField(
                        label = "Program",
                        options = state.programs.map { ProgramOption(it.name) },
                        selectedOption = ProgramOption(state.selectedProgramName),
                        onOptionSelected = { onAction(RegistrationAction.SelectProgram(it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Button(
                        onClick  = { onAction(RegistrationAction.ConfirmRegistration) },
                        enabled  = state.selectedProgramName != "",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}
