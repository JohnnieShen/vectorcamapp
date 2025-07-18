package com.vci.vectorcamapp.core.presentation.components.form

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun ToggleField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.dimensions.paddingSmall)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.successConfirm,
                uncheckedThumbColor = MaterialTheme.colors.disabled,
                checkedTrackColor = MaterialTheme.colors.successConfirm.copy(alpha = 0.25f),
                uncheckedTrackColor = MaterialTheme.colors.disabled.copy(alpha = 0.25f),
                checkedBorderColor = MaterialTheme.colors.successConfirm,
                uncheckedBorderColor = MaterialTheme.colors.disabled
            )
        )
    }
}
