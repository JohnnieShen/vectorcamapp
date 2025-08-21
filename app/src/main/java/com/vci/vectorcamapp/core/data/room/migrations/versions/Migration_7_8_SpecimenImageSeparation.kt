package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8_SPECIMEN_IMAGE_SEPARATION = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE `inference_result`")
        db.execSQL("DROP TABLE `specimen`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `specimen` (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                PRIMARY KEY(`id`, `sessionId`),
                FOREIGN KEY(`sessionId`) REFERENCES `session`(`localId`) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `specimen_image` (
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
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_specimen_image_specimenId_sessionId` ON `specimen_image` (`specimenId`, `sessionId`);")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `inference_result` (
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
            """.trimIndent()
        )
    }
}
