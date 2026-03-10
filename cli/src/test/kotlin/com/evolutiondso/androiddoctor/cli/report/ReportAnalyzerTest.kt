package com.evolutiondso.androiddoctor.cli.report

import com.evolutiondso.androiddoctor.core.model.ActionInfo
import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.core.model.Checks
import com.evolutiondso.androiddoctor.core.model.ImpactInfo
import com.evolutiondso.androiddoctor.core.model.ProjectInfo
import com.evolutiondso.androiddoctor.core.model.ScoresInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReportAnalyzerTest {

    @Test
    fun `summarize maps android app report fields`() {
        val report = AndroidDoctorReport(
            project = ProjectInfo(name = "demo", path = ":app"),
            status = "ok",
            checks = Checks(isAndroidApplication = true),
            scores = ScoresInfo(buildHealth = 81, modernization = 72),
            actions = listOf(
                ActionInfo(
                    title = "Enable cache",
                    severity = "medium",
                    effort = "low",
                    impact = ImpactInfo(buildHealthDelta = 5, modernizationDelta = -1)
                )
            )
        )

        val summary = ReportAnalyzer.summarize(report)

        assertEquals("demo", summary.projectName)
        assertEquals("ok", summary.status)
        assertEquals("Android App", summary.targetType)
        assertEquals(81, summary.buildScore)
        assertEquals(72, summary.modernScore)
        assertEquals("Enable cache", summary.actions.single().title)
        assertEquals("Build Health +5, Modernize -1", summary.actions.single().impact)
    }

    @Test
    fun `summarize falls back for non-android and empty impacts`() {
        val report = AndroidDoctorReport(
            checks = Checks(isAndroidApplication = false, isAndroidLibrary = false, isAndroidProject = false),
            actions = listOf(
                ActionInfo(id = "a1", impact = ImpactInfo(buildHealthDelta = 0, modernizationDelta = 0))
            )
        )

        val summary = ReportAnalyzer.summarize(report)

        assertEquals("<unknown>", summary.projectName)
        assertEquals("<unknown>", summary.status)
        assertEquals("Non-Android", summary.targetType)
        assertEquals("a1", summary.actions.single().title)
        assertEquals("no score change", summary.actions.single().impact)
    }
}
