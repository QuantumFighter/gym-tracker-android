package com.quantumfighter.gymtracker.data.exercise

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

/**
 * Replaces SQLite's destructive default corruption callback while forwarding Room's lifecycle.
 * Does not close/checkpoint, delete, rename or log damaged storage or its sidecars.
 */
internal class PreservingOpenHelperFactory(
    private val delegate: SupportSQLiteOpenHelper.Factory = FrameworkSQLiteOpenHelperFactory(),
) : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        val roomCallback = configuration.callback
        val callback = object : SupportSQLiteOpenHelper.Callback(roomCallback.version) {
            override fun onConfigure(db: SupportSQLiteDatabase) = roomCallback.onConfigure(db)

            override fun onCreate(db: SupportSQLiteDatabase) = roomCallback.onCreate(db)

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = roomCallback.onUpgrade(db, oldVersion, newVersion)

            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = roomCallback.onDowngrade(db, oldVersion, newVersion)

            override fun onOpen(db: SupportSQLiteDatabase) = roomCallback.onOpen(db)

            override fun onCorruption(db: SupportSQLiteDatabase): Unit = throw PreservedDatabaseCorruptionException()
        }
        return delegate.create(
            SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
                .name(configuration.name)
                .callback(callback)
                .noBackupDirectory(configuration.useNoBackupDirectory)
                .allowDataLossOnRecovery(false)
                .build(),
        )
    }
}

/** Non-SQLite failure also prevents the helper's SQLiteException-based delete/retry recovery path. */
internal class PreservedDatabaseCorruptionException : IllegalStateException("Database corruption detected; automatic reset is disabled")
