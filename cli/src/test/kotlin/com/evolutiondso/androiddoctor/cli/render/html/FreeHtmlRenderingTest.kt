package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.core.model.ActionInfo
import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.core.model.Checks
import com.evolutiondso.androiddoctor.core.model.DiagnosticsInfo
import com.evolutiondso.androiddoctor.core.model.EnvironmentInfo
import com.evolutiondso.androiddoctor.core.model.ExecutionInfo
import com.evolutiondso.androiddoctor.core.model.ImpactInfo
import com.evolutiondso.androiddoctor.core.model.PhaseDurationInfo
import com.evolutiondso.androiddoctor.core.model.ProjectInfo
import com.evolutiondso.androiddoctor.core.model.ScoresInfo
import com.evolutiondso.androiddoctor.core.model.TestsInfo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FreeHtmlRenderingTest {

    private val report = AndroidDoctorReport(
        generatedAt = "2026-01-01T00:00:00Z",
        project = ProjectInfo(name = "demo", path = ":app"),
        checks = Checks(moduleCount = 4),
        scores = ScoresInfo(buildHealth = 80, modernization = 70, testingOverall = 65, unitTestCoverage = 60, uiTestCoverage = 40),
        diagnostics = DiagnosticsInfo(
            configuration = PhaseDurationInfo(durationMs = 120),
            execution = ExecutionInfo(durationMs = 400)
        ),
        tests = TestsInfo(
            moduleCount = 4,
            modulesWithUnitTests = 3,
            modulesWithUiTests = 1,
            unitCoverageScore = 60,
            uiCoverageScore = 40,
            overallScore = 65
        ),
        environment = EnvironmentInfo(ci = true),
        actions = listOf(
            ActionInfo(
                title = "Use configuration cache",
                why = "improves performance",
                how = "set gradle property",
                severity = "high",
                effort = "low",
                impact = ImpactInfo(buildHealthDelta = 4, modernizationDelta = 1)
            )
        )
    )

    @Test
    fun `free renderer outputs free html without premium markers`() {
        val html = FreeHtmlRenderer().render(report)

        assertTrue(html.contains("AndroidDoctor Report"))
        assertTrue(html.contains("Project: demo"))
        assertTrue(html.contains("Use configuration cache"))
        assertFalse(html.contains("Premium"))
        assertFalse(html.contains("chart.js"))
    }

    @Test
    fun `html helpers return expected snippets`() {
        val body = HtmlSections.buildFreeBody(report)
        val page = HtmlTemplates.page(report, body)
        val gauge = HtmlGauge.gauge(150, "Build Health")

        assertTrue(body.contains("Diagnostics Summary"))
        assertTrue(page.contains("<!DOCTYPE html>"))
        assertTrue(page.contains("AndroidDoctor Report"))
        assertTrue(gauge.contains("data-value=\"100\""))
        assertTrue(HtmlAssets.styleCss().contains(".container"))
    }
}
