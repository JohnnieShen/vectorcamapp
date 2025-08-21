package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4_SCHEMA_V2 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS program (
                `id` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `country` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_program_country ON program(`country`)
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS site (
                `id` INTEGER NOT NULL,
                `programId` INTEGER NOT NULL,
                `district` TEXT NOT NULL,
                `subCounty` TEXT NOT NULL,
                `parish` TEXT NOT NULL,
                `sentinelSite` TEXT NOT NULL,
                `healthCenter` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`programId`) REFERENCES `program`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_site_programId ON site(`programId`)
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS session")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS session (
                `localId` TEXT NOT NULL,
                `siteId` INTEGER NOT NULL,
                `remoteId` INTEGER,
                `houseNumber` TEXT NOT NULL,
                `collectorTitle` TEXT NOT NULL,
                `collectorName` TEXT NOT NULL,
                `collectionDate` INTEGER NOT NULL,
                `collectionMethod` TEXT NOT NULL,
                `specimenCondition` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `completedAt` INTEGER,
                `submittedAt` INTEGER,
                `notes` TEXT NOT NULL,
                PRIMARY KEY(`localId`),
                FOREIGN KEY(`siteId`) REFERENCES `site`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_session_submittedAt ON session(`submittedAt`)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_session_siteId ON session(`siteId`)
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS surveillance_form")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS surveillance_form (
                `sessionId` TEXT NOT NULL,
                `numPeopleSleptInHouse` INTEGER NOT NULL,
                `wasIrsConducted` INTEGER NOT NULL,
                `monthsSinceIrs` INTEGER,
                `numLlinsAvailable` INTEGER NOT NULL,
                `llinType` TEXT,
                `llinBrand` TEXT,
                `numPeopleSleptUnderLlin` INTEGER,
                PRIMARY KEY(`sessionId`),
                FOREIGN KEY(`sessionId`) REFERENCES `session`(`localId`) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("DROP TABLE IF EXISTS specimen")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS specimen (
                `id` TEXT NOT NULL,
                `sessionId` TEXT NOT NULL,
                `species` TEXT,
                `sex` TEXT,
                `abdomenStatus` TEXT,
                `imageUri` TEXT NOT NULL,
                `capturedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`sessionId`) REFERENCES `session`(`localId`) ON DELETE CASCADE ON UPDATE CASCADE
            )
            """.trimIndent()
        )
    }
}