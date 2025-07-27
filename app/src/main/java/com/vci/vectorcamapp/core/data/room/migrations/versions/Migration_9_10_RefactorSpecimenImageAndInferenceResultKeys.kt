package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10_REFACTOR_SPECIMEN_IMAGE_AND_INFERENCE_RESULT_KEYS = object: Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add remoteId to specimen
        db.execSQL("ALTER TABLE `specimen` ADD COLUMN `remoteId` INTEGER")

        // Drop and recreate specimen_image table with localId as TEXT
        db.execSQL("DROP TABLE IF EXISTS `specimen_image`")
        db.execSQL("""
            CREATE TABLE `specimen_image` (
                `localId` TEXT NOT NULL,
                `specimenId` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `remoteId` INTEGER,
                `species` TEXT,
                `sex` TEXT,
                `abdomenStatus` TEXT,
                `imageUri` TEXT NOT NULL,
                `metadataUploadStatus` TEXT NOT NULL,
                `imageUploadStatus` TEXT NOT NULL,
                `capturedAt` INTEGER NOT NULL,
                `submittedAt` INTEGER,
                PRIMARY KEY(`localId`),
                FOREIGN KEY(`specimenId`, `sessionId`) REFERENCES `specimen`(`id`, `sessionId`) ON DELETE CASCADE ON UPDATE CASCADE
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_specimen_image_specimenId_sessionId` ON `specimen_image` (`specimenId`, `sessionId`)")

        // Drop and recreate inference_result with logits as TEXT (float array)
        db.execSQL("DROP TABLE IF EXISTS `inference_result`")
        db.execSQL("""
            CREATE TABLE `inference_result` (
                `specimenImageId` TEXT NOT NULL,
                `bboxTopLeftX` REAL NOT NULL,
                `bboxTopLeftY` REAL NOT NULL,
                `bboxWidth` REAL NOT NULL,
                `bboxHeight` REAL NOT NULL,
                `bboxConfidence` REAL NOT NULL,
                `bboxClassId` INTEGER NOT NULL,
                `speciesLogits` TEXT,
                `sexLogits` TEXT,
                `abdomenStatusLogits` TEXT,
                PRIMARY KEY(`specimenImageId`),
                FOREIGN KEY(`specimenImageId`) REFERENCES `specimen_image`(`localId`) ON DELETE CASCADE ON UPDATE CASCADE
            )
        """.trimIndent())
    }
}
