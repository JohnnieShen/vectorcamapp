package com.vci.vectorcamapp.complete_session.specimens.presentation

import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.Specimen
import java.util.UUID

data class CompleteSessionSpecimensState (
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
    val specimens: List<Specimen> = emptyList(),
)
