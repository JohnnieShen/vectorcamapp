package com.vci.vectorcamapp.core.data.dto.site

import kotlinx.serialization.Serializable

@Serializable
data class SiteDto(
    val id: Int,
    val programId: Int,
    val district: String,
    val subCounty: String,
    val parish: String,
    val sentinelSite: String,
    val healthCenter: String
)