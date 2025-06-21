package com.vci.vectorcamapp.complete_session.list.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Site
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompleteSessionListCard(session: Session, site: Site, modifier: Modifier = Modifier) {

    val dateTimeFormatter = remember { SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()) }
    val createdAtDateTimeFormatted = dateTimeFormatter.format(session.createdAt)
    val completedAtDateTimeFormatted = session.completedAt?.let { dateTimeFormatter.format(it) }

    Card(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Session ID: ${session.localId}",
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
                text = "Site ID: ${site.id}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Parish: ${site.parish}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "District: ${site.district}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sub-County: ${site.subCounty}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sentinel Site: ${site.sentinelSite}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "House Number: ${session.houseNumber}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
