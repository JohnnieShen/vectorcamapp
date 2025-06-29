package com.vci.vectorcamapp.surveillance_form.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import com.vci.vectorcamapp.surveillance_form.location.data.toString
import com.vci.vectorcamapp.surveillance_form.domain.enums.CollectionMethodOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.DistrictOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.LlinBrandOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.LlinTypeOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.SentinelSiteOption
import com.vci.vectorcamapp.surveillance_form.domain.enums.SpecimenConditionOption
import com.vci.vectorcamapp.surveillance_form.location.data.LocationError
import com.vci.vectorcamapp.surveillance_form.presentation.components.DatePickerField
import com.vci.vectorcamapp.surveillance_form.presentation.components.DropdownField
import com.vci.vectorcamapp.surveillance_form.presentation.components.TextEntryField
import com.vci.vectorcamapp.surveillance_form.presentation.components.ToggleField
import com.vci.vectorcamapp.ui.theme.VectorcamappTheme

@Composable
fun SurveillanceFormScreen(
    state: SurveillanceFormState,
    onAction: (SurveillanceFormAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val verticalScrollState = rememberScrollState()

    BackHandler {
        onAction(SurveillanceFormAction.SaveSessionProgress)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(verticalScrollState)
    ) {

        TextEntryField(
            label = "Collector Title",
            value = state.session.collectorTitle,
            onValueChange = { onAction(SurveillanceFormAction.EnterCollectorTitle(it)) },
            singleLine = true,
            error = state.surveillanceFormErrors.collectorTitle,
            modifier = Modifier,
        )

        TextEntryField(
            label = "Collector Name",
            value = state.session.collectorName,
            onValueChange = { onAction(SurveillanceFormAction.EnterCollectorName(it)) },
            singleLine = true,
            error = state.surveillanceFormErrors.collectorName,
            modifier = Modifier,
        )

        DropdownField(
            label = "District",
            options = state.allSitesInProgram.map { DistrictOption(it.district) }
                .distinctBy { it.label },
            selectedOption = DistrictOption(state.selectedDistrict),
            onOptionSelected = { onAction(SurveillanceFormAction.SelectDistrict(it)) },
            error = state.surveillanceFormErrors.district,
            modifier = Modifier
        )

        if (state.selectedDistrict.isNotBlank()) {
            DropdownField(
                label = "Sentinel Site",
                options = state.allSitesInProgram.filter { it.district == state.selectedDistrict }
                    .map { SentinelSiteOption(it.sentinelSite) }.distinctBy { it.label },
                selectedOption = SentinelSiteOption(state.selectedSentinelSite),
                onOptionSelected = { onAction(SurveillanceFormAction.SelectSentinelSite(it)) },
                error = state.surveillanceFormErrors.sentinelSite,
                modifier = Modifier
            )
        }

        TextEntryField(
            label = "House Number",
            value = state.session.houseNumber,
            onValueChange = { onAction(SurveillanceFormAction.EnterHouseNumber(it)) },
            singleLine = true,
            error = state.surveillanceFormErrors.houseNumber,
            modifier = Modifier
        )

        TextEntryField(
            label = "Number of People who Slept in the House",
            value = state.surveillanceForm.numPeopleSleptInHouse.toString(),
            onValueChange = { onAction(SurveillanceFormAction.EnterNumPeopleSleptInHouse(it)) },
            singleLine = true,
            keyboardType = KeyboardType.Number,
            modifier = Modifier
        )

        ToggleField(
            label = "IRS Conducted in this Household",
            checked = state.surveillanceForm.wasIrsConducted,
            onCheckedChange = { onAction(SurveillanceFormAction.ToggleIrsConducted(it)) },
            modifier = Modifier,
        )

        state.surveillanceForm.monthsSinceIrs?.let {
            TextEntryField(
                label = "Months Since IRS",
                value = state.surveillanceForm.monthsSinceIrs.toString(),
                onValueChange = { onAction(SurveillanceFormAction.EnterMonthsSinceIrs(it)) },
                singleLine = true,
                keyboardType = KeyboardType.Number,
                modifier = Modifier
            )
        }

        TextEntryField(
            label = "Number of LLINs Available",
            value = state.surveillanceForm.numLlinsAvailable.toString(),
            onValueChange = { onAction(SurveillanceFormAction.EnterNumLlinsAvailable(it)) },
            singleLine = true,
            keyboardType = KeyboardType.Number,
            modifier = Modifier
        )

        state.surveillanceForm.llinType?.let {
            DropdownField(
                label = "LLIN Type",
                options = LlinTypeOption.entries,
                selectedOption = LlinTypeOption.entries.find { it.label == state.surveillanceForm.llinType },
                onOptionSelected = { onAction(SurveillanceFormAction.SelectLlinType(it)) },
                error = state.surveillanceFormErrors.llinType,
                modifier = Modifier
            )
        }

        state.surveillanceForm.llinBrand?.let {
            DropdownField(
                label = "LLIN Brand",
                options = LlinBrandOption.entries,
                selectedOption = LlinBrandOption.entries.find { it.label == state.surveillanceForm.llinBrand },
                onOptionSelected = { onAction(SurveillanceFormAction.SelectLlinBrand(it)) },
                error = state.surveillanceFormErrors.llinBrand,
                modifier = Modifier
            )
        }

        state.surveillanceForm.numPeopleSleptUnderLlin?.let {
            TextEntryField(
                label = "Number of People who Slept Under LLIN",
                value = state.surveillanceForm.numPeopleSleptUnderLlin.toString(),
                onValueChange = { onAction(SurveillanceFormAction.EnterNumPeopleSleptUnderLlin(it)) },
                singleLine = true,
                keyboardType = KeyboardType.Number,
                modifier = Modifier
            )
        }

        DatePickerField(
            label = "Collection Date",
            selectedDateInMillis = state.session.collectionDate,
            onDateSelected = { onAction(SurveillanceFormAction.PickCollectionDate(it)) },
            error = state.surveillanceFormErrors.collectionDate,
            modifier = Modifier
        )

        DropdownField(
            label = "Collection Method",
            options = CollectionMethodOption.entries,
            selectedOption = CollectionMethodOption.entries.find { it.label == state.session.collectionMethod },
            onOptionSelected = { onAction(SurveillanceFormAction.SelectCollectionMethod(it)) },
            error = state.surveillanceFormErrors.collectionMethod,
            modifier = Modifier
        )

        DropdownField(
            label = "Specimen Condition",
            options = SpecimenConditionOption.entries,
            selectedOption = SpecimenConditionOption.entries.find { it.label == state.session.specimenCondition },
            onOptionSelected = { onAction(SurveillanceFormAction.SelectSpecimenCondition(it)) },
            error = state.surveillanceFormErrors.specimenCondition,
            modifier = Modifier
        )

        TextEntryField(
            label = "Notes",
            value = state.session.notes,
            onValueChange = { onAction(SurveillanceFormAction.EnterNotes(it)) },
            modifier = Modifier
        )

        if (state.latitude != null && state.longitude != null) {
            Text("Latitude: ${state.latitude}", Modifier.padding(vertical = 4.dp))
            Text("Longitude: ${state.longitude}", Modifier.padding(vertical = 4.dp))
        } else if (state.locationError != null) {
            Text(
                "Could not get location: ${state.locationError.toString(context)}",
                Modifier.padding(vertical = 4.dp)
            )
            if(state.locationError == LocationError.GPS_TIMEOUT) {
                Button(onClick = {
                    onAction(SurveillanceFormAction.RetryLocation ) },
                    modifier = Modifier
                ) {
                    Text("Retry Location")
                }
            }
        } else {
            CircularProgressIndicator()
            Text("Getting location…", Modifier.padding(start = 8.dp))
        }

        Button(
            onClick = { onAction(SurveillanceFormAction.SubmitSurveillanceForm) },
            modifier = Modifier
        ) {
            Text("Start Imaging")
        }
    }
}

@PreviewLightDark
@Composable
fun SurveillanceFormScreenPreview() {
    VectorcamappTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SurveillanceFormScreen(
                state = SurveillanceFormState(),
                onAction = { },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
