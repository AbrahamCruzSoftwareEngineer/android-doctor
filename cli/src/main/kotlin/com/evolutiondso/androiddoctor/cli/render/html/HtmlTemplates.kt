package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlTemplates {

    fun page(
        report: AndroidDoctorReport,
        bodyContent: String,
        premium: Boolean
    ): String {

        val safeDate = DateFormatter.pretty(report.generatedAt)
        val safeName = report.project?.name ?: "Unknown"

        return """
        <!DOCTYPE html>
        <html lang="en" data-theme="light">
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>AndroidDoctor Report — ${if (premium) "Premium" else "Free"}</title>
            <style>
                ${HtmlAssets.styleCss()}
            </style>
        </head>

        <body>
            <header class="header">
                <div class="left">
                    <h1>AndroidDoctor Report — ${if (premium) "Premium" else "Free"}</h1>
                    <p class="muted">Generated: $safeDate</p>
                </div>

                <div class="right">
                    <button id="themeToggle" 
                        class="theme-btn ${if (!premium) "disabled" else ""}">
                        Toggle Theme
                    </button>

                    ${if (!premium) """
                    <span class="locked-tag">Premium Feature</span>
                    """.trimIndent() else ""}
                </div>
            </header>

            <main class="content">
                $bodyContent
            </main>

            <script>
                ${HtmlAssets.appJs(premium)}
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}
