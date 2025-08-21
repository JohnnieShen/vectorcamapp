package com.vci.vectorcamapp.core.data.room.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.vci.vectorcamapp.core.data.room.entities.SessionEntity
import com.vci.vectorcamapp.core.data.room.entities.SiteEntity

data class SessionAndSiteRelation(
    @Embedded val session: SessionEntity,

    @Relation(
        parentColumn = "siteId",
        entityColumn = "id"
    )
    val site: SiteEntity,
)
