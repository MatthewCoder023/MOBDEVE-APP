package com.dlsu.unisync

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.dlsu.unisync.data.UniSyncDatabase

// An in-memory database for the DAO tests. Queries run on the calling thread on
// purpose: with Room's default background query executor, a LiveData refresh
// queued by the last insert can still be running when @After closes the
// database, and it then dies with "connection pool has been closed", failing the
// test for reasons that have nothing to do with the SQL under test. Running the
// refresh inline means nothing is left in flight at close() time.
//
// An in-memory database also skips the seed callback, so each test starts from a
// known empty table.
fun createTestDatabase(): UniSyncDatabase {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return Room.inMemoryDatabaseBuilder(context, UniSyncDatabase::class.java)
        .allowMainThreadQueries()
        .setQueryExecutor { command -> command.run() }
        .build()
}
