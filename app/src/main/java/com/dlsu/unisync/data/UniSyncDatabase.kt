package com.dlsu.unisync.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlsu.unisync.models.CheckIn
import com.dlsu.unisync.models.ScheduleEntry

// Local store for the class schedule and attendance history. Tasks moved to
// Firestore (see FirestoreTaskRepository) so they sync across devices; the data
// here is deliberately device-local.
@Database(
    entities = [CheckIn::class, ScheduleEntry::class],
    version = 3,
    exportSchema = false
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }

        // v1 -> v2: structured due date on tasks, plus check-in history and the
        // editable schedule. Upgraders also get the schedule seed data.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
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
                seedSchedule(db)
            }
        }

        // v2 -> v3: tasks now live in Firestore, so the local table is dropped.
        // Any tasks created before this upgrade are not carried over.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS tasks")
            }
        }

        // Seeds demo data the first time the database file is created.
        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedSchedule(db)
            }
        }

        private fun seedSchedule(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT INTO schedule_entries (course, schedule, room) VALUES " +
                    "('MOBDEVE', 'Mon/Wed • 1:00 PM', 'Gokongwei 305')," +
                    "('CCAPDEV', 'Tue/Thu • 9:15 AM', 'Velasco 201')," +
                    "('ST-MATH', 'Friday • 10:00 AM', 'Andrew 1404')," +
                    "('GEWORLD', 'Saturday • 8:00 AM', 'Online')"
            )
        }
    }
}
