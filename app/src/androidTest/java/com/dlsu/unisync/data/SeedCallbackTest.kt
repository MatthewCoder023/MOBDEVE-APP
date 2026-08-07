package com.dlsu.unisync.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dlsu.unisync.getOrAwaitValue
import com.dlsu.unisync.util.ScheduleDays
import com.dlsu.unisync.util.hasReadableSchedule
import com.dlsu.unisync.util.meetingStartMinutes
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Covers the first launch of a fresh install: the database is created from the
// entity definitions and then seeded.
//
// This path had no test, and that is exactly how a seed insert that omitted a
// NOT NULL column shipped -- the DAO tests build their database without the
// callback, and the migration test only ever upgrades an existing file, so both
// were green while a new install crashed on launch.
@RunWith(AndroidJUnit4::class)
class SeedCallbackTest {

    private lateinit var database: UniSyncDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, UniSyncDatabase::class.java)
            .addCallback(UniSyncDatabase.seedCallback)
            .allowMainThreadQueries()
            .setQueryExecutor { command -> command.run() }
            .build()
        // Creation, and therefore the seed, is lazy until the first query.
        database.openHelper.writableDatabase
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun a_new_database_seeds_without_violating_the_schema() = runTest {
        val entries = database.scheduleDao().getEntries().getOrAwaitValue()

        assertEquals(4, entries.size)
    }

    @Test
    fun every_seeded_class_is_structured_enough_to_reach_reminders() = runTest {
        val entries = database.scheduleDao().getEntries().getOrAwaitValue()

        entries.forEach { entry ->
            assertTrue("${entry.course} should have days set", entry.daysMask != 0)
            assertTrue("${entry.course} should be readable", entry.hasReadableSchedule)
        }
    }

    @Test
    fun the_seeded_days_and_times_match_the_text_they_display() = runTest {
        val entries = database.scheduleDao().getEntries().getOrAwaitValue()
        val mobdeve = entries.first { it.course == "MOBDEVE" }

        assertEquals(
            ScheduleDays.maskOf(listOf(Calendar.MONDAY, Calendar.WEDNESDAY)),
            mobdeve.daysMask
        )
        assertEquals(13 * 60, mobdeve.meetingStartMinutes())
    }
}
