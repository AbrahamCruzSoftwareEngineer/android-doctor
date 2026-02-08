package com.evolutiondso.androiddoctor.cli.render.markdown

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import java.nio.file.Files
import java.nio.file.Paths

object MarkdownRenderer {

    fun render(report: AndroidDoctorReport): String {
        val project = report.project?.name ?: "<unknown>"
        val buildScore = report.scores?.buildHealth ?: 0
        val modernScore = report.scores?.modernization ?: 0
        val diagnostics = report.diagnostics
        val cacheHits = report.cache?.hits ?: diagnostics?.buildCache?.hits
        val cacheMisses = report.cache?.misses ?: diagnostics?.buildCache?.misses
        val performance = report.performance
        val configCache = diagnostics?.configurationCache
        val deps = report.dependencies
        val architecture = report.architecture

        val actions = report.actions.orEmpty().joinToString("\n") { a ->
            buildString {
                appendLine("- **${a.title}**")
                appendLine("  - Why: ${a.why}")
                appendLine("  - How: ${a.how}")
                appendLine("  - Impact: +${a.impact?.buildHealthDelta} / +${a.impact?.modernizationDelta}")
            }
        }

        val longestTasks = report.diagnostics?.execution?.topLongestTasks.orEmpty().joinToString("\n") { task ->
            "- `${task.path}` (${task.durationMs ?: "?"} ms)"
        }.ifBlank { "- No task timing data available." }

        val outdated = deps?.outdated.orEmpty().joinToString("\n") { item ->
            "- ${item.group}:${item.name} ${item.currentVersion} → ${item.latestVersion}"
        }.ifBlank { "- None" }

        val duplicates = deps?.duplicates.orEmpty().joinToString("\n") { item ->
            "- ${item.group}:${item.name} (${item.versions?.joinToString(", ") ?: "?"})"
        }.ifBlank { "- None" }

        val unused = deps?.unused.orEmpty().joinToString("\n") { item ->
            "- ${item.group}:${item.name} (${item.version ?: "?"}) in ${item.configuration}"
        }.ifBlank { "- None" }

        val heavy = deps?.heavy.orEmpty().joinToString("\n") { item ->
            "- ${item.group}:${item.name} (${item.version ?: "?"}) ${item.sizeBytes ?: 0} bytes"
        }.ifBlank { "- None" }

        val diagnosticsModules = report.modulesDiagnostics?.modules
        val moduleList = when {
            diagnosticsModules?.isNotEmpty() == true -> diagnosticsModules.joinToString("\n") { module ->
                "- ${module.path}: tasks ${module.taskCount ?: 0}, time ${module.executionMs?.let { "${it} ms" } ?: "Unknown"}, kapt ${module.usesKapt ?: "Unknown"}, cache ${module.buildCacheEnabled ?: "Unknown"}"
            }
            report.modules?.isNotEmpty() == true -> report.modules.orEmpty().joinToString("\n") { module ->
                "- ${module.name}: tasks ${module.tasks ?: 0}, time ${module.totalMs?.let { "${it} ms" } ?: "Unknown"}, kapt ${module.usesKapt ?: "Unknown"}, cache ${module.buildCacheEnabled ?: "Unknown"}"
            }
            else -> "- No module data available."
        }

        val architectureDistribution = architecture?.let { dist ->
            "- MVC: ${dist.mvc ?: 0}%\n- MVP: ${dist.mvp ?: 0}%\n- MVVM: ${dist.mvvm ?: 0}%\n- MVI: ${dist.mvi ?: 0}%"
        } ?: "- No distribution data."
        val architectureViolations = architecture?.violations.orEmpty().joinToString("\n") { violation ->
            "- ${violation.type}: ${violation.description} (file: ${violation.file ?: "N/A"})"
        }.ifBlank { "- None" }
        val architectureRecommendations = architecture?.recommendedFixes.orEmpty().joinToString("\n") { rec ->
            "- ${rec.title}: ${rec.description}"
        }.ifBlank { "- None" }

        return """
        # AndroidDoctor Premium Markdown Report

        **Project:** $project  
        **Build Health:** $buildScore  
        **Modernization:** $modernScore  

        ## Build Performance
        - Configuration: ${performance?.configurationMs?.let { "${it} ms" } ?: diagnostics?.configuration?.durationMs?.let { "${it} ms" } ?: "Unknown"}
        - Execution: ${performance?.executionMs?.let { "${it} ms" } ?: diagnostics?.execution?.durationMs?.let { "${it} ms" } ?: "Unknown"}
        - Build Cache: Hits ${cacheHits ?: 0} / Misses ${cacheMisses ?: 0}
        - Incremental Compilation: ${performance?.incrementalCompilation ?: diagnostics?.buildCache?.incrementalCompilationUsed ?: "Unknown"}
        - Longest Tasks:
        $longestTasks

        ## Configuration Cache Report
        - Requested: ${configCache?.requested ?: "Unknown"}
        - Stored: ${configCache?.stored ?: "Unknown"}
        - Reused: ${configCache?.reused ?: "Unknown"}
        - Incompatible Tasks: ${configCache?.incompatibleTasks ?: "Unknown"}

        ## Dependency Insights
        ### Outdated
        $outdated
        ### Duplicates
        $duplicates
        ### Unused
        $unused
        ### Heavy
        $heavy

        ## Toolchain Diagnostics
        - Kotlin Compiler: ${report.tooling?.kotlinCompilerVersion ?: "Unknown"}
        - Kotlin JVM Target: ${report.toolchain?.kotlinJvmTarget ?: "Unknown"}
        - Java Toolchain: ${report.toolchain?.javaToolchainVersion ?: "Unknown"}
        - AGP Version: ${report.android?.agpVersion ?: "Unknown"}
        - compileSdk: ${report.android?.compileSdk ?: "Unknown"}
        - JVM Mismatch: ${report.toolchain?.jvmTargetMismatch ?: "Unknown"}

        ## Module Graph Summary
        $moduleList

        ## Annotation Processing Metrics
        - Processors: ${report.annotationProcessing?.processors?.joinToString(", ") ?: "Unknown"}
        - Total Time: ${report.annotationProcessing?.totalProcessingMs?.let { "${it} ms" } ?: "Unknown"}
        - Kapt Stub Overhead: ${report.annotationProcessing?.kaptStubGenerationMs?.let { "${it} ms" } ?: "Unknown"}

        ## Compose Compiler Insights
        - Enabled: ${report.android?.composeEnabled ?: "Unknown"}
        - Compiler Version: ${report.android?.composeCompilerVersion ?: "Unknown"}
        - Metrics Enabled: ${report.android?.composeMetricsEnabled ?: "Unknown"}
        - Reports Enabled: ${report.android?.composeReportsEnabled ?: "Unknown"}

        ## Environment Metadata
        - OS: ${report.environment?.os ?: "Unknown"}
        - Arch: ${report.environment?.arch ?: "Unknown"}
        - CI: ${report.environment?.ci ?: "Unknown"}
        - RAM: ${report.environment?.availableRamMb?.let { "${it} MB" } ?: "Unknown"}

        ## Test Diagnostics
        - Total: ${report.tests?.total ?: 0}
        - Passed: ${report.tests?.passed ?: 0}
        - Failed: ${report.tests?.failed ?: 0}
        - Skipped: ${report.tests?.skipped ?: 0}
        - Duration: ${report.tests?.durationMs?.let { "${it} ms" } ?: "Unknown"}
        - UI Test Duration: ${report.tests?.uiTestDurationMs?.let { "${it} ms" } ?: "Unknown"}

        ## Architecture Diagnostics
        ### Detected Patterns
        $architectureDistribution
        ### Violations
        $architectureViolations
        ### Recommended Fixes
        $architectureRecommendations

        ## Recommended Actions
        $actions
        """.trimIndent()
    }

    fun renderToFile(report: AndroidDoctorReport, outputPath: String): String {
        val md = render(report)
        val path = Paths.get(outputPath)

        Files.createDirectories(path.parent)
        Files.writeString(path, md)

        return path.toAbsolutePath().toString()
    }
}
