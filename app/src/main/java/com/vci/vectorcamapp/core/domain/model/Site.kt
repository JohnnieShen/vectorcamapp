package com.vci.vectorcamapp.core.domain.model

data class Site(
    val id: Int,
    val district: String,
    val subCounty: String,
    val parish: String,
    val sentinelSite: String,
    val healthCenter: String,
)
