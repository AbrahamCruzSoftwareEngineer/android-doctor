package com.evolutiondso.androiddoctor.cli.render.terminal

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

object TerminalRenderer {

    fun render(report: AndroidDoctorReport) {
        println("AndroidDoctor Report Summary")
        println("----------------------------")

        val name = report.project?.name ?: "<unknown>"
        val path = report.project?.path ?: "<unknown>"
        val status = report.status ?: "<unknown>"

        println("Project : $name ($path)")
        println("Status  : $status")
        println()

        // Display scores
        val health = report.scores?.buildHealth ?: 0
        val modern = report.scores?.modernization ?: 0
        println("Scores:")
        println("  Build Health : $health")
        println("  Modernization: $modern")
        println()

        println("Actions:")
        report.actions.orEmpty().forEachIndexed { i, a ->
            println("  ${i + 1}. ${a.title} [${a.priority}]")
        }

        println()
        println("Diagnostics:")
        println("  Config Time : ${report.performance?.configurationMs?.let { "${it} ms" } ?: report.diagnostics?.configuration?.durationMs?.let { "${it} ms" } ?: "Unknown"}")
        println("  Exec Time   : ${report.performance?.executionMs?.let { "${it} ms" } ?: report.diagnostics?.execution?.durationMs?.let { "${it} ms" } ?: "Unknown"}")
        val cache = report.cache ?: report.diagnostics?.buildCache
        println("  Cache Hits  : ${cache?.hits ?: 0}")
        println("  Cache Misses: ${cache?.misses ?: 0}")
        println("  CI          : ${report.environment?.ci ?: "Unknown"}")
    }
}
