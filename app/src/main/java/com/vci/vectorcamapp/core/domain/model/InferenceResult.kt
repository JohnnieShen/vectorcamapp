package com.vci.vectorcamapp.core.domain.model

data class InferenceResult(
    val bboxTopLeftX: Float,
    val bboxTopLeftY: Float,
    val bboxWidth: Float,
    val bboxHeight: Float,
    val bboxConfidence: Float,
    val bboxClassId: Int,
    val speciesLogits: List<Float>?,
    val sexLogits: List<Float>?,
    val abdomenStatusLogits: List<Float>?,
)
