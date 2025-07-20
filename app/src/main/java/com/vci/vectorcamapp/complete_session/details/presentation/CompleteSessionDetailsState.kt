package com.vci.vectorcamapp.complete_session.details.presentation

import com.vci.vectorcamapp.complete_session.details.presentation.enums.CompleteSessionDetailsTab
import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Site
import com.vci.vectorcamapp.core.domain.model.Specimen
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
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
        longitude = null
    ),
    val site: Site = Site(
        id = -1,
        district = "",
        subCounty = "",
        parish = "",
        sentinelSite = "",
        healthCenter = ""
    ),
    val surveillanceForm: SurveillanceForm = SurveillanceForm(
        numPeopleSleptInHouse = 0,
        wasIrsConducted = false,
        monthsSinceIrs = null,
        numLlinsAvailable = 0,
        llinType = null,
        llinBrand = null,
        numPeopleSleptUnderLlin = null,
        submittedAt = null
    ),
    val specimens: List<Specimen> = emptyList(),
)
