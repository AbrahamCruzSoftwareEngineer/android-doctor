package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.base.HtmlRendererBase
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FreeHtmlRenderer : HtmlRendererBase() {

    override fun render(report: AndroidDoctorReport): String {
        val timestamp = cleanDate(report.generatedAt)

        return """
        <html>
            <head>
                <meta charset="UTF-8" />
                <title>AndroidDoctor Report — Free</title>
                <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
                ${HtmlComponents.css()}
            </head>

            <body class="light">
                ${HtmlComponents.gradientHeader("AndroidDoctor Report — Free", HtmlComponents.freeThemeToggle())}

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
                        ${report.actions.orEmpty().take(1).joinToString("\n") { "<strong>${escape(it.title)}</strong>" }}
                        <div class="upsell" style="margin-top:10px; font-size:14px; opacity:0.7;">
                            Upgrade to Premium to unlock full recommendations ✨
                        </div>
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
        } catch (_: Exception) {
            raw ?: "<unknown>"
        }
    }
}
