package com.vci.vectorcamapp.complete_session.details.presentation

import com.vci.vectorcamapp.complete_session.details.presentation.enums.CompleteSessionDetailsTab
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Site
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenImageAndInferenceResult
import com.vci.vectorcamapp.core.domain.model.composites.SpecimenWithSpecimenImagesAndInferenceResults
import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import java.util.UUID

data class CompleteSessionDetailsState(
    val selectedTab: CompleteSessionDetailsTab = CompleteSessionDetailsTab.SESSION_FORM,
    val session: Session = Session(
        localId = UUID(0, 0),
        remoteId = null,
        houseNumber = "",
        collectorTitle = "",
        collectorName = "",
        collectionDate = 0L,
        collectionMethod = "",
        specimenCondition = "",
        createdAt = 0L,
        completedAt = null,
        submittedAt = null,
        notes = "",
        latitude = null,
        longitude = null,
        type = SessionType.SURVEILLANCE
    ),
    val site: Site = Site(
        id = -1,
        district = "",
        subCounty = "",
        parish = "",
        sentinelSite = "",
        healthCenter = ""
    ),
    val surveillanceForm: SurveillanceForm? = null,
    val specimensWithImagesAndInferenceResults: List<SpecimenWithSpecimenImagesAndInferenceResults> = emptyList(),
)
