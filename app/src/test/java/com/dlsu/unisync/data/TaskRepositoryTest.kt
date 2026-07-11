package com.dlsu.unisync.data

import com.dlsu.unisync.models.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    @Before
    fun resetRepository() {
        TaskRepository.reset()
    }

    @Test
    fun `reset seeds three tasks`() {
        assertEquals(3, TaskRepository.getTasks().size)
    }

    @Test
    fun `addTask inserts at the top`() {
        val task = TaskItem("New task", "Now")
        TaskRepository.addTask(task)

        val tasks = TaskRepository.getTasks()
        assertEquals(4, tasks.size)
        assertEquals(task.id, tasks.first().id)
    }

    @Test
    fun `setDone replaces the task with an updated copy`() {
        val target = TaskRepository.getTasks().first()
        assertFalse(target.isDone)

        TaskRepository.setDone(target.id, true)

        val updated = TaskRepository.getTasks().first { it.id == target.id }
        assertTrue(updated.isDone)
        assertEquals(target.title, updated.title)
    }

    @Test
    fun `setDone with unknown id changes nothing`() {
        val before = TaskRepository.getTasks()
        TaskRepository.setDone(id = -1, done = true)
        assertEquals(before, TaskRepository.getTasks())
    }

    @Test
    fun `removeTask deletes only the matching task`() {
        val target = TaskRepository.getTasks()[1]
        TaskRepository.removeTask(target.id)

        val tasks = TaskRepository.getTasks()
        assertEquals(2, tasks.size)
        assertTrue(tasks.none { it.id == target.id })
    }

    @Test
    fun `insertTask restores a task at its old position`() {
        val target = TaskRepository.getTasks()[1]
        TaskRepository.removeTask(target.id)

        TaskRepository.insertTask(1, target)

        assertEquals(target.id, TaskRepository.getTasks()[1].id)
    }

    @Test
    fun `insertTask clamps out-of-range positions`() {
        val task = TaskItem("Appended", "Later")
        TaskRepository.insertTask(99, task)

        assertEquals(task.id, TaskRepository.getTasks().last().id)
    }

    @Test
    fun `getTasks returns a snapshot that does not leak internal state`() {
        val snapshot = TaskRepository.getTasks() as MutableList<TaskItem>
        snapshot.clear()

        assertEquals(3, TaskRepository.getTasks().size)
    }
}
