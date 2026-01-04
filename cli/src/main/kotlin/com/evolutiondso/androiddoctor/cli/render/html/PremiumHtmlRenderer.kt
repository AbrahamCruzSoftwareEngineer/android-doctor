package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PremiumHtmlRenderer : HtmlRendererBase() {

    override fun render(report: AndroidDoctorReport): String {
        val timestamp = cleanDate(report.generatedAt)
        val actionsHtml = report.actions.orEmpty().joinToString("\n") { a ->
            """
            <div class="action-item">
                <strong>${escape(a.title)}</strong><br>
                <em>Why:</em> ${escape(a.why)}<br>
                <em>How:</em> ${escape(a.how)}<br>
                <div class="impact">+${a.impact?.buildHealthDelta} Build • +${a.impact?.modernizationDelta} Modernize</div>
            </div>
            """.trimIndent()
        }

        return """
        <html>
            <head>
                <meta charset="UTF-8" />
                <title>AndroidDoctor Report — Premium</title>
                <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
                ${HtmlComponents.css()}
                ${HtmlComponents.themeScript()}
            </head>

            <body class="light">
                ${HtmlComponents.gradientHeader("AndroidDoctor Report — Premium", HtmlComponents.premiumThemeToggle())}

                <div class="container">

                    <div class="card">
                        <div class="section-title">Project Overview</div>
                        <div><strong>Generated:</strong> $timestamp</div>
                        <div><strong>Project:</strong> ${escape(report.project?.name)}</div>
                    </div>

                    <div class="card">
                        <div class="section-title">Scores</div>
                        <div>Build Health: ${report.scores?.buildHealth} / 100</div>
                        <div>Modernization: ${report.scores?.modernization} / 100</div>
                    </div>

                    <div class="card">
                        <div class="section-title">Top Actions</div>
                        $actionsHtml
                    </div>

                </div>
            </body>
        </html>
        """.trimIndent()
    }

    private fun escape(value: String?): String = value?.replace("<", "&lt;")?.replace(">", "&gt;") ?: ""

    private fun cleanDate(raw: String?): String {
        return try {
            val zdt = ZonedDateTime.parse(raw)
            zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } catch (e: Exception) {
            raw ?: "<unknown>"
        }
    }
}
