package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11_ADD_INFERENCE_DURATION_COLUMNS = object: Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add the three new inference duration columns to the existing inference_result table
        db.execSQL("ALTER TABLE `inference_result` ADD COLUMN `speciesInferenceDuration` INTEGER")
        db.execSQL("ALTER TABLE `inference_result` ADD COLUMN `sexInferenceDuration` INTEGER")
        db.execSQL("ALTER TABLE `inference_result` ADD COLUMN `abdomenStatusInferenceDuration` INTEGER")
    }
}
