package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase

class FreeHtmlRenderer : HtmlRendererBase() {

    override fun render(report: AndroidDoctorReport): String {
        val projectName = report.project?.name ?: "<unknown>"
        val buildHealth = report.scores?.buildHealth ?: 0
        val modernization = report.scores?.modernization ?: 0

        val content = """
            <h1>AndroidDoctor Report (Free)</h1>

            <div class="summary">
                <h2>Project: $projectName</h2>
                <p><strong>Build Health:</strong> $buildHealth / 100</p>
                <p><strong>Modernization:</strong> $modernization / 100</p>
            </div>

            <div class="section">
                <h2>Top Actions</h2>
                ${renderActions(report)}
            </div>
        """.trimIndent()

        return wrapHtml(content)
    }

    private fun renderActions(report: AndroidDoctorReport): String {
        return report.actions.orEmpty().take(3).joinToString("\n") { a ->
            """
            <div class="action">
                <strong>${a.title ?: a.id}</strong><br/>
                Priority: P${a.priority}<br/>
                Why: ${a.why}<br/>
            </div>
            """.trimIndent()
        }
    }
}
