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
        val configDuration = report.performance?.configurationMs ?: report.diagnostics?.configuration?.durationMs ?: 0
        val executionDuration = report.performance?.executionMs ?: report.diagnostics?.execution?.durationMs ?: 0
        val cacheHits = report.cache?.hits ?: report.diagnostics?.buildCache?.hits ?: 0
        val cacheMisses = report.cache?.misses ?: report.diagnostics?.buildCache?.misses ?: 0
        val incrementalCompile = report.performance?.incrementalCompilation
            ?: report.diagnostics?.buildCache?.incrementalCompilationUsed
            ?: false
        val configCacheRequested = report.diagnostics?.configurationCache?.requested ?: false
        val outdatedDeps = report.dependencies?.outdated?.size ?: 0
        val duplicateDeps = report.dependencies?.duplicates?.size ?: 0
        val architecture = report.architecture
        val mvcScore = architecture?.mvc ?: 0
        val mvpScore = architecture?.mvp ?: 0
        val mvvmScore = architecture?.mvvm ?: 0
        val mviScore = architecture?.mvi ?: 0
        val architectureScore = (mvvmScore + mviScore).coerceAtMost(100)

        val annotationMs = report.annotationProcessing?.totalProcessingMs
        val totalMs = listOfNotNull(configDuration.takeIf { it > 0 }, executionDuration.takeIf { it > 0 }, annotationMs)
            .sum()

        val fallbackConfig = when (configurationCacheEnabled) {
            true -> 18
            false -> 36
            null -> 28
        }
        val fallbackAnnotation = if (usesKapt) 22 else 10
        val fallbackExecution = (100 - fallbackConfig - fallbackAnnotation).coerceAtLeast(10)

        val configShare = if (totalMs > 0) ((configDuration.toDouble() / totalMs) * 100).toInt() else fallbackConfig
        val annotationShare = if (totalMs > 0) (((annotationMs ?: 0).toDouble() / totalMs) * 100).toInt() else fallbackAnnotation
        val executionShare = if (totalMs > 0) (100 - configShare - annotationShare).coerceAtLeast(0) else fallbackExecution

        val actionsJson = report.actions?.joinToString(prefix = "[", postfix = "]") { action ->
            """{
                "title": "${escapeJs(action.title)}",
                "impact": {
                    "buildHealthDelta": ${action.impact?.buildHealthDelta ?: 0},
                    "modernizationDelta": ${action.impact?.modernizationDelta ?: 0}
                }
            }"""
        } ?: "[]"

        val architectureViolationsJson = architecture?.violations?.joinToString(prefix = "[", postfix = "]") { violation ->
            """{
                "type": "${escapeJs(violation.type)}",
                "file": "${escapeJs(violation.file)}",
                "description": "${escapeJs(violation.description)}"
            }"""
        } ?: "[]"

        val architectureFixesJson = architecture?.recommendedFixes?.joinToString(prefix = "[", postfix = "]") { fix ->
            """{
                "title": "${escapeJs(fix.title)}",
                "description": "${escapeJs(fix.description)}"
            }"""
        } ?: "[]"

        val tests = report.tests
        val testsJson = tests?.let {
            """{
                "total": ${it.total ?: 0},
                "passed": ${it.passed ?: 0},
                "failed": ${it.failed ?: 0},
                "skipped": ${it.skipped ?: 0},
                "durationMs": ${it.durationMs ?: 0},
                "uiTestDurationMs": ${it.uiTestDurationMs ?: 0}
            }"""
        } ?: "null"

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
                architecture: {
                    mvc: $mvcScore,
                    mvp: $mvpScore,
                    mvvm: $mvvmScore,
                    mvi: $mviScore,
                    score: $architectureScore,
                    violations: $architectureViolationsJson,
                    recommendedFixes: $architectureFixesJson
                },
                tests: $testsJson,
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
