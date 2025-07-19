package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7_BOUNDING_BOX_TO_INFERENCE_RESULT = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `inference_result` (
                `specimenId` TEXT NOT NULL,
                `bboxTopLeftX` REAL NOT NULL,
                `bboxTopLeftY` REAL NOT NULL,
                `bboxWidth` REAL NOT NULL,
                `bboxHeight` REAL NOT NULL,
                `bboxConfidence` REAL NOT NULL,
                `bboxClassId` INTEGER NOT NULL,
                `speciesLogits` TEXT,
                `sexLogits` TEXT,
                `abdomenStatusLogits` TEXT,
                PRIMARY KEY(`specimenId`),
                FOREIGN KEY(`specimenId`) REFERENCES `specimen`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `bounding_box`")

        db.execSQL("ALTER TABLE `session` ADD COLUMN `latitude` REAL")
        db.execSQL("ALTER TABLE `session` ADD COLUMN `longitude` REAL")
    }
}