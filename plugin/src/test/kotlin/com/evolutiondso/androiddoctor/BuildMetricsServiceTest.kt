package com.evolutiondso.androiddoctor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildMetricsServiceTest {

    @Test
    fun `FROM-CACHE is classified as cache hit`() {
        val outcome = classifyCacheOutcome(
            skipped = true,
            didWork = false,
            skipMessage = "FROM-CACHE"
        )

        assertEquals(CacheOutcome.HIT, outcome)
    }

    @Test
    fun `skipped non cache task is classified as skipped`() {
        val outcome = classifyCacheOutcome(
            skipped = true,
            didWork = false,
            skipMessage = "UP-TO-DATE"
        )

        assertEquals(CacheOutcome.SKIPPED, outcome)
    }

    @Test
    fun `did work task is classified as miss`() {
        val outcome = classifyCacheOutcome(
            skipped = false,
            didWork = true,
            skipMessage = null
        )

        assertEquals(CacheOutcome.MISS, outcome)
    }

    @Test
    fun `idle task is classified as none`() {
        val outcome = classifyCacheOutcome(
            skipped = false,
            didWork = false,
            skipMessage = null
        )

        assertEquals(CacheOutcome.NONE, outcome)
    }
}
