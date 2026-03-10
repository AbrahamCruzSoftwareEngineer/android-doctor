package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlSections {

    fun formattedGenerated(report: AndroidDoctorReport): String = DateFormatter.pretty(report.generatedAt)

    fun buildFreeBody(report: AndroidDoctorReport): String {
        return listOf(
            HtmlComponents.overviewCard(report, showGenerated = false),
            HtmlComponents.scoresCard(report),
            HtmlComponents.testingCoverageCard(report),
            HtmlComponents.diagnosticsSummaryCard(report),
            HtmlComponents.actionsCard(report)
        ).joinToString("\n")
    }
}
