package com.dlsu.unisync.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlsu.unisync.models.CheckIn
import com.dlsu.unisync.models.LegacyScheduleEntry
import com.dlsu.unisync.util.ScheduleDays
import com.dlsu.unisync.util.ScheduleParser

// Local store for attendance history, plus the retired schedule table.
//
// Tasks and now the class schedule live in Firestore so they follow the account
// rather than the handset; schedule_entries survives only until ScheduleUploader
// has handed its rows over. Check-ins stay device-local on purpose.
@Database(
    entities = [CheckIn::class, LegacyScheduleEntry::class],
    version = 5,
    exportSchema = true
)
abstract class UniSyncDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao

    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var instance: UniSyncDatabase? = null

        fun getInstance(context: Context): UniSyncDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    UniSyncDatabase::class.java,
                    "unisync.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }

        // v1 -> v2: structured due date on tasks, plus check-in history and the
        // editable schedule. This used to seed sample classes as well; it no
        // longer does, so a v1 database upgrades into an empty schedule.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN dueAt INTEGER")
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
            }
        }

        // v2 -> v3: tasks now live in Firestore, so the local table is dropped.
        // Any tasks created before this upgrade are not carried over.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS tasks")
            }
        }

        // v3 -> v4: schedules become structured (day mask + start time) instead of
        // only free text. Existing rows are backfilled by parsing the text they
        // already hold, so a schedule typed before this upgrade keeps working;
        // anything unparseable keeps a mask of 0 and is flagged in the UI rather
        // than silently dropping out of reminders.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE schedule_entries ADD COLUMN daysMask INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE schedule_entries ADD COLUMN startMinutes INTEGER")
                backfillSchedules(db)
            }
        }

        private fun backfillSchedules(db: SupportSQLiteDatabase) {
            val rows = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, schedule FROM schedule_entries").use { cursor ->
                while (cursor.moveToNext()) {
                    rows += cursor.getLong(0) to cursor.getString(1)
                }
            }
            rows.forEach { (id, text) ->
                val mask = ScheduleDays.maskOf(ScheduleParser.daysOf(text))
                val start = ScheduleParser.startMinutes(text)
                db.execSQL(
                    "UPDATE schedule_entries SET daysMask = ?, startMinutes = ? WHERE id = ?",
                    arrayOf<Any?>(mask, start, id)
                )
            }
        }

        // v4 -> v5: the app used to seed four sample classes on first launch, and
        // they were indistinguishable from real ones -- they drove the next-class
        // card, the Today list and notifications for someone who had entered
        // nothing. The seeding is gone; this clears what it left behind.
        //
        // Only rows that still match a sample exactly are deleted. Anyone who
        // renamed one, moved its room or changed its time has made it their own
        // data, and deleting that would be worse than leaving it.
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                FORMER_SAMPLE_ROWS.forEach { (course, schedule, room) ->
                    db.execSQL(
                        "DELETE FROM schedule_entries WHERE course = ? AND schedule = ? AND room = ?",
                        arrayOf<Any?>(course, schedule, room)
                    )
                }
            }
        }

        // Kept only so MIGRATION_4_5 can recognize an untouched sample row.
        private val FORMER_SAMPLE_ROWS = listOf(
            Triple("MOBDEVE", "Mon/Wed • 1:00 PM", "Gokongwei 305"),
            Triple("CCAPDEV", "Tue/Thu • 9:15 AM", "Velasco 201"),
            Triple("ST-MATH", "Friday • 10:00 AM", "Andrew 1404"),
            Triple("GEWORLD", "Saturday • 8:00 AM", "Online")
        )
    }
}
