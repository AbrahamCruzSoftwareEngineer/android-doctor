package com.evolutiondso.androiddoctor.cli

import com.evolutiondso.androiddoctor.cli.capabilities.CapabilitySet
import com.evolutiondso.androiddoctor.cli.model.AndroidDoctorReport

/**
 * Handles CLI commands after a report is loaded.
 */
class CommandRouter {

    fun printHelp() {
        println(
            """
            AndroidDoctor CLI
            
            Usage:
              androiddoctor --report <file>
              androiddoctor --report <file> --html
              
            Options:
              --report, -r   Path to report.json
              --html         Export HTML instead of terminal summary
              --help, -h     Show this help
            """.trimIndent()
        )
    }

    /**
     * Dispatches how a report should be rendered based on capabilities + flags.
     */
    fun handleReport(report: AndroidDoctorReport, capabilities: CapabilitySet) {
        val args = capabilitiesParsedArgs()

        // HTML Mode?
        if (args.exportHtml) {
            val htmlRenderer = capabilities.htmlRenderer
            val output = when (htmlRenderer) {
                is com.evolutiondso.androiddoctor.cli.render.html.FreeHtmlRenderer ->
                    htmlRenderer.render(report)

                is com.evolutiondso.androiddoctor.cli.render.html.PremiumHtmlRenderer ->
                    htmlRenderer.render(report)

                else -> error("Unknown HTML renderer")
            }

            val outputPath = exportHtmlToDisk(output)
            println("✨ HTML report generated at: $outputPath")
            return
        }

        // Default = terminal summary
        capabilities.terminalRenderer.render(report)
    }

    /**
     * Reads process args in a safe way.
     */
    private fun capabilitiesParsedArgs(): ParsedArgs {
        val raw = ProcessHandle.current()
            .info()
            .commandLine()
            .orElse("")
            .split(" ")

        val wantsHtml = raw.contains("--html")

        return ParsedArgs(
            exportHtml = wantsHtml
        )
    }

    /**
     * Writes HTML output to disk.
     */
    private fun exportHtmlToDisk(html: String): String {
        val out = java.nio.file.Paths.get("androiddoctor-report.html")
        java.nio.file.Files.writeString(out, html)
        return out.toAbsolutePath().toString()
    }

    private data class ParsedArgs(
        val exportHtml: Boolean = false
    )
}
