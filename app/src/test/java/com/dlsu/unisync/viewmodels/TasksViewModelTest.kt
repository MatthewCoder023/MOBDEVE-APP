package com.dlsu.unisync.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dlsu.unisync.MainDispatcherRule
import com.dlsu.unisync.data.FakeTaskRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TasksViewModelTest {

    // Makes LiveData setValue run synchronously on the test thread.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeTaskRepository
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        repository = FakeTaskRepository()
        viewModel = TasksViewModel(repository)
        viewModel.addTask("First task", "Due Monday")
        viewModel.addTask("Second task", "Due Tuesday")
        viewModel.addTask("Third task", "Due Wednesday")
    }

    @Test
    fun `addTask prepends and publishes the new list`() {
        val tasks = requireNotNull(viewModel.tasks.value)
        assertEquals(3, tasks.size)
        assertEquals("Third task", tasks.first().title)
    }

    @Test
    fun `setTaskDone publishes an updated copy`() {
        val target = requireNotNull(viewModel.tasks.value).first()

        viewModel.setTaskDone(target, true)

        val updated = requireNotNull(viewModel.tasks.value).first { it.id == target.id }
        assertTrue(updated.isDone)
        assertEquals(target.title, updated.title)
    }

    @Test
    fun `removeTask deletes only the matching task`() {
        val target = requireNotNull(viewModel.tasks.value)[1]

        viewModel.removeTask(target)

        val tasks = requireNotNull(viewModel.tasks.value)
        assertEquals(2, tasks.size)
        assertTrue(tasks.none { it.id == target.id })
    }

    @Test
    fun `removeTask then restoreTask puts the task back in place`() {
        val target = requireNotNull(viewModel.tasks.value)[1]

        viewModel.removeTask(target)
        assertEquals(2, viewModel.tasks.value?.size)

        viewModel.restoreTask(target)
        val tasks = requireNotNull(viewModel.tasks.value)
        assertEquals(3, tasks.size)
        assertEquals(target.id, tasks[1].id)
    }
}
