package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.ActionInfo
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.model.AndroidInfo
import com.evolutiondso.androiddoctor.cli.model.Checks
import com.evolutiondso.androiddoctor.cli.model.DiagnosticsInfo
import com.evolutiondso.androiddoctor.cli.model.ImpactInfo
import com.evolutiondso.androiddoctor.cli.model.PerformanceInfo
import com.evolutiondso.androiddoctor.cli.model.ProjectInfo
import com.evolutiondso.androiddoctor.cli.model.ScoresInfo
import com.evolutiondso.androiddoctor.cli.model.TestsInfo
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PremiumHtmlRendererTest {

    @Test
    fun `render includes mocked testing data in card and js payload`() {
        val report = AndroidDoctorReport(
            generatedAt = "2026-01-01T00:00:00Z",
            project = ProjectInfo(name = "mocked-app", path = ":app"),
            checks = Checks(usesKapt = true, configurationCacheEnabled = false),
            android = AndroidInfo(composeEnabled = true),
            scores = ScoresInfo(
                buildHealth = 85,
                modernization = 78,
                testingOverall = 66,
                unitTestCoverage = 72,
                uiTestCoverage = 49
            ),
            performance = PerformanceInfo(configurationMs = 1200, executionMs = 3500, incrementalCompilation = true),
            diagnostics = DiagnosticsInfo(),
            tests = TestsInfo(
                moduleCount = 4,
                modulesWithUnitTests = 3,
                modulesWithUiTests = 1,
                unitTestFiles = 20,
                uiTestFiles = 4,
                executedUnitTestTasks = 2,
                executedUiTestTasks = 1,
                unitCoverageScore = 72,
                uiCoverageScore = 49,
                overallScore = 66
            ),
            actions = listOf(
                ActionInfo(
                    title = "Improve UI tests",
                    impact = ImpactInfo(buildHealthDelta = 5, modernizationDelta = 7)
                )
            )
        )

        val html = PremiumHtmlRenderer().render(report)

        assertTrue(html.contains("Testing Coverage"))
        assertTrue(html.contains("Modules with Unit Tests:</strong> 3 / 4"))
        assertTrue(html.contains("window.__ANDROID_DOCTOR_DATA__"))
        assertTrue(html.contains("\"unitCoverageScore\": 72"))
        assertTrue(html.contains("\"uiCoverageScore\": 49"))
        assertTrue(html.contains("\"overallScore\": 66"))
    }

    @Test
    fun `render handles empty report with fallbacks and escaped action text`() {
        val report = AndroidDoctorReport(
            project = ProjectInfo(name = "edge-case-app"),
            actions = listOf(
                ActionInfo(
                    title = "Quote \"break\" and newline\\nattack",
                    impact = ImpactInfo(buildHealthDelta = -1, modernizationDelta = -2)
                )
            )
        )

        val html = PremiumHtmlRenderer().render(report)

        assertTrue(html.contains("Testing Coverage"))
        assertTrue(html.contains("Modules with Unit Tests:</strong> 0 / 0"))
        assertTrue(html.contains("\"unitCoverageScore\": 0"))
        assertTrue(html.contains("\"uiCoverageScore\": 0"))
        assertTrue(html.contains("\"overallScore\": 0"))
        assertTrue(html.contains("Quote \\\"break\\\" and newline\\nattack"))
    }
}
