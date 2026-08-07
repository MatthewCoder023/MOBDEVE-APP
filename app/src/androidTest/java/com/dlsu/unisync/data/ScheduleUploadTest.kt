package com.dlsu.unisync.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dlsu.unisync.createTestDatabase
import com.dlsu.unisync.models.LegacyScheduleEntry
import com.dlsu.unisync.models.ScheduleEntry
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Covers the hand-off from the retired device-local table to the account.
//
// This is the upgrade path for anyone who had classes before the schedule moved
// to Firestore. Getting it wrong looks exactly like the data loss the move is
// meant to prevent: an empty schedule screen, with the classes still on the
// handset but unreachable.
@RunWith(AndroidJUnit4::class)
class ScheduleUploadTest {

    private lateinit var database: UniSyncDatabase
    private lateinit var dao: ScheduleDao
    private lateinit var repository: RecordingScheduleRepository

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.scheduleDao()
        repository = RecordingScheduleRepository()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun local_classes_are_uploaded_and_then_cleared() = runTest {
        dao.insert(
            LegacyScheduleEntry(
                course = "MOBDEVE",
                schedule = "Mon/Wed • 1:00 PM",
                room = "Gokongwei 305",
                daysMask = (1 shl Calendar.MONDAY) or (1 shl Calendar.WEDNESDAY),
                startMinutes = 13 * 60
            )
        )

        ScheduleUploader.uploadLocalEntries(dao, repository)

        assertEquals(1, repository.added.size)
        val uploaded = repository.added.single()
        assertEquals("MOBDEVE", uploaded.course)
        assertEquals("Mon/Wed • 1:00 PM", uploaded.schedule)
        assertEquals("Gokongwei 305", uploaded.room)
        assertEquals((1 shl Calendar.MONDAY) or (1 shl Calendar.WEDNESDAY), uploaded.daysMask)
        assertEquals(13 * 60, uploaded.startMinutes)

        assertTrue("the local table should be empty once uploaded", dao.getAll().isEmpty())
    }

    @Test
    fun rows_keep_their_order() = runTest {
        listOf("FIRST", "SECOND", "THIRD").forEach {
            dao.insert(LegacyScheduleEntry(course = it, schedule = "Mon", room = ""))
        }

        ScheduleUploader.uploadLocalEntries(dao, repository)

        assertEquals(listOf("FIRST", "SECOND", "THIRD"), repository.added.map { it.course })
        // createdAt drives list order in Firestore, so it has to stay ascending.
        assertEquals(repository.added.map { it.createdAt }.sorted(), repository.added.map { it.createdAt })
    }

    @Test
    fun an_empty_table_uploads_nothing() = runTest {
        ScheduleUploader.uploadLocalEntries(dao, repository)

        assertTrue(repository.added.isEmpty())
    }

    // A failing upload must leave the rows alone so the next launch can retry,
    // rather than clearing them and losing the classes for good.
    @Test
    fun a_failed_upload_leaves_the_local_rows_in_place() = runTest {
        dao.insert(LegacyScheduleEntry(course = "MOBDEVE", schedule = "Mon", room = ""))
        val failing = object : ScheduleRepository by repository {
            override suspend fun add(entry: ScheduleEntry) = throw IllegalStateException("offline")
        }

        runCatching { ScheduleUploader.uploadLocalEntries(dao, failing) }

        assertEquals(1, dao.getAll().size)
    }
}

private class RecordingScheduleRepository : ScheduleRepository {
    val added = mutableListOf<ScheduleEntry>()

    override val entries: LiveData<List<ScheduleEntry>> = MutableLiveData(emptyList())

    override suspend fun add(entry: ScheduleEntry) {
        added += entry
    }

    override suspend fun update(entry: ScheduleEntry) = Unit

    override suspend fun remove(entry: ScheduleEntry) = Unit

    override suspend fun restore(entry: ScheduleEntry) = Unit
}
