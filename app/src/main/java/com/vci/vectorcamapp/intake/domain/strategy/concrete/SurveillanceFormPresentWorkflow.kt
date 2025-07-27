package com.vci.vectorcamapp.intake.domain.strategy.concrete

import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import com.vci.vectorcamapp.intake.domain.strategy.SurveillanceFormWorkflow

class SurveillanceFormPresentWorkflow : SurveillanceFormWorkflow {
    override fun getSurveillanceForm(): SurveillanceForm? {
        return SurveillanceForm(
            numPeopleSleptInHouse = 0,
            wasIrsConducted = false,
            monthsSinceIrs = null,
            numLlinsAvailable = 0,
            llinType = null,
            llinBrand = null,
            numPeopleSleptUnderLlin = null,
            submittedAt = null
        )
    }
}
