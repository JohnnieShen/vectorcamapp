package com.vci.vectorcamapp.core.data.dto.inference_result

import kotlinx.serialization.Serializable

@Serializable
data class InferenceResultDto(
    val bboxTopLeftX: Float = 0f,
    val bboxTopLeftY: Float = 0f,
    val bboxWidth: Float = 0f,
    val bboxHeight: Float = 0f,
    val speciesProbabilities: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val sexProbabilities: List<Float> = listOf(0f, 0f),
    val abdomenStatusProbabilities: List<Float> = listOf(0f, 0f, 0f),
)
