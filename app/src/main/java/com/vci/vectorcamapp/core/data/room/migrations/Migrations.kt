package com.vci.vectorcamapp.core.data.room.migrations

import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_10_11_ADD_INFERENCE_DURATION_COLUMNS
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_1_2_CREATE_BOUNDING_BOX_TABLE
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_2_3_CREATE_SURVEILLANCE_FORM_TABLE
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_3_4_SCHEMA_V2
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_4_5_ADD_SUBMITTED_AT_COLUMNS_TO_SPECIMEN_AND_SURVEILLANCE_FORM_TABLES
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_5_6_ADD_UPLOAD_STATUS_TO_SPECIMEN
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_6_7_BOUNDING_BOX_TO_INFERENCE_RESULT
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_7_8_SPECIMEN_IMAGE_SEPARATION
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_8_9_ADD_SESSION_TYPE_COLUMN
import com.vci.vectorcamapp.core.data.room.migrations.versions.MIGRATION_9_10_REFACTOR_SPECIMEN_IMAGE_AND_INFERENCE_RESULT_KEYS

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2_CREATE_BOUNDING_BOX_TABLE,
    MIGRATION_2_3_CREATE_SURVEILLANCE_FORM_TABLE,
    MIGRATION_3_4_SCHEMA_V2,
    MIGRATION_4_5_ADD_SUBMITTED_AT_COLUMNS_TO_SPECIMEN_AND_SURVEILLANCE_FORM_TABLES,
    MIGRATION_5_6_ADD_UPLOAD_STATUS_TO_SPECIMEN,
    MIGRATION_6_7_BOUNDING_BOX_TO_INFERENCE_RESULT,
    MIGRATION_7_8_SPECIMEN_IMAGE_SEPARATION,
    MIGRATION_8_9_ADD_SESSION_TYPE_COLUMN,
    MIGRATION_9_10_REFACTOR_SPECIMEN_IMAGE_AND_INFERENCE_RESULT_KEYS,
    MIGRATION_10_11_ADD_INFERENCE_DURATION_COLUMNS
)
