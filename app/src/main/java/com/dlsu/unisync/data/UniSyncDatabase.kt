package com.dlsu.unisync.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlsu.unisync.models.CheckIn
import com.dlsu.unisync.models.ScheduleEntry
import com.dlsu.unisync.models.TaskItem

@Database(
    entities = [TaskItem::class, CheckIn::class, ScheduleEntry::class],
    version = 2,
    exportSchema = false
)
abstract class UniSyncDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

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
                    .addMigrations(MIGRATION_1_2)
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

        // Seeds demo data the first time the database file is created.
        // Staggered createdAt values keep the intended newest-first order.
        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL(
                    "INSERT INTO tasks (title, due, isDone, createdAt) VALUES " +
                        "('Finalize MOBDEVE wireframes', 'Due tonight at 11:59 PM', 0, $now)," +
                        "('Read HCI chapter 6', 'Due tomorrow', 0, ${now - 1})," +
                        "('Group meeting notes', 'Due Friday', 1, ${now - 2})"
                )
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
