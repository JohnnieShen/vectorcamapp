package com.vci.vectorcamapp.intake.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.components.button.ActionButton
import com.vci.vectorcamapp.core.presentation.components.form.DatePickerField
import com.vci.vectorcamapp.core.presentation.components.form.DropdownField
import com.vci.vectorcamapp.core.presentation.components.form.TextEntryField
import com.vci.vectorcamapp.core.presentation.components.form.ToggleField
import com.vci.vectorcamapp.core.presentation.components.header.ScreenHeader
import com.vci.vectorcamapp.core.presentation.components.pill.InfoPill
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.intake.domain.model.IntakeDropdownOptions
import com.vci.vectorcamapp.intake.domain.util.IntakeError
import com.vci.vectorcamapp.intake.presentation.components.IntakeTile
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun IntakeScreen(
    state: IntakeState, onAction: (IntakeAction) -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        onAction(IntakeAction.ReturnToLandingScreen)
    }

    ScreenHeader(
        title = "Session Intake",
        subtitle = "Please fill out the information below",
        modifier = modifier
    ) {
        item {
            IntakeTile(
                title = "General Information",
                iconPainter = painterResource(R.drawable.ic_info),
                iconDescription = "General Information Icon"
            ) {
                InfoPill(text = "Session Type: ${state.session.type.name}", color = MaterialTheme.colors.info)

                TextEntryField(
                    label = "Collector Name",
                    value = state.session.collectorName,
                    onValueChange = { onAction(IntakeAction.EnterCollectorName(it)) },
                    singleLine = true,
                    error = state.intakeErrors.collectorName
                )

                TextEntryField(
                    label = "Collector Title",
                    value = state.session.collectorTitle,
                    onValueChange = { onAction(IntakeAction.EnterCollectorTitle(it)) },
                    singleLine = true,
                    error = state.intakeErrors.collectorTitle
                )

                DatePickerField(
                    label = "Collection Date",
                    selectedDateInMillis = state.session.collectionDate,
                    onDateSelected = { onAction(IntakeAction.PickCollectionDate(it)) },
                    error = state.intakeErrors.collectionDate,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownField(
                    label = "Collection Method",
                    options = IntakeDropdownOptions.CollectionMethodOption.entries,
                    selectedOption = IntakeDropdownOptions.CollectionMethodOption.entries.firstOrNull { it.label == state.session.collectionMethod },
                    onOptionSelected = {
                        onAction(
                            IntakeAction.SelectCollectionMethod(
                                it
                            )
                        )
                    },
                    error = state.intakeErrors.collectionMethod,
                    modifier = Modifier.fillMaxWidth()
                ) { collectionMethod ->
                    Text(
                        text = collectionMethod.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colors.textPrimary
                    )
                }

                DropdownField(
                    label = "Specimen Condition",
                    options = IntakeDropdownOptions.SpecimenConditionOption.entries,
                    selectedOption = IntakeDropdownOptions.SpecimenConditionOption.entries.firstOrNull { it.label == state.session.specimenCondition },
                    onOptionSelected = { onAction(IntakeAction.SelectSpecimenCondition(it)) },
                    error = state.intakeErrors.specimenCondition,
                    modifier = Modifier.fillMaxWidth()
                ) { specimenCondition ->
                    Text(
                        text = specimenCondition.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colors.textPrimary
                    )
                }
            }
        }

        item {
            IntakeTile(
                title = "Geographical Information",
                iconPainter = painterResource(id = R.drawable.ic_pin),
                iconDescription = "Geographical Information Icon"
            ) {
                DropdownField(
                    label = "District",
                    options = state.allSitesInProgram.map { it.district }.distinct(),
                    selectedOption = state.selectedDistrict,
                    onOptionSelected = { onAction(IntakeAction.SelectDistrict(it)) },
                    error = state.intakeErrors.district,
                ) { district ->
                    Text(
                        text = district,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colors.textPrimary
                    )
                }

                if (state.selectedDistrict.isNotBlank()) {
                    DropdownField(
                        label = "Sentinel Site",
                        options = state.allSitesInProgram.filter { it.district == state.selectedDistrict }
                            .map { it.sentinelSite }.distinct(),
                        selectedOption = state.selectedSentinelSite,
                        onOptionSelected = {
                            onAction(IntakeAction.SelectSentinelSite(it))
                        },
                        error = state.intakeErrors.sentinelSite,

                        ) { sentinelSite ->
                        Text(
                            text = sentinelSite,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colors.textPrimary
                        )
                    }
                }

                TextEntryField(
                    label = "House Number",
                    value = state.session.houseNumber,
                    onValueChange = { onAction(IntakeAction.EnterHouseNumber(it)) },
                    singleLine = true,
                    error = state.intakeErrors.houseNumber
                )

                state.surveillanceForm?.let { surveillanceForm ->
                    TextEntryField(
                        label = "Number of People Living in the House",
                        value = if (surveillanceForm.numPeopleSleptInHouse == 0) "" else surveillanceForm.numPeopleSleptInHouse.toString(),
                        onValueChange = { onAction(IntakeAction.EnterNumPeopleSleptInHouse(it.filter { character -> character.isDigit() })) },
                        placeholder = "0",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                    )
                }

                when {
                    state.session.latitude != null && state.session.longitude != null -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InfoPill(
                                text = "Latitude: ${state.session.latitude}",
                                color = MaterialTheme.colors.info,
                                modifier = Modifier.weight(1f)
                            )
                            InfoPill(
                                text = "Longitude: ${state.session.longitude}",
                                color = MaterialTheme.colors.info,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    state.locationError != null -> {
                        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall)) {
                            Text(
                                text = "Could not get location: ${
                                    state.locationError.toString(
                                        context
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colors.error
                            )

                            if (state.locationError == IntakeError.LOCATION_GPS_TIMEOUT) {
                                ActionButton(
                                    label = "Retry Location",
                                    onClick = { onAction(IntakeAction.RetryLocation) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    else -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colors.secondary,
                                modifier = Modifier.size(MaterialTheme.dimensions.iconSizeMedium)
                            )
                            Text(
                                "Getting location…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        state.surveillanceForm?.let { surveillanceForm ->
            item {
                IntakeTile(
                    title = "Surveillance Form",
                    iconPainter = painterResource(id = R.drawable.ic_clipboard),
                    iconDescription = "Surveillance Form Icon"
                ) {
                    ToggleField(
                        label = "Was IRS conducted in this household?",
                        checked = surveillanceForm.wasIrsConducted,
                        onCheckedChange = {
                            onAction(
                                IntakeAction.ToggleIrsConducted(
                                    it
                                )
                            )
                        })

                    if (surveillanceForm.wasIrsConducted) {
                        TextEntryField(
                            label = "Months Since IRS",
                            value = surveillanceForm.monthsSinceIrs?.let { if (it == 0) "" else it.toString() }
                                .orEmpty(),
                            onValueChange = {
                                onAction(IntakeAction.EnterMonthsSinceIrs(it.filter { character -> character.isDigit() }))
                            },
                            placeholder = "0",
                            singleLine = true,
                            keyboardType = KeyboardType.Number,
                        )
                    }

                    TextEntryField(
                        label = "Number of LLINs Available",
                        value = if (surveillanceForm.numLlinsAvailable == 0) "" else surveillanceForm.numLlinsAvailable.toString(),
                        onValueChange = { onAction(IntakeAction.EnterNumLlinsAvailable(it.filter { character -> character.isDigit() })) },
                        placeholder = "0",
                        singleLine = true,
                        keyboardType = KeyboardType.Number
                    )

                    surveillanceForm.llinType?.let { current ->
                        DropdownField(
                            label = "LLIN Type",
                            options = IntakeDropdownOptions.LlinTypeOption.entries,
                            selectedOption = IntakeDropdownOptions.LlinTypeOption.entries.firstOrNull { it.label == current },
                            onOptionSelected = {
                                onAction(IntakeAction.SelectLlinType(it))
                            },
                            error = state.intakeErrors.llinType,
                        ) { llinType ->
                            Text(
                                text = llinType.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colors.textPrimary
                            )
                        }
                    }

                    surveillanceForm.llinBrand?.let { current ->
                        DropdownField(
                            label = "LLIN Brand",
                            options = IntakeDropdownOptions.LlinBrandOption.entries,
                            selectedOption = IntakeDropdownOptions.LlinBrandOption.entries.firstOrNull { it.label == current },
                            onOptionSelected = {
                                onAction(IntakeAction.SelectLlinBrand(it))
                            },
                            error = state.intakeErrors.llinBrand,
                        ) { llinBrand ->
                            Text(
                                text = llinBrand.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colors.textPrimary
                            )
                        }
                    }

                    surveillanceForm.numPeopleSleptUnderLlin?.let { current ->
                        TextEntryField(
                            label = "Number of People who Slept Under LLIN",
                            value = if (current == 0) "" else current.toString(),
                            onValueChange = {
                                onAction(IntakeAction.EnterNumPeopleSleptUnderLlin(it.filter { character -> character.isDigit() }))
                            },
                            placeholder = "0",
                            singleLine = true,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }

        item {
            IntakeTile(
                title = "Additional Notes",
                iconPainter = painterResource(id = R.drawable.ic_notes),
                iconDescription = "Additional Notes Icon"
            ) {
                TextEntryField(
                    label = "Notes",
                    value = state.session.notes,
                    onValueChange = { onAction(IntakeAction.EnterNotes(it)) })
            }
        }

        item {
            ActionButton(
                label = "Continue",
                onClick = { onAction(IntakeAction.SubmitIntakeForm) },
                modifier = Modifier.padding(MaterialTheme.dimensions.paddingMedium)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SurveillanceFormScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            IntakeScreen(
                state = IntakeState(), onAction = { }, modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
