package com.evolutiondso.androiddoctor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BuildMetricsServiceTest {

    @Test
    fun `classifyCacheOutcome covers all state combinations`() {
        data class Case(
            val skipped: Boolean,
            val didWork: Boolean,
            val skipMessage: String?,
            val expected: CacheOutcome
        )

        val cases = listOf(
            Case(skipped = true, didWork = false, skipMessage = "FROM-CACHE", expected = CacheOutcome.HIT),
            Case(skipped = true, didWork = true, skipMessage = "from-cache", expected = CacheOutcome.HIT),
            Case(skipped = true, didWork = false, skipMessage = "UP-TO-DATE", expected = CacheOutcome.SKIPPED),
            Case(skipped = true, didWork = false, skipMessage = null, expected = CacheOutcome.SKIPPED),
            Case(skipped = false, didWork = true, skipMessage = null, expected = CacheOutcome.MISS),
            Case(skipped = false, didWork = false, skipMessage = null, expected = CacheOutcome.NONE),
            Case(skipped = false, didWork = false, skipMessage = "FROM-CACHE", expected = CacheOutcome.NONE)
        )

        cases.forEach { c ->
            assertEquals(c.expected, classifyCacheOutcome(c.skipped, c.didWork, c.skipMessage))
        }
    }
}
