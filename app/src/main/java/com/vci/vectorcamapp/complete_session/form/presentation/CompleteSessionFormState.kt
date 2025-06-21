package com.vci.vectorcamapp.complete_session.form.presentation

import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Site
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import java.util.UUID

data class CompleteSessionFormState (
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
        notes = ""
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
    val error: String? = null
)
