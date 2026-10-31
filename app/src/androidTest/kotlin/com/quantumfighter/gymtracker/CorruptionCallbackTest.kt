package com.quantumfighter.gymtracker

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.PreservedDatabaseCorruptionException
import com.quantumfighter.gymtracker.data.exercise.PreservingOpenHelperFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.reflect.Proxy

/** T-11: exercise the actual installed callback, including sidecars that default handling deletes. */
@RunWith(AndroidJUnit4::class)
class CorruptionCallbackTest {
    @Test
    fun corruptionCallbackPreservesMainAndSidecarBytesAndDisablesLossyRecovery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "b02-corruption-callback-test.db"
        context.deleteDatabase(name)
        lateinit var installed: SupportSQLiteOpenHelper.Configuration
        val factory = PreservingOpenHelperFactory(object : SupportSQLiteOpenHelper.Factory {
            override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
                installed = configuration
                return FrameworkSQLiteOpenHelperFactory().create(configuration)
            }
        })
        val original = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) = Unit
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            override fun onCorruption(db: SupportSQLiteDatabase): Unit = error("Must not invoke destructive callback")
        }
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(original)
                .allowDataLossOnRecovery(true).build(),
        )
        val files = listOf("", "-wal", "-shm", "-journal").associate { suffix ->
            File(context.getDatabasePath(name).path + suffix) to "Preserve fixture $suffix".toByteArray()
        }
        try {
            files.forEach { (file, bytes) ->
                file.parentFile!!.mkdirs()
                file.writeBytes(bytes)
            }
            // Any database call, including close/checkpoint, is an error in this handler.
            val untouchedDatabase = Proxy.newProxyInstance(
                SupportSQLiteDatabase::class.java.classLoader,
                arrayOf(SupportSQLiteDatabase::class.java),
            ) { _, method, _ -> error("Corruption callback must not call ${method.name}") } as SupportSQLiteDatabase
            assertFalse(installed.allowDataLossOnRecovery)
            assertThrows(PreservedDatabaseCorruptionException::class.java) {
                installed.callback.onCorruption(untouchedDatabase)
            }
            files.forEach { (file, bytes) ->
                assertTrue("Preserved ${file.name}", file.isFile)
                assertArrayEquals(bytes, file.readBytes())
            }
        } finally {
            helper.close()
            context.deleteDatabase(name)
        }
    }
}
