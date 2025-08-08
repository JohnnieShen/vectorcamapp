package com.vci.vectorcamapp.core.domain.model.results

data class ClassifierResult(
    val logits: List<Float>?,
    val inferenceDuration: Long?
)
