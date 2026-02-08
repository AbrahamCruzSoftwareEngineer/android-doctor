package com.evolutiondso.androiddoctor

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.Task
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicInteger

abstract class BuildMetricsService : BuildService<BuildMetricsService.Params>,
    TaskExecutionListener,
    BuildListener,
    TestListener {

    interface Params : BuildServiceParameters

    private val configurationStart = AtomicLong(0)
    private val configurationEnd = AtomicLong(0)
    private val buildFinishedAt = AtomicLong(0)

    private val taskStartTimes = ConcurrentHashMap<String, Long>()
    private val taskDurations = CopyOnWriteArrayList<TaskTiming>()

    private val totalExecutionMs = AtomicLong(0)
    private val cacheHits = AtomicLong(0)
    private val cacheMisses = AtomicLong(0)
    private val cacheSkipped = AtomicLong(0)
    private val incrementalCompilationUsed = AtomicLong(0)
    private val kaptIncrementalUsed = AtomicLong(0)

    private val testTotal = AtomicInteger(0)
    private val testPassed = AtomicInteger(0)
    private val testFailed = AtomicInteger(0)
    private val testSkipped = AtomicInteger(0)
    private val testTimings = CopyOnWriteArrayList<TestTiming>()
    private val testFailures = CopyOnWriteArrayList<TestFailure>()

    override fun settingsEvaluated(settings: Settings) {
        configurationStart.compareAndSet(0, System.nanoTime())
    }

    override fun projectsLoaded(gradle: Gradle) = Unit

    override fun projectsEvaluated(gradle: Gradle) {
        if (configurationStart.get() == 0L) {
            configurationStart.set(System.nanoTime())
        }
        configurationEnd.set(System.nanoTime())
    }

    override fun buildFinished(result: BuildResult) {
        buildFinishedAt.set(System.nanoTime())
    }

    override fun beforeExecute(task: Task) {
        taskStartTimes[task.path] = System.nanoTime()
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val start = taskStartTimes.remove(task.path) ?: return
        val durationMs = (System.nanoTime() - start) / 1_000_000
        totalExecutionMs.addAndGet(durationMs)
        taskDurations.add(
            TaskTiming(
                path = task.path,
                projectPath = task.project.path,
                durationMs = durationMs,
                didWork = state.didWork,
                skipped = state.skipped,
                skipMessage = state.skipMessage
            )
        )

        val skipMessage = state.skipMessage.orEmpty().uppercase()
        if (state.skipped) {
            if (skipMessage.contains("FROM-CACHE")) {
                cacheHits.incrementAndGet()
            } else {
                cacheSkipped.incrementAndGet()
            }
        } else if (state.didWork) {
            cacheMisses.incrementAndGet()
        }

        if (isIncrementalCompilation(task)) {
            incrementalCompilationUsed.compareAndSet(0, 1)
        }
        if (isKaptIncremental(task)) {
            kaptIncrementalUsed.compareAndSet(0, 1)
        }
    }

    override fun beforeSuite(suite: TestDescriptor) = Unit

    override fun afterSuite(suite: TestDescriptor, result: TestResult) = Unit

    override fun beforeTest(testDescriptor: TestDescriptor) = Unit

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        testTotal.incrementAndGet()
        when (result.resultType) {
            TestResult.ResultType.SUCCESS -> testPassed.incrementAndGet()
            TestResult.ResultType.FAILURE -> testFailed.incrementAndGet()
            TestResult.ResultType.SKIPPED -> testSkipped.incrementAndGet()
        }

        val durationMs = (result.endTime - result.startTime).coerceAtLeast(0)
        testTimings.add(
            TestTiming(
                className = testDescriptor.className ?: "<unknown>",
                name = testDescriptor.name,
                durationMs = durationMs
            )
        )

        result.exception?.let { ex ->
            testFailures.add(
                TestFailure(
                    className = testDescriptor.className ?: "<unknown>",
                    name = testDescriptor.name,
                    message = ex.message ?: "Test failed",
                    stackTrace = ex.stackTraceToString()
                )
            )
        }
    }

    fun snapshot(): BuildMetricsSnapshot {
        val configDurationMs = if (configurationStart.get() != 0L && configurationEnd.get() != 0L) {
            (configurationEnd.get() - configurationStart.get()) / 1_000_000
        } else {
            null
        }

        val durations = taskDurations.toList()

        return BuildMetricsSnapshot(
            configurationDurationMs = configDurationMs,
            executionDurationMs = totalExecutionMs.get().takeIf { it > 0 },
            taskDurations = durations,
            topLongestTasks = durations.sortedByDescending { it.durationMs }.take(10),
            cacheHits = cacheHits.get().toInt(),
            cacheMisses = cacheMisses.get().toInt(),
            cacheSkipped = cacheSkipped.get().toInt(),
            incrementalCompilationUsed = incrementalCompilationUsed.get() == 1L,
            kaptIncrementalUsed = kaptIncrementalUsed.get() == 1L,
            tests = TestsSnapshot(
                total = testTotal.get(),
                passed = testPassed.get(),
                failed = testFailed.get(),
                skipped = testSkipped.get(),
                durationMs = testTimings.sumOf { it.durationMs },
                slowest = testTimings.sortedByDescending { it.durationMs }.take(5),
                failures = testFailures.toList()
            )
        )
    }

    private fun isIncrementalCompilation(task: Task): Boolean {
        return try {
            val taskClass = task.javaClass.name
            if (!taskClass.contains("KotlinCompile")) return false
            val incrementalProp = task.javaClass.methods.firstOrNull { it.name == "isIncremental" }
            val incremental = incrementalProp?.invoke(task) as? Boolean
            incremental == true
        } catch (_: Throwable) {
            false
        }
    }

    private fun isKaptIncremental(task: Task): Boolean {
        return try {
            val taskClass = task.javaClass.name
            if (!taskClass.contains("Kapt")) return false
            val incrementalProp = task.javaClass.methods.firstOrNull { it.name == "isIncremental" }
            val incremental = incrementalProp?.invoke(task) as? Boolean
            incremental == true
        } catch (_: Throwable) {
            false
        }
    }
}

data class TaskTiming(
    val path: String,
    val projectPath: String,
    val durationMs: Long,
    val didWork: Boolean,
    val skipped: Boolean,
    val skipMessage: String?
)

data class BuildMetricsSnapshot(
    val configurationDurationMs: Long?,
    val executionDurationMs: Long?,
    val taskDurations: List<TaskTiming>,
    val topLongestTasks: List<TaskTiming>,
    val cacheHits: Int,
    val cacheMisses: Int,
    val cacheSkipped: Int,
    val incrementalCompilationUsed: Boolean,
    val kaptIncrementalUsed: Boolean,
    val tests: TestsSnapshot
)

data class TestsSnapshot(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val durationMs: Long,
    val slowest: List<TestTiming>,
    val failures: List<TestFailure>
)

data class TestTiming(
    val className: String,
    val name: String,
    val durationMs: Long
)

data class TestFailure(
    val className: String,
    val name: String,
    val message: String,
    val stackTrace: String
)
