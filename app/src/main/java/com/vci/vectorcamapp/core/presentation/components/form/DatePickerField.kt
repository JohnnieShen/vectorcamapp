package com.vci.vectorcamapp.core.presentation.components.form

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.intake.domain.util.FormValidationError
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DatePickerField(
    label: String,
    selectedDateInMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    error: FormValidationError? = null
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()) }
    calendar.timeInMillis = selectedDateInMillis

    val formattedDate = remember(selectedDateInMillis) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDateInMillis)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )

        OutlinedTextField(
            value = formattedDate,
            onValueChange = { },
            isError = error != null,
            enabled = false,
            singleLine = true,
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = "Calendar"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            onDateSelected(calendar.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.maxDate = System.currentTimeMillis()
                    }.show()
                }
        )

        if (error != null) {
            Text(
                text = error.toString(context),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}
