package com.vci.vectorcamapp.core.data.room.migrations

import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_1_2_CREATE_BOUNDING_BOX_TABLE
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_2_3_CREATE_SURVEILLANCE_FORM_TABLE
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_3_4_SCHEMA_V2
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_4_5_ADD_SUBMITTED_AT_COLUMNS_TO_SPECIMEN_AND_SURVEILLANCE_FORM_TABLES
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_5_6_ADD_UPLOAD_STATUS_TO_SPECIMEN

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2_CREATE_BOUNDING_BOX_TABLE,
    MIGRATION_2_3_CREATE_SURVEILLANCE_FORM_TABLE,
    MIGRATION_3_4_SCHEMA_V2,
    MIGRATION_4_5_ADD_SUBMITTED_AT_COLUMNS_TO_SPECIMEN_AND_SURVEILLANCE_FORM_TABLES,
    MIGRATION_5_6_ADD_UPLOAD_STATUS_TO_SPECIMEN
)