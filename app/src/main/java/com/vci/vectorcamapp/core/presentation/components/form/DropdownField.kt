package com.vci.vectorcamapp.core.presentation.components.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.intake.domain.enums.DropdownOption
import com.vci.vectorcamapp.intake.domain.util.FormValidationError
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.screenHeightFraction

@Composable
fun <T : DropdownOption> DropdownField(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    highlightBorder: Boolean = false,
    arrowAlwaysDown: Boolean = true,
    menuItemContent: @Composable (T) -> Unit = { Text(text = it.label) },
    error: FormValidationError? = null
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )

        BoxWithConstraints(
            modifier = modifier

        ) {
            val parentWidth = maxWidth
            val parentHeight = maxHeight

            val borderColor = when {
                error != null -> MaterialTheme.colors.error
                highlightBorder -> MaterialTheme.colors.primary
                else -> Color.Black
            }

            val borderThickness = if (error != null || highlightBorder)
                MaterialTheme.dimensions.borderThicknessThick
            else
                MaterialTheme.dimensions.borderThicknessThin

            OutlinedTextField(
                value = selectedOption?.label ?: "",
                onValueChange = { },
                placeholder = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = MaterialTheme.dimensions.paddingSmall)
                    )
                },
                shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusSmall),
                singleLine = true,
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colors.textPrimary,
                    disabledPlaceholderColor = MaterialTheme.colors.textSecondary,
                    disabledContainerColor = MaterialTheme.colors.cardBackground,
                    disabledTrailingIconColor = if (selectedOption == null) MaterialTheme.colors.textSecondary else MaterialTheme.colors.textPrimary
                ),
                trailingIcon = {
                    Box(modifier = Modifier.padding(end = MaterialTheme.dimensions.paddingLarge)) {
                        val showDown = if (arrowAlwaysDown) true else expanded
                        Icon(
                            imageVector = if (showDown) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand dropdown",
                            modifier = Modifier.size(MaterialTheme.dimensions.iconSizeLarge)
                        )
                    }
                },
                modifier = modifier
                    .fillMaxWidth()
                    .border(
                        width = borderThickness,
                        color = borderColor,
                        shape = RoundedCornerShape(
                            topStart = MaterialTheme.dimensions.cornerRadiusSmall,
                            topEnd = MaterialTheme.dimensions.cornerRadiusSmall,
                            bottomStart = MaterialTheme.dimensions.cornerRadiusSmall,
                            bottomEnd = MaterialTheme.dimensions.cornerRadiusSmall
                        )
                    )
                    .clickable { expanded = true })

            DropdownMenu(
                expanded = expanded,
                containerColor = MaterialTheme.colors.cardBackground,
                border = BorderStroke(
                    width = MaterialTheme.dimensions.borderThicknessThick,
                    color = MaterialTheme.colors.primary
                ),
                shape = RoundedCornerShape(
                    topStart = MaterialTheme.dimensions.cornerRadiusSmall,
                    topEnd = MaterialTheme.dimensions.cornerRadiusSmall,
                    bottomEnd = MaterialTheme.dimensions.cornerRadiusSmall,
                    bottomStart = MaterialTheme.dimensions.cornerRadiusSmall
                ),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(parentWidth)
                    .heightIn(max = screenHeightFraction(0.3f))
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { menuItemContent(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(parentHeight),
                    )
                    if (index != options.lastIndex) {
                        HorizontalDivider(
                            thickness = MaterialTheme.dimensions.dividerThickness,
                            color = MaterialTheme.colors.divider
                        )
                    }
                }
            }
        }

        if (error != null) {
            Text(
                text = error.toString(context),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}
