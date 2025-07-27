package com.vci.vectorcamapp.core.domain.model.composites

import com.vci.vectorcamapp.core.domain.model.Session
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm

data class SessionAndSurveillanceForm(
    val session: Session,
    val surveillanceForm: SurveillanceForm?
)