package com.vci.vectorcamapp.core.presentation.components.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.intake.domain.util.FormValidationError
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions

@Composable
fun TextEntryField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: FormValidationError? = null,
    placeholder: String? = null
) {
    val context = LocalContext.current

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

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = error != null,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = {
                if (placeholder != null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colors.textSecondary,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colors.transparent,
                unfocusedBorderColor = MaterialTheme.colors.transparent,
                errorBorderColor = MaterialTheme.colors.transparent,
                cursorColor = MaterialTheme.colors.primary,
                selectionColors = TextSelectionColors(
                    handleColor = MaterialTheme.colors.primary,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.25f)
                )
            ),
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.cardBackground,
                    shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusSmall)
                )
                .border(
                    width = MaterialTheme.dimensions.borderThicknessThick,
                    shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusSmall),
                    color = if (error != null) MaterialTheme.colors.error else MaterialTheme.colors.primary
                )
                .heightIn(min = MaterialTheme.dimensions.componentHeightMedium)
        )

        if (error != null) {
            Text(
                text = error.toString(context),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.error
            )
        }
    }
}
