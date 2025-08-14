package com.vci.vectorcamapp.core.logging

data class CrashyContext(
    val screen: String? = null,
    val feature: String? = null,
    val action: String? = null,
    val sessionId: String? = null,
    val programId: String? = null,
    val siteId: String? = null,
    val specimenId: String? = null
)
