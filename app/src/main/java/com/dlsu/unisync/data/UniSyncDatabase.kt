package com.dlsu.unisync.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlsu.unisync.models.CheckIn
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.util.ScheduleDays
import com.dlsu.unisync.util.ScheduleParser

// Local store for the class schedule and attendance history. Tasks moved to
// Firestore (see FirestoreTaskRepository) so they sync across devices; the data
// here is deliberately device-local.
@Database(
    entities = [CheckIn::class, ScheduleEntry::class],
    version = 4,
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
                    .addCallback(SeedCallback)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }

        // v1 -> v2: structured due date on tasks, plus check-in history and the
        // editable schedule. Upgraders also get the schedule seed data.
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
                seedSchedule(db, structured = false)
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

        // Exposed like the migrations so SeedCallbackTest can build a database
        // that seeds exactly the way a fresh install does. The DAO tests leave the
        // callback out, which is how a seed that violated the schema reached a
        // device with every suite green.
        val seedCallback: Callback get() = SeedCallback

        // Seeds demo data the first time the database file is created.
        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedSchedule(db, structured = true)
            }
        }

        private val SEED_ROWS = listOf(
            Triple("MOBDEVE", "Mon/Wed • 1:00 PM", "Gokongwei 305"),
            Triple("CCAPDEV", "Tue/Thu • 9:15 AM", "Velasco 201"),
            Triple("ST-MATH", "Friday • 10:00 AM", "Andrew 1404"),
            Triple("GEWORLD", "Saturday • 8:00 AM", "Online")
        )

        // structured=false writes the v2-shaped row used by MIGRATION_1_2, where
        // the day and time columns do not exist yet; MIGRATION_3_4 fills them in
        // afterwards. On a fresh v4 database the columns are NOT NULL with no SQL
        // default, so the insert has to supply them itself.
        private fun seedSchedule(db: SupportSQLiteDatabase, structured: Boolean) {
            SEED_ROWS.forEach { (course, schedule, room) ->
                if (structured) {
                    db.execSQL(
                        "INSERT INTO schedule_entries (course, schedule, room, daysMask, startMinutes) " +
                            "VALUES (?, ?, ?, ?, ?)",
                        arrayOf<Any?>(
                            course,
                            schedule,
                            room,
                            ScheduleDays.maskOf(ScheduleParser.daysOf(schedule)),
                            ScheduleParser.startMinutes(schedule)
                        )
                    )
                } else {
                    db.execSQL(
                        "INSERT INTO schedule_entries (course, schedule, room) VALUES (?, ?, ?)",
                        arrayOf<Any?>(course, schedule, room)
                    )
                }
            }
        }
    }
}
