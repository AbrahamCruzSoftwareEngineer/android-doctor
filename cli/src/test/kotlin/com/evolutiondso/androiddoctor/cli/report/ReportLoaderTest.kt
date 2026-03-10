package com.evolutiondso.androiddoctor.cli.report

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class ReportLoaderTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `load parses mocked report json with testing fields`() {
        val reportFile = tempDir.resolve("report.json")
        reportFile.writeText(
            """
            {
              "generatedAt": "2026-01-01T00:00:00Z",
              "project": { "name": "mocked-app", "path": ":app" },
              "scores": {
                "buildHealth": 87,
                "modernization": 79,
                "testingOverall": 64,
                "unitTestCoverage": 70,
                "uiTestCoverage": 45
              },
              "tests": {
                "moduleCount": 4,
                "modulesWithUnitTests": 3,
                "modulesWithUiTests": 1,
                "unitTestFiles": 24,
                "uiTestFiles": 5,
                "executedUnitTestTasks": 2,
                "executedUiTestTasks": 1,
                "unitCoverageScore": 70,
                "uiCoverageScore": 45,
                "overallScore": 64
              }
            }
            """.trimIndent()
        )

        val report = ReportLoader.load(reportFile.toString())

        assertNotNull(report)
        assertEquals("mocked-app", report?.project?.name)
        assertEquals(87, report?.scores?.buildHealth)
        assertEquals(64, report?.tests?.overallScore)
        assertEquals(5, report?.tests?.uiTestFiles)
    }
}
