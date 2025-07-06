package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6_ADD_UPLOAD_STATUS_TO_SPECIMEN = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `specimen` ADD COLUMN `uploadStatus` TEXT NOT NULL DEFAULT 'NOT_STARTED'"
        )
    }
}
