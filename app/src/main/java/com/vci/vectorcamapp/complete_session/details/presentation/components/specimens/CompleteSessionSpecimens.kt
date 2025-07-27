package com.vci.vectorcamapp.complete_session.details.presentation.components.specimens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenWithSpecimenImagesAndInferenceResults
import com.vci.vectorcamapp.ui.extensions.colors
import com.vci.vectorcamapp.ui.theme.screenWidthFraction

@Composable
fun CompleteSessionSpecimens(
    session: Session,
    specimensWithImagesAndInferenceResults: List<SpecimenWithSpecimenImagesAndInferenceResults>,
    modifier: Modifier = Modifier
) {
    if (specimensWithImagesAndInferenceResults.isEmpty()) {
        Text(
            "No specimens were captured during this session.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyRow(
            modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center
        ) {
            items(items = specimensWithImagesAndInferenceResults.asReversed()) { specimenWithSpecimenImagesAndInferenceResults ->
                Row {
                    val specimen = specimenWithSpecimenImagesAndInferenceResults.specimen
                    specimenWithSpecimenImagesAndInferenceResults.specimenImagesAndInferenceResults.map { (specimenImage, _) ->
                        CompleteSessionSpecimensTile(
                            session = session,
                            specimen = specimen,
                            specimenImage = specimenImage,
                            modifier = Modifier.width(
                                screenWidthFraction(0.9f)
                            )
                        )
                    }
                }
            }
        }
    }
}
