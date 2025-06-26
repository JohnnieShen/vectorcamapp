package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.room.entities.SurveillanceFormEntity
import com.vci.vectorcamapp.core.domain.model.SurveillanceForm
import java.util.UUID

fun SurveillanceFormEntity.toDomain() : SurveillanceForm {
    return SurveillanceForm(
        numPeopleSleptInHouse = this.numPeopleSleptInHouse,
        wasIrsConducted = this.wasIrsConducted,
        monthsSinceIrs = this.monthsSinceIrs,
        numLlinsAvailable = this.numLlinsAvailable,
        llinType = this.llinType,
        llinBrand = this.llinBrand,
        numPeopleSleptUnderLlin = this.numPeopleSleptUnderLlin,
        submittedAt = this.submittedAt
    )
}

fun SurveillanceForm.toEntity(sessionId: UUID) : SurveillanceFormEntity {
    return SurveillanceFormEntity(
        sessionId = sessionId,
        numPeopleSleptInHouse = this.numPeopleSleptInHouse,
        wasIrsConducted = this.wasIrsConducted,
        monthsSinceIrs = this.monthsSinceIrs,
        numLlinsAvailable = this.numLlinsAvailable,
        llinType = this.llinType,
        llinBrand = this.llinBrand,
        numPeopleSleptUnderLlin = this.numPeopleSleptUnderLlin,
        submittedAt = this.submittedAt
    )
}
