package com.vci.vectorcamapp.complete_session.form.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompleteSessionFormScreen(
    state: CompleteSessionFormState,
    modifier: Modifier = Modifier
) {
    val dateTimeFormatter = remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val createdAtDateTimeFormatted = dateTimeFormatter.format(state.session.createdAt)
    val collectionDateFormatted = dateFormatter.format(Date(state.session.collectionDate))
    val submittedAtDateTimeFormatted = state.session.submittedAt?.let { dateTimeFormatter.format(it) }
    val completedAtDateTimeFormatted = state.session.completedAt?.let { dateTimeFormatter.format(it) }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Session ID: ${state.session.localId}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Created At: $createdAtDateTimeFormatted",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Completed At: $completedAtDateTimeFormatted",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Submitted At: $submittedAtDateTimeFormatted",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Site ID: ${state.site.id}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sentinel Site: ${state.site.sentinelSite}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Health Center: ${state.site.healthCenter}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "District: ${state.site.district}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sub-County: ${state.site.subCounty}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Parish: ${state.site.parish}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Collector: ${state.session.collectorName} (${state.session.collectorTitle})",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Collection Date: $collectionDateFormatted",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Collection Method: ${state.session.collectionMethod}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Specimen Condition: ${state.session.specimenCondition}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Notes: ${state.session.notes}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Number of People Slept in House: ${state.surveillanceForm?.numPeopleSleptInHouse}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "IRS Conducted: ${state.surveillanceForm?.wasIrsConducted}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Months Since IRS: ${state.surveillanceForm?.monthsSinceIrs}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Number of LLINs Available: ${state.surveillanceForm?.numLlinsAvailable}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "LLIN Type: ${state.surveillanceForm?.llinType}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "LLIN Brand: ${state.surveillanceForm?.llinBrand}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Number of People Slept Under LLIN: ${state.surveillanceForm?.numPeopleSleptUnderLlin}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
