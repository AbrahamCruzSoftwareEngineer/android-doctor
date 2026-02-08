package com.evolutiondso.androiddoctor.cli.render.html

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.utils.DateFormatter

object HtmlTemplates {

    private fun escapeJs(value: String?): String {
        return value
            ?.replace("\\", "\\\\")
            ?.replace("\"", "\\\"")
            ?.replace("\n", "\\n")
            ?.replace("\r", "\\r")
            ?.replace("\t", "\\t")
            ?: ""
    }

    fun page(report: AndroidDoctorReport, bodyContent: String, premium: Boolean): String {

        val safeName = report.project?.name ?: "<unknown>"
        val safeDate = DateFormatter.pretty(report.generatedAt)
        val buildHealth = report.scores?.buildHealth ?: 0
        val modernization = report.scores?.modernization ?: 0
        val composition = if (report.android?.composeEnabled == true) 100 else 0
        val buildImpactTotal = report.actions?.sumOf { it.impact?.buildHealthDelta ?: 0 } ?: 0
        val modernizationImpactTotal = report.actions?.sumOf { it.impact?.modernizationDelta ?: 0 } ?: 0
        val usesKapt = report.checks?.usesKapt == true
        val configurationCacheEnabled = report.checks?.configurationCacheEnabled
        val configDuration = report.diagnostics?.configuration?.durationMs ?: 0
        val executionDuration = report.diagnostics?.execution?.durationMs ?: 0
        val cacheHits = report.diagnostics?.buildCache?.hits ?: 0
        val cacheMisses = report.diagnostics?.buildCache?.misses ?: 0
        val incrementalCompile = report.diagnostics?.buildCache?.incrementalCompilationUsed ?: false
        val configCacheRequested = report.diagnostics?.configurationCache?.requested ?: false
        val outdatedDeps = report.dependencies?.outdated?.size ?: 0
        val duplicateDeps = report.dependencies?.duplicates?.size ?: 0

        val configShare = when (configurationCacheEnabled) {
            true -> 18
            false -> 36
            null -> 28
        }
        val annotationShare = if (usesKapt) 22 else 10
        val executionShare = (100 - configShare - annotationShare).coerceAtLeast(10)

        val actionsJson = report.actions?.joinToString(prefix = "[", postfix = "]") { action ->
            """{
                "title": "${escapeJs(action.title)}",
                "impact": {
                    "buildHealthDelta": ${action.impact?.buildHealthDelta ?: 0},
                    "modernizationDelta": ${action.impact?.modernizationDelta ?: 0}
                }
            }"""
        } ?: "[]"

        val dataJson = """
            window.__ANDROID_DOCTOR_DATA__ = {
                buildHealth: $buildHealth,
                modernization: $modernization,
                composition: $composition,
                impactTotals: {
                    buildHealth: $buildImpactTotal,
                    modernization: $modernizationImpactTotal
                },
                diagnostics: {
                    configurationMs: $configDuration,
                    executionMs: $executionDuration,
                    cacheHits: $cacheHits,
                    cacheMisses: $cacheMisses,
                    incrementalCompilation: $incrementalCompile,
                    configCacheRequested: $configCacheRequested,
                    outdatedDeps: $outdatedDeps,
                    duplicateDeps: $duplicateDeps
                },
                buildTimeBreakdown: {
                    configuration: $configShare,
                    execution: $executionShare,
                    annotation: $annotationShare
                },
                actions: $actionsJson
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

            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        </head>

        <body>
            <header class="header">
                <div class="header-left">
                    <div class="title">AndroidDoctor Report — ${if (premium) "Premium" else "Free"}</div>
                    <div class="subtitle">Project: $safeName</div>
                    <div class="meta">Generated: $safeDate</div>
                </div>

                <div class="header-right">
                    <button id="themeToggle" class="theme-btn ${if (!premium) "disabled" else ""}" ${if (!premium) "disabled" else ""}>
                        Theme
                    </button>
                    ${if (!premium) """<div class=\"locked-tag\">Premium Feature</div>""" else ""}
                </div>
            </header>

            <main class="container">
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
