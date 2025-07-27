package com.vci.vectorcamapp.imaging.domain.strategy

import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
import com.vci.vectorcamapp.imaging.domain.strategy.concrete.DataCollectionImagingWorkflow
import com.vci.vectorcamapp.imaging.domain.strategy.concrete.SurveillanceImagingWorkflow
import javax.inject.Inject

class ImagingWorkflowFactory @Inject constructor(
    private val inferenceRepository: InferenceRepository
) {
    fun create(sessionType: SessionType): ImagingWorkflow {
        return when (sessionType) {
            SessionType.SURVEILLANCE -> SurveillanceImagingWorkflow(inferenceRepository)
            SessionType.DATA_COLLECTION -> DataCollectionImagingWorkflow(inferenceRepository)
        }
    }
}
