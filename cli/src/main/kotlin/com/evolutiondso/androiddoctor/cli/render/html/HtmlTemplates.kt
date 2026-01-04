package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlTemplates {

    fun page(report: AndroidDoctorReport, bodyContent: String, premium: Boolean): String {

        val safeName = report.project?.name ?: "<unknown>"
        val safeDate = DateFormatter.pretty(report.generatedAt)

        // Inject data for charts.js
        val dataJson = """
            window.__ANDROID_DOCTOR_DATA__ = {
                buildHealth: ${report.scores?.buildHealth ?: 0},
                modernization: ${report.scores?.modernization ?: 0},
                usesKapt: ${report.checks?.usesKapt ?: false},
                moduleCount: ${report.checks?.moduleCount ?: 1},
                actions: ${report.actions?.joinToString(prefix = "[", postfix = "]") { a ->
            """{
                        "title": "${a.title}",
                        "impact": {
                            "buildHealthDelta": ${a.impact?.buildHealthDelta ?: 0},
                            "modernizationDelta": ${a.impact?.modernizationDelta ?: 0}
                        }
                    }"""
        } ?: "[]"}
            };
        """.trimIndent()

        return """
        <!DOCTYPE html>
        <html lang="en" data-theme="light">
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>AndroidDoctor Report — ${if (premium) "Premium" else "Free"}</title>

            <style>${HtmlAssets.styleCss()}</style>

            <!-- Chart.js CDN -->
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        </head>

        <body>
            <header class="header">
                <div class="header-left">
                    <h1>AndroidDoctor Report — ${if (premium) "Premium" else "Free"}</h1>
                    <p class="small muted">$safeDate</p>
                </div>

                <div class="header-right">
                    <button id="themeToggle" class="theme-btn ${if (!premium) "disabled" else ""}">
                        Toggle Theme
                    </button>

                    ${if (!premium)
            """<div class="locked-tag">Premium Feature</div>""" else ""}
                </div>
            </header>

            <main>
                $bodyContent
            </main>

            <script>
                const IS_PREMIUM = $premium;
                $dataJson
            </script>

            <script>${HtmlAssets.chartsJs()}</script>
            <script>${HtmlAssets.appJs(premium)}</script>
        </body>
        </html>
        """.trimIndent()
    }
}
