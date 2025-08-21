package com.vci.vectorcamapp.intake.domain.strategy.concrete

import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.intake.domain.strategy.SurveillanceFormWorkflow

class SurveillanceFormAbsentWorkflow : SurveillanceFormWorkflow {
    override fun getSurveillanceForm(): SurveillanceForm? = null
}
