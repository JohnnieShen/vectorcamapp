package com.vci.vectorcamapp.core.data.room.migrations.versions

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9_ADD_SESSION_TYPE_COLUMN = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add the new type column with default value
        db.execSQL("ALTER TABLE `session` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'SURVEILLANCE'")

        // Drop old index on submittedAt
        db.execSQL("DROP INDEX `index_session_submittedAt`")

        // Create new indices
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_completedAt` ON `session` (`completedAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_type` ON `session` (`type`)")
    }
}