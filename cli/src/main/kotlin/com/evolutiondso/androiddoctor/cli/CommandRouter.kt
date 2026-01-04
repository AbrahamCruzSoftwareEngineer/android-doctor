package com.evolutiondso.androiddoctor.cli

import com.evolutiondso.androiddoctor.cli.capabilities.CapabilitySet
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport
import com.evolutiondso.androiddoctor.cli.render.markdown.MarkdownRenderer
import com.evolutiondso.androiddoctor.cli.render.pdf.PdfRenderer
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
              androiddoctor --report <file> [--html] [--md] [--pdf] [--open]
              
            Options:
              --report, -r      Path to report.json
              --html            Export HTML report
              --md              Export Markdown report (Premium only)
              --pdf             Export PDF report     (Premium only)
              --open            Open the exported file automatically
              --help, -h        Show this help
            """.trimIndent()
        )
    }

    /**
     * Main dispatch for rendering a loaded report based on capability set.
     */
    fun handleReport(report: AndroidDoctorReport, capabilities: CapabilitySet) {
        val args = parsedArgs()

        var exported = false
        var exportedFile: String? = null

        // -----------------------
        // HTML EXPORT
        // -----------------------
        if (args.exportHtml && capabilities.canExportHtml()) {
            capabilities.htmlRenderer?.let { renderer ->
                val outputPath = "build/androidDoctor/html/report.html"
                exportedFile = renderer.renderToFile(report, outputPath)
                println("HTML report exported → $exportedFile")
                exported = true
            }
        }

        // -----------------------
        // MARKDOWN EXPORT
        // -----------------------
        if (args.exportMarkdown && capabilities.canExportMarkdown()) {
            val outputPath = "build/androidDoctor/markdown/report.md"
            exportedFile = MarkdownRenderer.renderToFile(report, outputPath)
            println("📝 Markdown report exported → $exportedFile")
            exported = true
        }

        // -----------------------
        // PDF EXPORT
        // -----------------------
        if (args.exportPdf && capabilities.canExportPdf()) {
            val outputPath = "build/androidDoctor/pdf/report.pdf"
            exportedFile = PdfRenderer.renderToFile(report, outputPath)
            println("📄 PDF report exported → $exportedFile")
            exported = true
        }

        // AUTO-OPEN exported file
        if (exported && args.openFile && exportedFile != null) {
            openFile(exportedFile!!)
        }

        // If exports requested, do not print terminal summary
        if (exported) return

        // -----------------------
        // DEFAULT: TERMINAL SUMMARY
        // -----------------------
        capabilities.terminalRenderer.render(report)
    }

    /**
     * Parse flags from the real CLI invocation.
     */
    private fun parsedArgs(): ParsedArgs {
        val raw = ProcessHandle.current()
            .info()
            .commandLine()
            .orElse("")
            .split(" ")

        return ParsedArgs(
            exportHtml = raw.contains("--html"),
            exportMarkdown = raw.contains("--md"),
            exportPdf = raw.contains("--pdf"),
            openFile = raw.contains("--open")
        )
    }

    private data class ParsedArgs(
        val exportHtml: Boolean = false,
        val exportMarkdown: Boolean = false,
        val exportPdf: Boolean = false,
        val openFile: Boolean = false
    )

    /**
     * Opens a file using the OS default program.
     */
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
