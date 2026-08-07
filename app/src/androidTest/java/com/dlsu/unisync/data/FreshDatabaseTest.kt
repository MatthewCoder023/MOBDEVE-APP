package com.dlsu.unisync.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dlsu.unisync.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// A new install starts with nothing in it.
//
// This replaces the test that checked the sample classes were seeded correctly.
// The app used to insert four of them on first launch, and they were
// indistinguishable from real entries: the next-class card, the Today list and
// notifications all presented them as the user's own schedule. The guard now
// runs the other way, so nobody reintroduces a seed by accident.
@RunWith(AndroidJUnit4::class)
class FreshDatabaseTest {

    private lateinit var database: UniSyncDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, UniSyncDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { command -> command.run() }
            .build()
        database.openHelper.writableDatabase
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun a_new_database_has_no_classes() = runTest {
        assertTrue(database.scheduleDao().getAll().isEmpty())
    }

    @Test
    fun a_new_database_has_no_check_ins() = runTest {
        assertTrue(database.checkInDao().getRecent().getOrAwaitValue().isEmpty())
    }
}
