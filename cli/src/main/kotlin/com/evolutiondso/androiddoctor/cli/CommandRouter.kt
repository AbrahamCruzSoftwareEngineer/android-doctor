package com.evolutiondso.androiddoctor.cli

import com.evolutiondso.androiddoctor.core.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.html.FreeHtmlRenderer
import com.evolutiondso.androiddoctor.cli.render.markdown.MarkdownRenderer
import com.evolutiondso.androiddoctor.cli.render.terminal.TerminalRenderer
import java.io.File

/**
 * Handles CLI commands after a report is loaded.
 */
class CommandRouter {

    fun printHelp() {
        println(
            """
            AndroidDoctor CLI
            
            Usage:
              androiddoctor --report <file> [--html] [--md] [--open]
              
            Options:
              --report, -r      Path to report.json
              --html            Export HTML report
              --md              Export Markdown report
              --open            Open the exported file automatically
              --help, -h        Show this help
            """.trimIndent()
        )
    }

    /**
     * Main dispatch for rendering a loaded report.
     */
    fun handleReport(report: AndroidDoctorReport, cliArgs: Array<String>) {
        val args = parsedArgs(cliArgs)

        var exported = false
        var exportedFile: String? = null

        if (args.exportHtml) {
            val outputPath = "build/androidDoctor/html/report.html"
            exportedFile = FreeHtmlRenderer().renderToFile(report, outputPath)
            println("HTML report exported → $exportedFile")
            exported = true
        }

        if (args.exportMarkdown) {
            val outputPath = "build/androidDoctor/markdown/report.md"
            exportedFile = MarkdownRenderer.renderToFile(report, outputPath)
            println("📝 Markdown report exported → $exportedFile")
            exported = true
        }

        if (exported && args.openFile && exportedFile != null) {
            openFile(exportedFile)
        }

        if (exported) return

        TerminalRenderer.render(report)
    }

    private fun parsedArgs(raw: Array<String>): ParsedArgs {
        return ParsedArgs(
            exportHtml = raw.contains("--html"),
            exportMarkdown = raw.contains("--md"),
            openFile = raw.contains("--open")
        )
    }

    private data class ParsedArgs(
        val exportHtml: Boolean = false,
        val exportMarkdown: Boolean = false,
        val openFile: Boolean = false
    )

    private fun openFile(path: String) {
        val file = File(path)
        if (!file.exists()) {
            println("Cannot open file — does not exist: $path")
            return
        }

        try {
            val os = System.getProperty("os.name").lowercase()

            val process = when {
                os.contains("mac") -> ProcessBuilder("open", file.absolutePath)
                os.contains("linux") -> ProcessBuilder("xdg-open", file.absolutePath)
                os.contains("windows") -> ProcessBuilder("cmd", "/c", "start", file.absolutePath)
                else -> {
                    println("Unsupported OS — cannot auto-open file.")
                    return
                }
            }

            process.start()
            println("Opened: ${file.absolutePath}")

        } catch (e: Exception) {
            println("Failed to open file automatically: ${e.message}")
        }
    }
}
