package com.vci.vectorcamapp.intake.domain.strategy

import com.vci.vectorcamapp.core.domain.model.SurveillanceForm

interface SurveillanceFormWorkflow {
    fun getSurveillanceForm(): SurveillanceForm?
}
