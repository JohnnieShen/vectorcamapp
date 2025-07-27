package com.vci.vectorcamapp.intake.domain.strategy

import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import com.vci.vectorcamapp.intake.domain.strategy.concrete.SurveillanceFormAbsentWorkflow
import com.vci.vectorcamapp.intake.domain.strategy.concrete.SurveillanceFormPresentWorkflow
import javax.inject.Inject

class SurveillanceFormWorkflowFactory @Inject constructor() {
    fun create(sessionType: SessionType): SurveillanceFormWorkflow {
        return when (sessionType) {
            SessionType.SURVEILLANCE -> SurveillanceFormPresentWorkflow()
            SessionType.DATA_COLLECTION -> SurveillanceFormAbsentWorkflow()
        }
    }
}
