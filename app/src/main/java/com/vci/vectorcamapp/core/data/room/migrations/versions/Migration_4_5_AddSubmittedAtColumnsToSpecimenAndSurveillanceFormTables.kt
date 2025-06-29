package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5_ADD_SUBMITTED_AT_COLUMNS_TO_SPECIMEN_AND_SURVEILLANCE_FORM_TABLES = object: Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `surveillance_form` ADD COLUMN `submittedAt` INTEGER")

        db.execSQL("ALTER TABLE `specimen` ADD COLUMN `submittedAt` INTEGER")
    }
}
