package com.evolutiondso.androiddoctor.cli.report

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

data class Summary(
    val projectName: String,
    val status: String,
    val targetType: String,
    val buildScore: Int?,
    val modernScore: Int?,
    val actions: List<ActionSummary>
)

data class ActionSummary(
    val title: String,
    val severity: String?,
    val effort: String?,
    val impact: String
)

object ReportAnalyzer {

    fun summarize(report: AndroidDoctorReport): Summary {

        val projectName = report.project?.name ?: "<unknown>"
        val status = report.status ?: "<unknown>"

        val targetType = when {
            report.checks?.isAndroidApplication == true -> "Android App"
            report.checks?.isAndroidLibrary == true -> "Android Library"
            report.checks?.isAndroidProject == true -> "Android Project"
            else -> "Non-Android"
        }

        val actions = report.actions.orEmpty().map {
            ActionSummary(
                title = it.title ?: it.id ?: "<action>",
                severity = it.severity,
                effort = it.effort,
                impact = buildImpact(it.impact?.buildHealthDelta, it.impact?.modernizationDelta)
            )
        }

        return Summary(
            projectName = projectName,
            status = status,
            targetType = targetType,
            buildScore = report.scores?.buildHealth,
            modernScore = report.scores?.modernization,
            actions = actions
        )
    }

    private fun buildImpact(b: Int?, m: Int?): String {
        val parts = mutableListOf<String>()
        if (b != null && b != 0) parts += "Build Health ${format(b)}"
        if (m != null && m != 0) parts += "Modernize ${format(m)}"
        return parts.joinToString(", ").ifBlank { "no score change" }
    }

    private fun format(v: Int): String = if (v >= 0) "+$v" else "$v"
}
