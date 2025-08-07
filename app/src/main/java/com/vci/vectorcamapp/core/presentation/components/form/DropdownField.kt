package com.vci.vectorcamapp.core.presentation.components.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.util.Error
import com.vci.vectorcamapp.core.presentation.util.error.toString
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.extensions.dimensions
import com.vci.vectorcamapp.ui.theme.screenHeightFraction

@Composable
fun <T> DropdownField(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    error: Error? = null,
    menuTestTag: String? = null,
    menuItemTestTagPrefix: String? = null,
    itemContent: @Composable (T) -> Unit,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

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

        BoxWithConstraints(modifier = modifier) {
            val parentWidth = maxWidth
            val parentHeight = maxHeight

            Box(
                modifier = Modifier
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
                    .clickable { expanded = true }
                    .testTag(menuTestTag ?: "dropdown-menu")) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.dimensions.paddingMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = MaterialTheme.dimensions.paddingSmall)
                    ) {
                        if (selectedOption != null) {
                            itemContent(selectedOption)
                        } else {
                            Text(
                                text = label ?: "Select",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colors.textSecondary
                            )
                        }
                    }

                    Box(modifier = Modifier.padding(start = MaterialTheme.dimensions.paddingLarge)) {
                        Icon(
                            painter = painterResource(if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                            contentDescription = if (expanded) "Collapse dropdown" else "Expand dropdown",
                            tint = if (selectedOption == null) MaterialTheme.colors.textSecondary else MaterialTheme.colors.textPrimary,
                            modifier = Modifier.size(MaterialTheme.dimensions.iconSizeLarge)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                containerColor = MaterialTheme.colors.cardBackground,
                border = BorderStroke(
                    width = MaterialTheme.dimensions.borderThicknessThick,
                    color = MaterialTheme.colors.primary
                ),
                shape = RoundedCornerShape(MaterialTheme.dimensions.cornerRadiusSmall),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(parentWidth)
                    .heightIn(max = screenHeightFraction(0.3f))
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { itemContent(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(parentHeight)
                            .testTag(if (menuItemTestTagPrefix != null) "${menuItemTestTagPrefix}-$index" else "dropdown-menu-item-$index"),
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.error
            )
        }
    }
}
