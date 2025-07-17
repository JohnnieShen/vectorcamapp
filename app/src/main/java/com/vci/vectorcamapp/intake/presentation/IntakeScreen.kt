package com.vci.vectorcamapp.intake.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.vci.vectorcamapp.core.presentation.components.ui.ScreenHeader
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.intake.domain.enums.CollectionMethodOption
import com.vci.vectorcamapp.intake.domain.enums.DistrictOption
import com.vci.vectorcamapp.intake.domain.enums.LlinBrandOption
import com.vci.vectorcamapp.intake.domain.enums.LlinTypeOption
import com.vci.vectorcamapp.intake.domain.enums.SentinelSiteOption
import com.vci.vectorcamapp.intake.domain.enums.SpecimenConditionOption
import com.vci.vectorcamapp.intake.domain.util.IntakeError
import com.vci.vectorcamapp.intake.presentation.components.DatePickerField
import com.vci.vectorcamapp.intake.presentation.components.DropdownField
import com.vci.vectorcamapp.intake.presentation.components.SectionCard
import com.vci.vectorcamapp.intake.presentation.components.TextEntryField
import com.vci.vectorcamapp.intake.presentation.components.ToggleField
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.LocalColors
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun IntakeScreen(
    state: IntakeState,
    onAction: (IntakeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onAction(IntakeAction.ReturnToLandingScreen)
    }

    ScreenHeader(
        title = "Session Intake",
        subtitle = "Fill out the information below",
        modifier = modifier
    ) {
        item {
            SectionCard(sectionTitle = "General Information") {
                TextEntryField(
                    label = "Collector Name",
                    value = state.session.collectorName,
                    onValueChange = { onAction(IntakeAction.EnterCollectorName(it)) },
                    singleLine = true,
                    error = state.intakeErrors.collectorName
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                TextEntryField(
                    label = "Collector Title",
                    value = state.session.collectorTitle,
                    onValueChange = { onAction(IntakeAction.EnterCollectorTitle(it)) },
                    singleLine = true,
                    error = state.intakeErrors.collectorTitle
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                DatePickerField(
                    label = "Collection Date",
                    selectedDateInMillis = state.session.collectionDate,
                    onDateSelected = { onAction(IntakeAction.PickCollectionDate(it)) },
                    error = state.intakeErrors.collectionDate,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                DropdownField(
                    label = "Collection Method",
                    options = CollectionMethodOption.entries,
                    selectedOption = CollectionMethodOption.entries
                        .firstOrNull { it.label == state.session.collectionMethod },
                    onOptionSelected = {
                        onAction(
                            IntakeAction.SelectCollectionMethod(
                                it
                            )
                        )
                    },
                    error = state.intakeErrors.collectionMethod,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                DropdownField(
                    label = "Specimen Condition",
                    options = SpecimenConditionOption.entries,
                    selectedOption = SpecimenConditionOption.entries
                        .firstOrNull { it.label == state.session.specimenCondition },
                    onOptionSelected = {
                        onAction(
                            IntakeAction.SelectSpecimenCondition(
                                it
                            )
                        )
                    },
                    error = state.intakeErrors.specimenCondition,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            SectionCard(sectionTitle = "Geographical Information") {

                DropdownField(
                    label = "District",
                    options = state.allSitesInProgram
                        .map { DistrictOption(it.district) }
                        .distinctBy { it.label },
                    selectedOption = DistrictOption(state.selectedDistrict),
                    onOptionSelected = { onAction(IntakeAction.SelectDistrict(it)) },
                    error = state.intakeErrors.district
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                if (state.selectedDistrict.isNotBlank()) {
                    DropdownField(
                        label = "Sentinel Site",
                        options = state.allSitesInProgram
                            .filter { it.district == state.selectedDistrict }
                            .map { SentinelSiteOption(it.sentinelSite) }
                            .distinctBy { it.label },
                        selectedOption = SentinelSiteOption(state.selectedSentinelSite),
                        onOptionSelected = {
                            onAction(IntakeAction.SelectSentinelSite(it))
                        },
                        error = state.intakeErrors.sentinelSite
                    )
                    Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))
                }

                TextEntryField(
                    label = "House Number",
                    value = state.session.houseNumber,
                    onValueChange = { onAction(IntakeAction.EnterHouseNumber(it)) },
                    singleLine = true,
                    error = state.intakeErrors.houseNumber
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                TextEntryField(
                    label = "Number of House Occupants",
                    value = if (state.surveillanceForm.numPeopleSleptInHouse == 0) "" else state.surveillanceForm.numPeopleSleptInHouse.toString(),
                    onValueChange = {
                        onAction(IntakeAction.EnterNumPeopleSleptInHouse(it.filter { character -> character.isDigit() }))
                    },
                    placeholder = "0",
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                when {
                    state.latitude != null && state.longitude != null -> {
                        Text("Latitude: ${state.latitude}")
                        Spacer(Modifier.height(MaterialTheme.dimensions.spacingSmall))
                        Text("Longitude: ${state.longitude}")
                    }

                    state.locationError != null -> {
                        val context = LocalContext.current
                        Text(
                            text = "Could not get location: " +
                                    state.locationError.toString(context)
                        )
                        if (state.locationError == IntakeError.LOCATION_GPS_TIMEOUT) {
                            Spacer(Modifier.height(MaterialTheme.dimensions.spacingSmall))

                            val colors = LocalColors.current
                            Button(
                                onClick = { onAction(IntakeAction.RetryLocation) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                listOf(
                                                    colors.buttonGradientLeft,
                                                    colors.buttonGradientRight
                                                )
                                            ),
                                            shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
                                        )
                                        .padding(
                                            horizontal = MaterialTheme.dimensions.paddingMedium,
                                            vertical = MaterialTheme.dimensions.paddingSmall
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Retry Location",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(MaterialTheme.dimensions.spacingSmall))
                            Text("Getting location…")
                        }
                    }
                }
            }
        }

        item {
            SectionCard(sectionTitle = "Surveillance Form") {
                TextEntryField(
                    label = "Number of LLINs Available",
                    value = if (state.surveillanceForm.numLlinsAvailable == 0) "" else state.surveillanceForm.numLlinsAvailable.toString(),
                    onValueChange = {
                        onAction(IntakeAction.EnterNumLlinsAvailable(it.filter { character -> character.isDigit() }))
                    },
                    placeholder = "0",
                    singleLine = true,
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                state.surveillanceForm.llinType?.let { current ->
                    DropdownField(
                        label = "LLIN Type",
                        options = LlinTypeOption.entries,
                        selectedOption = LlinTypeOption.entries.firstOrNull { it.label == current },
                        onOptionSelected = {
                            onAction(IntakeAction.SelectLlinType(it))
                        },
                        error = state.intakeErrors.llinType
                    )
                    Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))
                }

                state.surveillanceForm.llinBrand?.let { current ->
                    DropdownField(
                        label = "LLIN Brand",
                        options = LlinBrandOption.entries,
                        selectedOption = LlinBrandOption.entries.firstOrNull { it.label == current },
                        onOptionSelected = {
                            onAction(IntakeAction.SelectLlinBrand(it))
                        },
                        error = state.intakeErrors.llinBrand
                    )
                    Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))
                }

                state.surveillanceForm.numPeopleSleptUnderLlin?.let { current ->
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
                    Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))
                }

                ToggleField(
                    label = "Was IRS conducted in this household?",
                    checked = state.surveillanceForm.wasIrsConducted,
                    onCheckedChange = {
                        onAction(
                            IntakeAction.ToggleIrsConducted(
                                it
                            )
                        )
                    }
                )
                Spacer(Modifier.height(MaterialTheme.dimensions.spacingMedium))

                if (state.surveillanceForm.wasIrsConducted) {
                    TextEntryField(
                        label = "Months Since IRS",
                        value = state.surveillanceForm.monthsSinceIrs?.let { if (it == 0) "" else it.toString() }.orEmpty(),
                        onValueChange = {
                            onAction(IntakeAction.EnterMonthsSinceIrs(it.filter { character -> character.isDigit() }))
                        },
                        placeholder = "0",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                    )
                }
            }
        }

        item {
            SectionCard(sectionTitle = "Optional") {
                TextEntryField(
                    label = "Notes",
                    value = state.session.notes,
                    onValueChange = { onAction(IntakeAction.EnterNotes(it)) }
                )
            }
        }

        item {
            val colors = LocalColors.current
            Button(
                onClick = { onAction(IntakeAction.SubmitIntakeForm) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.dimensions.paddingLarge)
                    .height(MaterialTheme.dimensions.componentHeightLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    colors.buttonGradientLeft,
                                    colors.buttonGradientRight
                                )
                            ),
                            shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusMedium)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Confirm",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun SurveillanceFormScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            IntakeScreen(
                state = IntakeState(),
                onAction = { },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
