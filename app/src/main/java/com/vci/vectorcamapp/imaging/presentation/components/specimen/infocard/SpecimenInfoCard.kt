package com.vci.vectorcamapp.imaging.presentation.components.specimen.infocard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vci.vectorcamapp.core.domain.model.Specimen

@Composable
fun SpecimenInfoCard(
    specimen: Specimen,
    onSpecimenIdChanged: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Column {
            if (onSpecimenIdChanged != null) {
                OutlinedTextField(
                    value = specimen.id,
                    onValueChange = onSpecimenIdChanged,
                    label = { Text("Specimen ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                LabelRow("Specimen ID: ", specimen.id, modifier = Modifier)
            }

            specimen.species?.let {
                Spacer(modifier = Modifier.height(12.dp))

                LabelRow("Species: ", it, modifier = Modifier)
            }

            specimen.sex?.let {
                Spacer(modifier = Modifier.height(12.dp))

                LabelRow("Sex: ", it, modifier = Modifier)
            }

            specimen.abdomenStatus?.let {
                Spacer(modifier = Modifier.height(12.dp))

                LabelRow("Abdomen Status: ", it, modifier = Modifier)
            }
        }
    }
}