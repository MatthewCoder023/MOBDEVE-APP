package com.dlsu.unisync.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dlsu.unisync.getOrAwaitValue
import com.dlsu.unisync.models.ScheduleEntry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Exercises the real SQL. An in-memory database skips the seed callback, so
// each test starts from a known empty table.
@RunWith(AndroidJUnit4::class)
class ScheduleDaoTest {

    private lateinit var database: UniSyncDatabase
    private lateinit var dao: ScheduleDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, UniSyncDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.scheduleDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun insert_then_read_returns_the_entry() = runTest {
        dao.insert(ScheduleEntry(course = "MOBDEVE", schedule = "Mon/Wed • 1:00 PM", room = "Gokongwei 305"))

        val entries = dao.getEntries().getOrAwaitValue()
        assertEquals(1, entries.size)
        assertEquals("MOBDEVE", entries.first().course)
        assertEquals("Gokongwei 305", entries.first().room)
    }

    @Test
    fun entries_come_back_ordered_by_id() = runTest {
        dao.insert(ScheduleEntry(course = "FIRST", schedule = "Mon", room = "A"))
        dao.insert(ScheduleEntry(course = "SECOND", schedule = "Tue", room = "B"))
        dao.insert(ScheduleEntry(course = "THIRD", schedule = "Wed", room = "C"))

        val courses = dao.getEntries().getOrAwaitValue().map { it.course }
        assertEquals(listOf("FIRST", "SECOND", "THIRD"), courses)
    }

    // The DAO uses OnConflictStrategy.REPLACE so the same call serves as update.
    @Test
    fun inserting_an_existing_id_updates_in_place() = runTest {
        dao.insert(ScheduleEntry(course = "MOBDEVE", schedule = "Mon", room = "G305"))
        val original = dao.getEntries().getOrAwaitValue().first()

        dao.insert(original.copy(room = "G306"))

        val entries = dao.getEntries().getOrAwaitValue()
        assertEquals("no duplicate row should be created", 1, entries.size)
        assertEquals("G306", entries.first().room)
        assertEquals(original.id, entries.first().id)
    }

    @Test
    fun delete_removes_only_the_given_entry() = runTest {
        dao.insert(ScheduleEntry(course = "KEEP", schedule = "Mon", room = "A"))
        dao.insert(ScheduleEntry(course = "DROP", schedule = "Tue", room = "B"))
        val toDelete = dao.getEntries().getOrAwaitValue().first { it.course == "DROP" }

        dao.delete(toDelete)

        val entries = dao.getEntries().getOrAwaitValue()
        assertEquals(1, entries.size)
        assertEquals("KEEP", entries.first().course)
    }

    // Undo re-inserts the deleted entry with its original id, which is what
    // restores its position in the list.
    @Test
    fun reinserting_a_deleted_entry_restores_its_position() = runTest {
        dao.insert(ScheduleEntry(course = "A", schedule = "Mon", room = "1"))
        dao.insert(ScheduleEntry(course = "B", schedule = "Tue", room = "2"))
        dao.insert(ScheduleEntry(course = "C", schedule = "Wed", room = "3"))
        val middle = dao.getEntries().getOrAwaitValue()[1]

        dao.delete(middle)
        dao.insert(middle)

        val courses = dao.getEntries().getOrAwaitValue().map { it.course }
        assertEquals(listOf("A", "B", "C"), courses)
    }

    @Test
    fun empty_table_emits_an_empty_list() = runTest {
        assertTrue(dao.getEntries().getOrAwaitValue().isEmpty())
    }
}
