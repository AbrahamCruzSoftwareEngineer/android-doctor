package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase

class PremiumHtmlRenderer : HtmlRendererBase() {

    override fun render(report: AndroidDoctorReport): String {
        val projectName = report.project?.name ?: "<unknown>"
        val buildHealth = report.scores?.buildHealth ?: 0
        val modernization = report.scores?.modernization ?: 0

        val content = """
            <h1>AndroidDoctor Premium Report</h1>

            <div class="summary">
                <h2>Project: $projectName</h2>
                <p><strong>Build Health:</strong> $buildHealth / 100</p>
                <p><strong>Modernization:</strong> $modernization / 100</p>
                <p><em>Premium insights enabled</em></p>
            </div>

            <div class="section">
                <h2>Top Actions (Full Detail)</h2>
                ${renderActions(report)}
            </div>

            <div class="section">
                <h2>Score Improvement Estimate</h2>
                ${renderImprovements(report)}
            </div>
        """.trimIndent()

        return wrapHtml(content)
    }

    private fun renderActions(report: AndroidDoctorReport): String {
        return report.actions.orEmpty().joinToString("\n") { a ->
            """
            <div class="action">
                <strong>${a.title ?: a.id}</strong><br/>
                Severity: ${a.severity} — Effort: ${a.effort}<br/>
                Why: ${a.why}<br/>
                How: ${a.how}<br/>
                Impact: +${a.impact?.buildHealthDelta} build, +${a.impact?.modernizationDelta} modernization
            </div>
            """.trimIndent()
        }
    }

    private fun renderImprovements(report: AndroidDoctorReport): String {
        val b = report.actions?.sumOf { it.impact?.buildHealthDelta ?: 0 } ?: 0
        val m = report.actions?.sumOf { it.impact?.modernizationDelta ?: 0 } ?: 0
        return """
            <p><strong>Total Potential Build Score Gain:</strong> +$b</p>
            <p><strong>Total Potential Modernization Gain:</strong> +$m</p>
        """.trimIndent()
    }
}
