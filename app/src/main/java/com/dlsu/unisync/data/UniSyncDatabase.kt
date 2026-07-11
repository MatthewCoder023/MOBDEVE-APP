package com.dlsu.unisync.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dlsu.unisync.models.TaskItem

@Database(entities = [TaskItem::class], version = 1, exportSchema = false)
abstract class UniSyncDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

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
                    .build()
                    .also { instance = it }
            }

        // Seeds demo tasks the first time the database file is created.
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
            }
        }
    }
}
