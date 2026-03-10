package com.evolutiondso.androiddoctor

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildMetricsServiceTest {

    private class TestMetricsService : BuildMetricsService() {
        override fun getParameters(): BuildMetricsService.Params = object : BuildMetricsService.Params {}
    }

    @Test
    fun `tracks from-cache skips as cache hits`() {
        val service = TestMetricsService()
        val task = mock(Task::class.java)
        val project = mock(Project::class.java)
        val state = mock(TaskState::class.java)

        `when`(project.path).thenReturn(":app")
        `when`(task.path).thenReturn(":app:compileDebugKotlin")
        `when`(task.project).thenReturn(project)
        `when`(state.skipped).thenReturn(true)
        `when`(state.didWork).thenReturn(false)
        `when`(state.skipMessage).thenReturn("FROM-CACHE")

        service.beforeExecute(task)
        service.afterExecute(task, state)

        val snapshot = service.snapshot()
        assertEquals(1, snapshot.cacheHits)
        assertEquals(0, snapshot.cacheMisses)
        assertEquals(0, snapshot.cacheSkipped)
    }

    @Test
    fun `tracks non-cache skips as cache skipped`() {
        val service = TestMetricsService()
        val task = mock(Task::class.java)
        val project = mock(Project::class.java)
        val state = mock(TaskState::class.java)

        `when`(project.path).thenReturn(":app")
        `when`(task.path).thenReturn(":app:lint")
        `when`(task.project).thenReturn(project)
        `when`(state.skipped).thenReturn(true)
        `when`(state.didWork).thenReturn(false)
        `when`(state.skipMessage).thenReturn("UP-TO-DATE")

        service.beforeExecute(task)
        service.afterExecute(task, state)

        val snapshot = service.snapshot()
        assertEquals(0, snapshot.cacheHits)
        assertEquals(0, snapshot.cacheMisses)
        assertEquals(1, snapshot.cacheSkipped)
    }

    @Test
    fun `tracks executed work as cache miss`() {
        val service = TestMetricsService()
        val task = mock(Task::class.java)
        val project = mock(Project::class.java)
        val state = mock(TaskState::class.java)

        `when`(project.path).thenReturn(":feature")
        `when`(task.path).thenReturn(":feature:compileDebugJavaWithJavac")
        `when`(task.project).thenReturn(project)
        `when`(state.skipped).thenReturn(false)
        `when`(state.didWork).thenReturn(true)
        `when`(state.skipMessage).thenReturn(null)

        service.beforeExecute(task)
        service.afterExecute(task, state)

        val snapshot = service.snapshot()
        assertEquals(0, snapshot.cacheHits)
        assertEquals(1, snapshot.cacheMisses)
        assertEquals(0, snapshot.cacheSkipped)
    }
}
