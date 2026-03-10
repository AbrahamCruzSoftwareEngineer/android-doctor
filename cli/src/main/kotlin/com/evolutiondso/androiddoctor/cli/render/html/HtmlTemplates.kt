package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlTemplates {

    fun page(report: AndroidDoctorReport, bodyContent: String): String {
        val safeName = report.project?.name ?: "<unknown>"
        val safeDate = DateFormatter.pretty(report.generatedAt)

        return """
        <!DOCTYPE html>
        <html lang="en" data-theme="light">
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>AndroidDoctor Report</title>

            <style>${HtmlAssets.styleCss()}</style>
        </head>

        <body>
            <header class="header">
                <div class="header-left">
                    <div class="title">AndroidDoctor Report</div>
                    <div class="subtitle">Project: $safeName</div>
                    <div class="meta">Generated: $safeDate</div>
                </div>
            </header>

            <main class="container">
                $bodyContent
            </main>
        </body>
        </html>
        """.trimIndent()
    }
}
