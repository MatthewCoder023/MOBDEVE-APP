package com.dlsu.unisync.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dlsu.unisync.data.TaskRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TasksViewModelTest {

    // Makes LiveData setValue run synchronously on the test thread.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        TaskRepository.reset()
        viewModel = TasksViewModel()
    }

    @Test
    fun `tasks starts with the repository seed data`() {
        assertEquals(3, viewModel.tasks.value?.size)
    }

    @Test
    fun `addTask prepends and publishes the new list`() {
        viewModel.addTask("Study for quiz", "Due Monday")

        val tasks = requireNotNull(viewModel.tasks.value)
        assertEquals(4, tasks.size)
        assertEquals("Study for quiz", tasks.first().title)
    }

    @Test
    fun `setTaskDone publishes an updated copy`() {
        val target = requireNotNull(viewModel.tasks.value).first()

        viewModel.setTaskDone(target, true)

        val updated = requireNotNull(viewModel.tasks.value).first { it.id == target.id }
        assertTrue(updated.isDone)
    }

    @Test
    fun `removeTask then restoreTask puts the task back in place`() {
        val target = requireNotNull(viewModel.tasks.value)[1]

        viewModel.removeTask(target)
        assertEquals(2, viewModel.tasks.value?.size)

        viewModel.restoreTask(1, target)
        val tasks = requireNotNull(viewModel.tasks.value)
        assertEquals(3, tasks.size)
        assertEquals(target.id, tasks[1].id)
    }
}
