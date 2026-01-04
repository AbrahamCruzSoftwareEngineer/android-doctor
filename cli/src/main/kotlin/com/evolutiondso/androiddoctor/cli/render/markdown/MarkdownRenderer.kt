package com.evolutiondso.androiddoctor.cli.render.markdown

import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import java.nio.file.Files
import java.nio.file.Paths

object MarkdownRenderer {

    fun render(report: AndroidDoctorReport): String {
        val project = report.project?.name ?: "<unknown>"
        val buildScore = report.scores?.buildHealth ?: 0
        val modernScore = report.scores?.modernization ?: 0

        val actions = report.actions.orEmpty().joinToString("\n") { a ->
            buildString {
                appendLine("- **${a.title}**")
                appendLine("  - Why: ${a.why}")
                appendLine("  - How: ${a.how}")
                appendLine("  - Impact: +${a.impact?.buildHealthDelta} / +${a.impact?.modernizationDelta}")
            }
        }

        return """
        # AndroidDoctor Premium Markdown Report

        **Project:** $project  
        **Build Health:** $buildScore  
        **Modernization:** $modernScore  

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
