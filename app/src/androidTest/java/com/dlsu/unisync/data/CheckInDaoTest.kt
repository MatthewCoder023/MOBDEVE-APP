package com.dlsu.unisync.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dlsu.unisync.getOrAwaitValue
import com.dlsu.unisync.models.CheckIn
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckInDaoTest {

    private lateinit var database: UniSyncDatabase
    private lateinit var dao: CheckInDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, UniSyncDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.checkInDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun recent_check_ins_are_newest_first() = runTest {
        dao.insert(CheckIn(course = "OLDEST", room = "A", timestamp = 1_000))
        dao.insert(CheckIn(course = "NEWEST", room = "B", timestamp = 3_000))
        dao.insert(CheckIn(course = "MIDDLE", room = "C", timestamp = 2_000))

        val courses = dao.getRecent().getOrAwaitValue().map { it.course }
        assertEquals(listOf("NEWEST", "MIDDLE", "OLDEST"), courses)
    }

    // The query caps at 10 so the QR screen's history stays bounded.
    @Test
    fun recent_check_ins_are_capped_at_ten() = runTest {
        repeat(15) { index ->
            dao.insert(CheckIn(course = "COURSE$index", room = "R", timestamp = index.toLong()))
        }

        val recent = dao.getRecent().getOrAwaitValue()
        assertEquals(10, recent.size)
        // Newest first means the highest timestamps survive the limit.
        assertEquals("COURSE14", recent.first().course)
        assertEquals("COURSE5", recent.last().course)
    }

    @Test
    fun empty_table_emits_an_empty_list() = runTest {
        assertEquals(emptyList<CheckIn>(), dao.getRecent().getOrAwaitValue())
    }
}
