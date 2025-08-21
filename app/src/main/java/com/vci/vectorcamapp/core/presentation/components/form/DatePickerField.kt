package com.vci.vectorcamapp.core.presentation.components.form

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.util.Error
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DatePickerField(
    selectedDateInMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    error: Error? = null
) {
    val context = LocalContext.current

    val calendar = remember(selectedDateInMillis) {
        Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()).apply {
            timeInMillis = selectedDateInMillis
        }
    }

    val dateFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    val formattedDate = remember(selectedDateInMillis) {
        dateFormatter.format(selectedDateInMillis)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingExtraSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        label?.let {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colors.textSecondary,
            )
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.cardBackground,
                    shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusSmall)
                )
                .border(
                    width = MaterialTheme.dimensions.borderThicknessThick,
                    color = if (error != null) MaterialTheme.colors.error else MaterialTheme.colors.primary,
                    shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusSmall)
                )
                .heightIn(min = MaterialTheme.dimensions.componentHeightMedium)
                .clickable {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCalendar =
                                Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
                            newCalendar.set(Calendar.YEAR, year)
                            newCalendar.set(Calendar.MONTH, month)
                            newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            newCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            newCalendar.set(Calendar.MINUTE, 0)
                            newCalendar.set(Calendar.SECOND, 0)
                            newCalendar.set(Calendar.MILLISECOND, 0)

                            onDateSelected(newCalendar.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.maxDate = System.currentTimeMillis()
                    }.show()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimensions.paddingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colors.textPrimary
                )

                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = "Calendar",
                    tint = MaterialTheme.colors.icon,
                    modifier = Modifier.size(MaterialTheme.dimensions.iconSizeLarge)
                )
            }
        }

        if (error != null) {
            Text(
                text = error.toString(context),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.error
            )
        }
    }
}
