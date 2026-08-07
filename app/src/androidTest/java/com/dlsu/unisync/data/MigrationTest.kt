package com.dlsu.unisync.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Calendar
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Drives the real upgrade path on a device. This is the one piece of the app
// that can only fail on existing installs: if MIGRATION_2_3 is wrong, users who
// already have the v2 database crash on launch while a fresh install is fine.
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var database: UniSyncDatabase? = null

    @Before
    fun deleteExisting() {
        context.deleteDatabase(TEST_DB)
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrating_from_v2_drops_tasks_but_keeps_schedule_and_check_ins() {
        seedV2Database()

        val db = openWithMigrations()
        val readable = db.openHelper.readableDatabase

        // The tasks table is gone...
        readable.query("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks'").use {
            assertFalse("tasks table should be dropped by MIGRATION_2_3", it.moveToFirst())
        }

        // ...while the tables Room still owns kept their rows.
        readable.query("SELECT course FROM schedule_entries").use {
            assertTrue("schedule row should survive the migration", it.moveToFirst())
            assertEquals("MOBDEVE", it.getString(0))
        }
        readable.query("SELECT course, room, timestamp FROM check_ins").use {
            assertTrue("check-in row should survive the migration", it.moveToFirst())
            assertEquals("MOBDEVE", it.getString(0))
            assertEquals("Gokongwei 305", it.getString(1))
            assertEquals(1_000L, it.getLong(2))
        }
    }

    @Test
    fun migrating_from_v1_reaches_v3() {
        seedV1Database()

        val db = openWithMigrations()
        val readable = db.openHelper.readableDatabase

        readable.query("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks'").use {
            assertFalse("tasks table should not survive to v3", it.moveToFirst())
        }
        // MIGRATION_1_2 seeds the schedule for upgraders.
        readable.query("SELECT COUNT(*) FROM schedule_entries").use {
            assertTrue(it.moveToFirst())
            assertEquals(4, it.getInt(0))
        }
    }

    // The v4 upgrade has to read every existing row and give it structured days
    // and a start time. A row it cannot read keeps a mask of 0, which is what the
    // schedule screen flags -- the one case where losing the data silently would
    // take a class out of reminders without telling anyone.
    @Test
    fun migrating_to_v4_backfills_days_and_time_from_existing_text() {
        seedV3Database()

        val db = openWithMigrations()
        val readable = db.openHelper.readableDatabase

        readable.query("SELECT course, daysMask, startMinutes FROM schedule_entries ORDER BY id").use {
            assertTrue(it.moveToFirst())

            // "Mon/Wed • 1:00 PM" -> Monday and Wednesday bits, 13:00.
            assertEquals("MOBDEVE", it.getString(0))
            assertEquals((1 shl Calendar.MONDAY) or (1 shl Calendar.WEDNESDAY), it.getInt(1))
            assertEquals(13 * 60, it.getInt(2))

            // "Friday • 10:00 AM" -> Friday bit, 10:00.
            assertTrue(it.moveToNext())
            assertEquals("ST-MATH", it.getString(0))
            assertEquals(1 shl Calendar.FRIDAY, it.getInt(1))
            assertEquals(10 * 60, it.getInt(2))

            // "Asynchronous" names no day, so it stays unreadable and flagged.
            assertTrue(it.moveToNext())
            assertEquals("ELECTIVE", it.getString(0))
            assertEquals(0, it.getInt(1))
            assertTrue("a row with no time should stay null", it.isNull(2))
        }
    }

    // Opening through Room runs the migration chain and then validates that the
    // resulting schema matches the entities, so a bad migration fails here.
    private fun openWithMigrations(): UniSyncDatabase =
        Room.databaseBuilder(context, UniSyncDatabase::class.java, TEST_DB)
            .addMigrations(
                UniSyncDatabase.MIGRATION_1_2,
                UniSyncDatabase.MIGRATION_2_3,
                UniSyncDatabase.MIGRATION_3_4
            )
            .build()
            .also { database = it }

    private fun seedV1Database() = writeRawDatabase(version = 1) { db ->
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, " +
                "due TEXT NOT NULL, isDone INTEGER NOT NULL, createdAt INTEGER NOT NULL)"
        )
        db.execSQL("INSERT INTO tasks (title, due, isDone, createdAt) VALUES ('old', 'soon', 0, 1)")
    }

    private fun seedV2Database() = writeRawDatabase(version = 2) { db ->
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, " +
                "due TEXT NOT NULL, isDone INTEGER NOT NULL, createdAt INTEGER NOT NULL, dueAt INTEGER)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS check_ins (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "course TEXT NOT NULL, room TEXT NOT NULL, timestamp INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS schedule_entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "course TEXT NOT NULL, schedule TEXT NOT NULL, room TEXT NOT NULL)"
        )
        db.execSQL("INSERT INTO tasks (title, due, isDone, createdAt) VALUES ('old', 'soon', 0, 1)")
        db.execSQL("INSERT INTO schedule_entries (course, schedule, room) VALUES ('MOBDEVE', 'Mon/Wed', 'G305')")
        db.execSQL("INSERT INTO check_ins (course, room, timestamp) VALUES ('MOBDEVE', 'Gokongwei 305', 1000)")
    }

    private fun seedV3Database() = writeRawDatabase(version = 3) { db ->
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS check_ins (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "course TEXT NOT NULL, room TEXT NOT NULL, timestamp INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS schedule_entries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "course TEXT NOT NULL, schedule TEXT NOT NULL, room TEXT NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO schedule_entries (course, schedule, room) VALUES " +
                "('MOBDEVE', 'Mon/Wed • 1:00 PM', 'Gokongwei 305')," +
                "('ST-MATH', 'Friday • 10:00 AM', 'Andrew 1404')," +
                "('ELECTIVE', 'Asynchronous', 'Online')"
        )
    }

    // Builds the pre-upgrade file with plain SQLite so the test does not depend
    // on exported schema JSON that predates this change.
    private fun writeRawDatabase(version: Int, populate: (SQLiteDatabase) -> Unit) {
        val file = context.getDatabasePath(TEST_DB)
        file.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        populate(db)
        db.version = version
        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
